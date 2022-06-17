package com.model

import indigo.shared.collections.Batch
import indigoextras.geometry.*
import indigoextras.trees.QuadTree

import scala.annotation.tailrec

final case class GameMap(
    // used for collision detection
    // https://gamedevelopment.tutsplus.com/tutorials/quick-tip-use-quadtrees-to-detect-likely-collisions-in-2d-space--gamedev-374
    private val quadTree: QuadTree[MapElement]
):
  def mapElements = quadTree.toBatch

  def insertElements(elements: Batch[MapElement]): GameMap =
    copy(
      quadTree = quadTree.insertElements(elements.map(e => e -> e.point)).prune
    )

  def insertWalls(pos: Batch[Vertex]) =
    insertElements(pos.map(MapElement.Wall(_)))

  // def spawn

object GameMap:
  def apply(grid: BoundingBox): GameMap =
    val gridSize = (grid.size + grid.position + Vertex.one)

    GameMap(QuadTree.empty[MapElement](gridSize))

  def walled(grid: BoundingBox): GameMap =
    val map = 
      GameMap(grid)
        .insertWalls(grid.topLeft --> grid.topRight)
        .insertWalls(grid.topRight --> grid.bottomRight)
        .insertWalls(grid.bottomLeft --> grid.bottomRight)
        .insertWalls(grid.topLeft --> grid.bottomLeft)

    println(
      "ele" -> map.mapElements
    )

    map

enum MapElement derives CanEqual:
  case Wall(point: Vertex)
  case Debris(point: Vertex, colour: String)

extension (underlying: MapElement)
  def point = underlying match
    case MapElement.Wall(p)      => p
    case MapElement.Debris(p, _) => p

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
