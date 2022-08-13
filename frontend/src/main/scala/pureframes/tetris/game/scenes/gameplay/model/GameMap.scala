package pureframes.tetris
package game.scenes.gameplay.model

import indigo.Vector2
import indigo.shared.collections.Batch
import indigo.shared.collections.NonEmptyBatch
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.RGBA
import indigoextras.geometry.*
import indigoextras.trees.QuadTree
import pureframes.tetris.game.core.*

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

  def intersects(position: Vector2): Boolean =
    !intersectsWith(position).isEmpty

  def intersects(positions: NonEmptyBatch[Vector2]): Boolean =
    !intersectsWith(positions).isEmpty

  def intersectsWith(position: Vector2): Option[MapElement] =
    quadTree.fetchElementAt(Vertex.fromVector2(position))

  def intersectsWith(positions: NonEmptyBatch[Vector2]): Batch[MapElement] =
    positions.toBatch.flatMap(p => Batch.fromOption(intersectsWith(p)))

  def insertElements(elements: Batch[MapElement]): GameMap =
    copy(
      quadTree = quadTree.insertElements(elements.map(e => e -> Vertex.fromVector2(e.point))).prune
    )

  def insertTetromino(t: Tetromino): GameMap =
    insertDebris(t.positions.toBatch, t.extractOrdinal)

  def insertDebris(pos: Batch[Vector2], ord: Tetromino.Ordinal): GameMap =
    insertElements(pos.map(MapElement.Debris(_, ord)))

  def insertWall(pos: Batch[Vector2]): GameMap =
    insertElements(pos.map(MapElement.Wall(_)))

  def insertFloor(pos: Batch[Vector2]): GameMap =
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
            val nextPoint = e.point.moveBy(Vector2(0, ys.size))
            e.copy(point = nextPoint) -> Vertex.fromVector2(nextPoint)
        }

        copy(
          quadTree = withMovedDebris.prune
        )
      }
      .getOrElse(this)

  def fullLinesWith(t: Tetromino): Batch[Int] =
    val ys = t.highestPoint.y.toInt to t.lowestPoint.y.toInt

    ys.foldLeft(Batch.empty[Int]) { (acc, y) =>
      if xs.map(Vector2(_, y)) forall intersects then acc :+ y else acc
    }

  def reset: GameMap =
    GameMap.walled(grid)

object GameMap:
  def apply(grid: BoundingBox): GameMap =
    // move the grid to center
    val gridSize = grid.size + grid.position + Vertex.one
    GameMap(grid, QuadTree.empty[MapElement](gridSize))

  def walled(grid: BoundingBox): GameMap =
    import pureframes.tetris.game.core.given // scalafix error when importen on top (?)
    
    GameMap(grid)
      // .insertWalls(grid.topLeft --> grid.topRight) // no top wall
      .insertWall(grid.topRight --> grid.bottomRight)
      .insertFloor(grid.bottomLeft --> grid.bottomRight)
      .insertWall(grid.topLeft --> grid.bottomLeft)

enum MapElement derives CanEqual:
  case Wall(point: Vector2)
  case Floor(point: Vector2)
  case Debris(point: Vector2, tetrominoOrdinal: Tetromino.Ordinal)

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
