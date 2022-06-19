package com.model

import cats.syntax.apply
import indigo.*
import indigo.shared.collections.Batch
import indigo.shared.collections.Batch.Unapply._

// https://tetris.fandom.com/wiki/SRS
// https://harddrop.com/wiki/SRS

object Rotation:
  enum Direction(val index: Int):
    case Clockwise        extends Direction(1)
    case CounterClockwise extends Direction(-1)

  // depends on ordinal index
  enum State:
    case Spawn, Clockwise, InvertedSpawn, CounterClockwise

  extension (state: State)
    def rotateClockwise: State =
      rotate(direction = Direction.Clockwise)
    def rotateCounterClockwise: State =
      rotate(direction = Direction.CounterClockwise)
    private def rotate(direction: Direction): State =
      State.fromOrdinal((state.ordinal + direction.index) % 4)
// (x % m + m) % m
end Rotation

type Positions = NonEmptyBatch[Point]

sealed trait TetrominoPiece:
  def positions: Positions
  def rotationState: Rotation.State

enum Tetromino extends TetrominoPiece:
  case I(positions: Positions, rotationState: Rotation.State)
  case J(positions: Positions, rotationState: Rotation.State)
  case L(positions: Positions, rotationState: Rotation.State)
  case O(positions: Positions, rotationState: Rotation.State)
  case S(positions: Positions, rotationState: Rotation.State)
  case T(positions: Positions, rotationState: Rotation.State)
  case Z(positions: Positions, rotationState: Rotation.State)
import Tetromino.*

type Intersects = NonEmptyBatch[Point] => Boolean
type RotateFn   = Intersects => Option[Tetromino]

extension (t: Tetromino)
  def moveBy(point: Point): Tetromino =
    withPositions(t.positions.map(_.moveBy(point)))
  def rotationCenter: Point =
    t.positions.head
  def rotate(direction: Rotation.Direction): RotateFn =
    direction match
      case Rotation.Direction.Clockwise =>
        rotateWith(
          Matrix2((0, 1), (-1, 0)),
          t.rotationState.rotateClockwise
        )
      case Rotation.Direction.CounterClockwise =>
        rotateWith(
          Matrix2((0, -1), (1, 0)),
          t.rotationState.rotateCounterClockwise
        )

  private def rotateWith(
      rotationMatrix: Matrix2,
      nextRotationState: Rotation.State
  )(intersects: Intersects): Option[Tetromino] =
    val rotatedPositions = t.positions.map { pos =>
      val relativePos = pos - rotationCenter
      val rotated     = (rotationMatrix * relativePos.toVector).toPoint
      val absolutePos = rotated + relativePos

      absolutePos
    }

    @annotation.tailrec
    def findMatchingRotation(
        offsets: Batch[Point]
    ): Option[NonEmptyBatch[Point]] =
      offsets match
        case offset :: xs =>
          val withOffsets = rotatedPositions.map(_ - offset)
          if intersects(withOffsets) then findMatchingRotation(xs)
          else Some(withOffsets)
        case _ => None

    findMatchingRotation(offsets(nextRotationState)).map(withPositions)

  def withPositions(pos: Positions): Tetromino =
    t match
      case t: I => t.copy(positions = pos)
      case t: J => t.copy(positions = pos)
      case t: L => t.copy(positions = pos)
      case t: O => t.copy(positions = pos)
      case t: S => t.copy(positions = pos)
      case t: T => t.copy(positions = pos)
      case t: Z => t.copy(positions = pos)

  private def offsets(state: Rotation.State): Batch[Point] =
    t match
      case _: J | _: L | _: S | _: T | _: Z => Offsets.jlstz(state)
      case _: I                             => Offsets.i(state)
      case _: O                             => Offsets.o(state)

object Offsets:
  import Rotation.State.*

  type Offset

  val jlstz = Map(
    Spawn            -> Batch((0, 0), (0, 0), (0, 0), (0, 0), (0, 0)),
    Clockwise        -> Batch((0, 0), (1, 0), (1, -1), (0, 2), (1, 2)),
    InvertedSpawn    -> Batch((0, 0), (0, 0), (0, 0), (0, 0), (0, 0)),
    CounterClockwise -> Batch((0, 0), (-1, 0), (-1, -1), (0, 2), (-1, 2))
  ).toPoints

  val i = Map(
    Spawn            -> Batch((0, 0), (-1, 0), (2, 0), (-1, 0), (2, 0)),
    Clockwise        -> Batch((-1, 0), (0, 0), (0, 0), (0, 1), (0, -2)),
    InvertedSpawn    -> Batch((-1, 1), (1, 1), (-2, 1), (1, 0), (-2, 0)),
    CounterClockwise -> Batch((0, 1), (0, 1), (0, 1), (0, -1), (0, 2))
  ).toPoints

  val o = Map(
    Spawn            -> Batch((0, 0)),
    Clockwise        -> Batch((0, -1)),
    InvertedSpawn    -> Batch((-1, -1)),
    CounterClockwise -> Batch((-1, 0))
  ).toPoints

  extension (offsets: Map[Rotation.State, Batch[(Int, Int)]])
    def toPoints: Map[Rotation.State, Batch[Point]] =
      offsets.view.mapValues(_.map(Point.tuple2ToPoint(_))).toMap
end Offsets

object Tetromino:
  type DiceValue = Int

  def spawn(center: Point, side: DiceValue): Tetromino =
    val rotation = Rotation.State.Spawn

    def fromCenter(pos: (Int, Int)*) =
      NonEmptyBatch(
        center,
        pos.map(p => center.moveBy(Point.tuple2ToPoint(p))): _*
      )

    side match
      case 0 => I(fromCenter((-1, 0), (1, 0), (2, 0)), rotation)
      case 1 => J(fromCenter((-1, 1), (-1, 0), (1, 0)), rotation)
      case 2 => L(fromCenter((-1, 0), (1, 0), (1, 1)), rotation)
      case 3 => O(fromCenter((0, 1), (1, 0), (1, 1)), rotation)
      case 4 => S(fromCenter((-1, 0), (0, 1), (1, 1)), rotation)
      case 5 => T(fromCenter((1, 0), (0, 1), (1, 1)), rotation)
      case 6 => Z(fromCenter((-1, 1), (0, 1), (1, 0)), rotation)

final case class Matrix2(private val mat: Array[Double]) derives CanEqual:
  def *(v: Vector2): Vector2 =
    val (a, b, c, d) = (mat(0), mat(1), mat(2), mat(3))

    Vector2(
      a * v.x + b * v.y,
      c * v.x + d * v.y
    )

object Matrix2:
  def apply(
      row0: (Double, Double),
      row1: (Double, Double)
  ): Matrix2 = Matrix2(
    Array(row0._1, row0._2, row1._1, row1._2)
  )

// https://www.youtube.com/watch?v=yIpk5TJ_uaI&t=1091s
// tetromino rotation
// 1. calc new rotation index (?) - might not be needed
// 2. relativePos = originPos - pos of center tile
// 3. relativePos * rotationMatrix = newRelativePos (dot product) [for each pos]
// clockwise
//  (0, 1)
//  (-1, 0)
// counterclockwise
// (0, -1)
// (1, 0)
// 5. convert to global coords
// newPos = newRelativePos + pos of center tile
// 4. apply offsets
// prev offset - offset
//
// - get first passing
// - fail if it intersects with sth
