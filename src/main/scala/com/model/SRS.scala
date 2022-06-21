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

/** SRS rotation system https://harddrop.com/wiki/SRS
  */
object SRS:
  def rotate(tetromino: Tetromino, direction: RotationDirection): RotateFn =
    val positionsFn =
      direction match
        case RotationDirection.Clockwise =>
          applyOffsets(
            clockwiseBaseRotation(tetromino),
            offsets(tetromino)
          )
        case RotationDirection.CounterClockwise =>
          applyOffsets(
            counterClockwiseBaseRotation(tetromino),
            offsets(
              tetromino
              // todo: new state should be merged back to tetromino (!??)
              // tetromino.rotationState.rotateCounterClockwise
            )
          )

    positionsFn andThen (_.map(pos =>
      tetromino
        .withPositions(pos)
        // .withRotationState(
        //   RotationState.rotate(tetromino.rotationState, direction)
        // )
    ))

  val clockwiseBaseRotation =
    baseRotation(Matrix2((0, 1), (-1, 0)))
  val counterClockwiseBaseRotation =
    baseRotation(Matrix2((0, -1), (1, 0)))

  private def baseRotation(rotationMatrix: Matrix2)(
      tetromino: Tetromino
  ) = tetromino.positions.map { pos =>
    val relativePos = pos - tetromino.rotationCenter
    val rotatedPos  = (rotationMatrix * relativePos.toVector).toPoint
    val absolutePos = rotatedPos + tetromino.rotationCenter

    absolutePos
  }

  def applyOffsets(
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

  def offsets(tetromino: Tetromino): Batch[Point] =
    import Tetromino.*
    val state = tetromino.rotationState

    tetromino match
      case _: J | _: L | _: S | _: T | _: Z => Offsets.jlstz(state)
      case _: I                             => Offsets.i(state)
      case _: O                             => Offsets.o(state)

  type Intersects = NonEmptyBatch[Point] => Boolean
  type RotateFn   = Intersects => Option[Tetromino]

  object Offsets:
    import RotationState.*

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

    extension (offsets: Map[RotationState, Batch[(Int, Int)]])
      def toPoints: Map[RotationState, Batch[Point]] =
        offsets.view.mapValues(_.map(Point.tuple2ToPoint(_))).toMap
