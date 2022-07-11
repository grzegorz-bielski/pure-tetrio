package com.scenes.gameplay.model

import com.core.*
import indigo.shared.collections.Batch
import indigo.shared.collections.NonEmptyBatch
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.RGBA
import indigoextras.geometry.*
import indigoextras.trees.QuadTree

import scala.annotation.tailrec

final case class GameMap(grid: BoundingBox, quadTree: QuadTree[MapElement]):
  lazy val mapElements = quadTree.toBatch
  lazy val walls       = mapElements.collect { case e: MapElement.Wall => e }
  lazy val debris      = mapElements.collect { case e: MapElement.Debris => e }

  // map size without walls
  lazy val bottomInternal: Int = grid.bottom.toInt - 1
  lazy val topInternal: Int    = grid.top.toInt // no wall
  lazy val leftInternal: Int   = grid.left.toInt + 1
  lazy val rigthInternal: Int  = grid.right.toInt - 1

  lazy val xs = leftInternal to rigthInternal

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

  def insertTetromino(t: Tetromino): GameMap =
    insertDebris(t.positions.map(_.toVertex).toBatch, t.extractOrdinal)

  def insertDebris(pos: Batch[Vertex], ord: Tetromino.Ordinal): GameMap =
    insertElements(pos.map(MapElement.Debris(_, ord)))

  def insertWall(pos: Batch[Vertex]): GameMap =
    insertElements(pos.map(MapElement.Wall(_)))

  def insertFloor(pos: Batch[Vertex]): GameMap =
    insertElements(pos.map(MapElement.Floor(_)))

  def removeFullLines(ys: Batch[Int]): GameMap =
    ys.toJSArray.minOption
      .map { yMin =>
        val withRemovedLines = ys
          .foldLeft(quadTree) { (acc, y) =>
            xs.foldLeft(acc) { (acc, x) =>
              acc.removeElement(Vertex(x.toDouble, y.toDouble))
            }
          }

        val withMovedDebris = withRemovedLines.update {
          case e: MapElement.Debris if e.point.y <= yMin =>
            val nextPoint = e.point.moveBy(Vertex(0, ys.size))
            e.copy(point = nextPoint) -> nextPoint
        }

        copy(
          quadTree = withMovedDebris.prune
        )
      }
      .getOrElse(this)

  def fullLinesWith(t: Tetromino): Batch[Int] =
    val ys = t.highestPoint.y to t.lowestPoint.y

    ys.foldLeft(Batch.empty[Int]) { (acc, y) =>
      if xs.map(Point(_, y)) forall intersects then acc :+ y else acc
    }

  def reset: GameMap =
    GameMap.walled(grid)

object GameMap:
  def apply(grid: BoundingBox): GameMap =
    // move the grid to center
    val gridSize = grid.size + grid.position + Vertex.one

    GameMap(grid, QuadTree.empty[MapElement](gridSize))

  def walled(grid: BoundingBox): GameMap =
    GameMap(grid)
      // .insertWalls(grid.topLeft --> grid.topRight) // no top wall
      .insertWall(grid.topRight --> grid.bottomRight)
      .insertFloor(grid.bottomLeft --> grid.bottomRight)
      .insertWall(grid.topLeft --> grid.bottomLeft)

enum MapElement derives CanEqual:
  case Wall(point: Vertex)
  case Floor(point: Vertex)
  case Debris(point: Vertex, tetrominoOrdinal: Tetromino.Ordinal)

extension (underlying: MapElement)
  def point = underlying match
    case MapElement.Floor(p)     => p
    case MapElement.Wall(p)      => p
    case MapElement.Debris(p, _) => p

extension [A](underlying: QuadTree[A])
  def update[B >: A](fn: PartialFunction[A, (B, Vertex)]): QuadTree[B] =
    QuadTree(
      underlying.toBatchWithPosition.map((v, a) => fn.applyOrElse(a, (_, v)))
    )

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
