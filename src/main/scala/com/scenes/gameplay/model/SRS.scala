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
  ): Tetromino.Positions = tetromino.positions.map { pos =>
    val relativePos = pos - tetromino.rotationCenter
    val rotatedPos  = rotationMatrix * relativePos
    val absolutePos = rotatedPos + tetromino.rotationCenter

    absolutePos
  }

  def applyOffsets(
      offsets: Batch[(Vector2, Vector2)],
      rotatedPositions: NonEmptyBatch[Vector2]
  )(intersects: Intersects) =

    @annotation.tailrec
    def findMatchingOffset(
        offsets: Batch[(Vector2, Vector2)]
    ): Option[NonEmptyBatch[Vector2]] =
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
  ): Batch[(Vector2, Vector2)] =
    import Tetromino.*

    tetromino match
      case _: J | _: L | _: S | _: T | _: Z =>
        Offsets.jlstz(states._1) zip Offsets.jlstz(states._2)
      case _: I => Offsets.i(states._1) zip Offsets.i(states._2)
      case _: O => Offsets.o(states._1) zip Offsets.o(states._2)

  type Intersects = NonEmptyBatch[Vector2] => Boolean
  type RotateFn   = Intersects => Option[Tetromino]

  object Offsets:
    import RotationState.*

    val jlstz = Map(
      Spawn            -> Batch((0, 0), (0, 0), (0, 0), (0, 0), (0, 0)),
      Clockwise        -> Batch((0, 0), (1, 0), (1, -1), (0, 2), (1, 2)),
      InvertedSpawn    -> Batch((0, 0), (0, 0), (0, 0), (0, 0), (0, 0)),
      CounterClockwise -> Batch((0, 0), (-1, 0), (-1, -1), (0, 2), (-1, 2))
    ).toVectors

    val i = Map(
      Spawn            -> Batch((0, 0), (-1, 0), (2, 0), (-1, 0), (2, 0)),
      Clockwise        -> Batch((-1, 0), (0, 0), (0, 0), (0, 1), (0, -2)),
      InvertedSpawn    -> Batch((-1, 1), (1, 1), (-2, 1), (1, 0), (-2, 0)),
      CounterClockwise -> Batch((0, 1), (0, 1), (0, 1), (0, -1), (0, 2))
    ).toVectors

    val o = Map(
      Spawn            -> Batch((0, 0)),
      Clockwise        -> Batch((0, 1)),
      InvertedSpawn    -> Batch((-1, 1)),
      CounterClockwise -> Batch((-1, 0))
    ).toVectors

    extension (offsets: Map[RotationState, Batch[(Int, Int)]])
      def toVectors: Map[RotationState, Batch[Vector2]] =
        offsets.view.mapValues(_.map(p => Vector2(p._1, p._2))).toMap
