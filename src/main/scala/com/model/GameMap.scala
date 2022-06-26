package com.model

import indigo.shared.collections.Batch
import indigo.shared.collections.NonEmptyBatch
import indigo.shared.datatypes.Point
import indigoextras.geometry.*
import indigoextras.trees.QuadTree

import scala.annotation.tailrec

final case class GameMap(quadTree: QuadTree[MapElement]):
  def mapElements = quadTree.toBatch

  def intersects(position: Vertex): Boolean = 
    !intersectsWith(position).isEmpty

  def intersects(position: Point): Boolean = 
    !intersectsWith(position).isEmpty

  def intersects(positions: NonEmptyBatch[Point]): Boolean =
    !intersectsWith(positions).isEmpty

  def intersectsWith(position: Vertex): Option[MapElement] =
    quadTree.fetchElementAt(position)

  def intersectsWith(position: Point): Option[MapElement] =
    intersectsWith(position.toVertex)

  def intersectsWith(positions: NonEmptyBatch[Point]): Batch[MapElement] =
    positions.toBatch.flatMap(p => Batch.fromOption(intersectsWith(p)))

  def insertElements(elements: Batch[MapElement]): GameMap =
    copy(
      quadTree = quadTree.insertElements(elements.map(e => e -> e.point)).prune
    )

  def insertDebris(pos: Batch[Vertex]) =
    insertElements(pos.map(MapElement.Debris(_)))

  def insertWall(pos: Batch[Vertex]) =
    insertElements(pos.map(MapElement.Wall(_)))

  def insertFloor(pos: Batch[Vertex]) =
    insertElements(pos.map(MapElement.Floor(_)))

object GameMap:
  def apply(grid: BoundingBox): GameMap =
    val gridSize = grid.size + grid.position + Vertex.one

    GameMap(QuadTree.empty[MapElement](gridSize))

  def walled(grid: BoundingBox): GameMap =
    val map =
      GameMap(grid)
        // .insertWalls(grid.topLeft --> grid.topRight)
        .insertWall(grid.topRight --> grid.bottomRight)
        .insertFloor(grid.bottomLeft --> grid.bottomRight)
        .insertWall(grid.topLeft --> grid.bottomLeft)

    map

enum MapElement derives CanEqual:
  case Wall(point: Vertex)
  case Floor(point: Vertex)
  case Debris(point: Vertex)

extension (underlying: MapElement)
  def point = underlying match
    case MapElement.Floor(p)  => p
    case MapElement.Wall(p)   => p
    case MapElement.Debris(p) => p

extension (underlying: Vertex)
  // adapted from snake demo
  def -->(end: Vertex): Batch[Vertex] =
    val start = underlying

    @tailrec
    def go(
        last: Vertex,
        dest: Vertex,
        p: Vertex => Boolean,
        acc: Batch[Vertex]
    ): Batch[Vertex] =
      if p(last) then acc
      else
        val next =
          Vertex(
            x = if (last.x + 1 <= end.x) last.x + 1 else last.x,
            y = if (last.y + 1 <= end.y) last.y + 1 else last.y
          )
        go(next, dest, p, acc :+ next)

    if lessThanOrEqual(start, end) then go(start, end, _ == end, Batch(start))
    else go(end, start, _ == start, Batch(end))

  private def lessThanOrEqual(a: Vertex, b: Vertex): Boolean =
    a.x <= b.x && a.y <= b.y
