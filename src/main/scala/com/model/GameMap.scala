package com.model

import indigo.shared.collections.Batch
import indigoextras.geometry.*
import indigoextras.trees.QuadTree

import scala.annotation.tailrec

final case class GameMap(
    // used for collision detection
    // https://gamedevelopment.tutsplus.com/tutorials/quick-tip-use-quadtrees-to-detect-likely-collisions-in-2d-space--gamedev-374
    val quadTree: QuadTree[MapElement]
):
  def mapElements = quadTree.toBatch

  def intersects(tetramino: Tetramino): Batch[MapElement] =
    tetramino.positions.flatMap(p =>
      Batch.fromOption(
        quadTree
          .fetchElementAt(Vertex.fromPoint(p))
      )
    )

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
    case MapElement.Floor(p)     => p
    case MapElement.Wall(p)      => p
    case MapElement.Debris(p) => p

// extension (underlying: Batch[MapElement])
//   def hasFloors: Boolean =
//     underlying.exists {
//       case e: MapElement.Floor => true
//       case _                   => false
//     }

extension (underlying: Vertex)
  // adapted from snake demo
  def -->(end: Vertex): Batch[Vertex] =
    val start = underlying

    // println(start -> end)

    @tailrec
    def rec(
        last: Vertex,
        dest: Vertex,
        p: Vertex => Boolean,
        acc: Batch[Vertex]
    ): Batch[Vertex] =
      if p(last) then acc
      else
        val nextX: Double = if (last.x + 1 <= end.x) last.x + 1 else last.x
        val nextY: Double = if (last.y + 1 <= end.y) last.y + 1 else last.y
        val next: Vertex  = Vertex(nextX, nextY)
        // println(next)
        rec(next, dest, p, acc :+ next)

    if lessThanOrEqual(start, end) then
      val res = rec(start, end, (gp: Vertex) => gp == end, Batch(start))
      println("res" -> res)
      res
    else rec(end, start, (gp: Vertex) => gp == start, Batch(end))

  private def lessThanOrEqual(a: Vertex, b: Vertex): Boolean =
    a.x <= b.x && a.y <= b.y
