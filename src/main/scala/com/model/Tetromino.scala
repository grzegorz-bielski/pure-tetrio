package com.model

import cats.syntax.apply
import indigo.*
import indigo.shared.collections.Batch
import indigo.shared.collections.Batch.Unapply._


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
      Rotation.rotate(t, direction)
  def withPositions(pos: Positions): Tetromino =
    t match
      case t: I => t.copy(positions = pos)
      case t: J => t.copy(positions = pos)
      case t: L => t.copy(positions = pos)
      case t: O => t.copy(positions = pos)
      case t: S => t.copy(positions = pos)
      case t: T => t.copy(positions = pos)
      case t: Z => t.copy(positions = pos)

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
