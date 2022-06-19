package com.model

import com.model.*
import indigo.*
import indigo.shared.collections.Batch
import indigo.shared.collections.Batch.Unapply._

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

/**
 * SRS rotation system
 * https://harddrop.com/wiki/SRS
*/
object Rotation:
  def rotate(tetromino: Tetromino, direction: Rotation.Direction): RotateFn =
    val positionsFn =
      direction match
        case Rotation.Direction.Clockwise =>
          applyOffsets(
            baseRotation(tetromino, Matrix2((0, 1), (-1, 0))),
            offsets(tetromino, tetromino.rotationState.rotateClockwise)
          )
        case Rotation.Direction.CounterClockwise =>
          applyOffsets(
            baseRotation(tetromino, Matrix2((0, -1), (1, 0))),
            offsets(
              tetromino,
              tetromino.rotationState.rotateCounterClockwise
            )
          )

    positionsFn andThen (_.map(tetromino.withPositions))

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
        // (x % m + m) % m (???)

  private def baseRotation(
      tetromino: Tetromino,
      rotationMatrix: Matrix2
  ) = tetromino.positions.map { pos =>
    val relativePos = pos - tetromino.rotationCenter
    val rotated     = (rotationMatrix * relativePos.toVector).toPoint
    val absolutePos = rotated + relativePos

    absolutePos
  }

  private def applyOffsets(
      rotatedPositions: NonEmptyBatch[Point],
      offsets: Batch[Point]
  )(intersects: Intersects) =

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

    findMatchingRotation(offsets)

  private def offsets(
      tetromino: Tetromino,
      state: Rotation.State
  ): Batch[Point] =
    import Tetromino.*

    tetromino match
      case _: J | _: L | _: S | _: T | _: Z => Offsets.jlstz(state)
      case _: I                             => Offsets.i(state)
      case _: O                             => Offsets.o(state)

  type Intersects = NonEmptyBatch[Point] => Boolean
  type RotateFn   = Intersects => Option[Tetromino]

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
