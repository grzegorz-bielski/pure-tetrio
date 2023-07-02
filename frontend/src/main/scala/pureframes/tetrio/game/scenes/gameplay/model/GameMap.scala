package pureframes.tetrio.game.scenes.gameplay.model

import indigo.Vector2
import indigo.shared.collections.Batch
import indigo.shared.collections.NonEmptyBatch
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.RGBA
import indigoextras.geometry.*
import indigoextras.trees.QuadTree
import pureframes.tetrio.game.core.*

import scala.annotation.tailrec

final case class GameMap(grid: BoundingBox, quadTree: QuadTree[MapElement]):
  lazy val elements = quadTree.toBatch
  lazy val walls    = elements.collect { case e: MapElement.Wall => e }
  lazy val debris   = elements.collect { case e: MapElement.Debris => e }

  // map size without walls
  lazy val bottomInternal: Int = grid.bottom.toInt - 1
  lazy val topInternal: Int    = grid.top.toInt // no wall
  lazy val leftInternal: Int   = grid.left.toInt + 1
  lazy val rightInternal: Int  = grid.right.toInt - 1

  lazy val xs = leftInternal to rightInternal

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
      quadTree =
        quadTree.insertElements(elements.map(e => e -> e.point.toVertex)).prune
    )

  def insertTetromino(t: Tetromino): GameMap =
    insertDebris(t.positions.toBatch, t.extractOrdinal)

  def insertDebris(pos: Batch[Vector2], ord: Tetromino.Ordinal): GameMap =
    insertElements(pos.map(MapElement.Debris(_, Some(ord))))

  def insertWall(pos: Batch[Vector2]): GameMap =
    insertElements(pos.map(MapElement.Wall(_)))

  def insertFloor(pos: Batch[Vector2]): GameMap =
    insertElements(pos.map(MapElement.Floor(_)))

  def removeFullLines(ys: Batch[Int]): GameMap =
    val mapElements = ys
      .foldLeft(quadTree.toBatch) { (acc, y) =>
        val moved = acc.collect {
          case e: MapElement.Debris if e.point.y < y =>
            e.copy(point = e.point.moveBy(Vector2(0, 1)))
        }

        val rest = acc.collect {
          case e: MapElement.Debris if e.point.y > y          => e
          case e @ (_: MapElement.Floor | _: MapElement.Wall) => e
        }

        moved ++ rest
      }

    copy(
      quadTree = QuadTree(mapElements.map(e => e -> e.point.toVertex))
    )

  // TODO: do we need this method ?
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

  def fromElements(elements: Batch[MapElement]): GameMap =
    GameMap(
      grid = BoundingBox.fromVertexCloud(elements.map(_.point.toVertex)),
      quadTree = QuadTree(elements.map(e => e -> e.point.toVertex))
    )

  def walled(grid: BoundingBox): GameMap =
    import pureframes.tetrio.game.core.given // scalafix error when imported on top (?)

    GameMap(grid)
      // .insertWalls(grid.topLeft --> grid.topRight) // no top wall
      .insertWall(grid.topRight --> grid.bottomRight)
      .insertFloor(grid.bottomLeft --> grid.bottomRight)
      .insertWall(grid.topLeft --> grid.bottomLeft)

  def fromGamePlan(plan: String): GameMap =
    val lines = plan
      .stripMargin('|')
      .split("\n")
      .toBatch
      .collect {
        case str if !str.isBlank => str.toBatch.zipWithIndex
      }
      .zipWithIndex

    val spawnOffset = 2
    val wallOffset  = 1

    def debrisOf(x: Int, y: Int, ordinal: Option[Tetromino.Ordinal]) =
      MapElement.Debris(Vector2(x + wallOffset, y + spawnOffset), ordinal)

    val elements = lines.flatMap { (line, y) =>
      line.collect {
        case ('D', x) => debrisOf(x, y, None)
        case ('I', x) => debrisOf(x, y, Some(0))
        case ('J', x) => debrisOf(x, y, Some(1))
        case ('L', x) => debrisOf(x, y, Some(2))
        case ('O', x) => debrisOf(x, y, Some(3))
        case ('S', x) => debrisOf(x, y, Some(4))
        case ('T', x) => debrisOf(x, y, Some(5))
        case ('Z', x) => debrisOf(x, y, Some(6))
      }
    }

    val height = lines.size
    val width  = lines.head._1.size + wallOffset

    GameMap
      .walled(
        BoundingBox(
          x = 0,
          y = spawnOffset,
          width = width,
          height = height
        )
      )
      .insertElements(elements)

enum MapElement derives CanEqual:
  case Wall(point: Vector2)
  case Floor(point: Vector2)
  case Debris(point: Vector2, tetrominoOrdinal: Option[Tetromino.Ordinal])

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
