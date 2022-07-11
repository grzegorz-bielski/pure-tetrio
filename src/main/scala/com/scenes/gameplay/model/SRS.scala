package com.scenes.gameplay.model

import com.core.*
import indigo.*
import indigo.shared.collections.Batch
import indigo.shared.collections.Batch.Unapply.*

/** SRS rotation system https://harddrop.com/wiki/SRS
  */
object SRS:
  def rotate(tetromino: Tetromino, direction: RotationDirection): RotateFn =
    val states = (
      tetromino.rotationState,
      RotationState.rotate(tetromino.rotationState, direction)
    )
    val (_, nextRotationState) = states
    val offsets                = tetrominoOffsets(tetromino, states)
    val rotatedPositions =
      direction match
        case RotationDirection.Clockwise =>
          clockwiseBaseRotation(tetromino)
        case RotationDirection.CounterClockwise =>
          counterClockwiseBaseRotation(tetromino)

    applyOffsets(offsets, rotatedPositions) andThen (_.map(
      tetromino
        .withPositions(_)
        .withRotationState(nextRotationState)
    ))

  val clockwiseBaseRotation =
    baseRotation(Matrix2((0, -1), (1, 0)))
  val counterClockwiseBaseRotation =
    baseRotation(Matrix2((0, 1), (-1, 0)))

  private def baseRotation(rotationMatrix: Matrix2)(
      tetromino: Tetromino
  ) = tetromino.positions.map { pos =>
    val relativePos = pos - tetromino.rotationCenter
    val rotatedPos  = (rotationMatrix * relativePos.toVector).toPoint
    val absolutePos = rotatedPos + tetromino.rotationCenter

    absolutePos
  }

  def applyOffsets(
      offsets: Batch[(Point, Point)],
      rotatedPositions: NonEmptyBatch[Point]
  )(intersects: Intersects) =

    @annotation.tailrec
    def findMatchingOffset(
        offsets: Batch[(Point, Point)]
    ): Option[NonEmptyBatch[Point]] =
      offsets match
        case (prevOffset, nextOffset) :: xs =>
          val endOffset   = prevOffset - nextOffset
          val withOffsets = rotatedPositions.map(_ + endOffset)
          if intersects(withOffsets) then findMatchingOffset(xs)
          else Some(withOffsets)
        case _ => None

    findMatchingOffset(offsets)

  def tetrominoOffsets(
      tetromino: Tetromino,
      states: (RotationState, RotationState)
  ): Batch[(Point, Point)] =
    import Tetromino.*

    tetromino match
      case _: J | _: L | _: S | _: T | _: Z =>
        Offsets.jlstz(states._1) zip Offsets.jlstz(states._2)
      case _: I => Offsets.i(states._1) zip Offsets.i(states._2)
      case _: O => Offsets.o(states._1) zip Offsets.o(states._2)

  type Intersects = NonEmptyBatch[Point] => Boolean
  type RotateFn   = Intersects => Option[Tetromino]

  object Offsets:
    import RotationState.*

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
      Clockwise        -> Batch((0, 1)),
      InvertedSpawn    -> Batch((-1, 1)),
      CounterClockwise -> Batch((-1, 0))
    ).toPoints

    extension (offsets: Map[RotationState, Batch[(Int, Int)]])
      def toPoints: Map[RotationState, Batch[Point]] =
        offsets.view.mapValues(_.map(Point.tuple2ToPoint(_))).toMap
