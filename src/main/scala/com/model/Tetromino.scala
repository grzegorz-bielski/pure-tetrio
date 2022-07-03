package com.model

import cats.syntax.apply
import com.model.*
import indigo.*
import indigo.shared.collections.Batch

type Positions = NonEmptyBatch[Point]

sealed trait TetrominoPiece:
  def positions: Positions
  def rotationState: RotationState

enum Tetromino(val color: RGBA) extends TetrominoPiece:
  case I(positions: Positions, rotationState: RotationState)
      extends Tetromino(RGBA.Cyan)
  case J(positions: Positions, rotationState: RotationState)
      extends Tetromino(RGBA.Blue)
  case L(positions: Positions, rotationState: RotationState)
      extends Tetromino(RGBA.Orange)
  case O(positions: Positions, rotationState: RotationState)
      extends Tetromino(RGBA.Yellow)
  case S(positions: Positions, rotationState: RotationState)
      extends Tetromino(RGBA.Green)
  case T(positions: Positions, rotationState: RotationState)
      extends Tetromino(RGBA.Indigo)
  case Z(positions: Positions, rotationState: RotationState)
      extends Tetromino(RGBA.Red)
import Tetromino.*

type Intersects = Positions => Boolean
type RotateFn   = Intersects => Option[Tetromino]

extension (t: Tetromino)
  def moveBy(point: Point): Tetromino =
    withPositions(t.positions.map(_.moveBy(point)))
  def rotationCenter: Point =
    t.positions.head
  def rotate(direction: RotationDirection): RotateFn =
    SRS.rotate(t, direction)
  def highestPoint: Point = 
    t.positions.toBatch.toJSArray.minBy(_.y)
  def lowestPoint: Point =
    t.positions.toBatch.toJSArray.maxBy(_.y)

  def withRotationState(state: RotationState): Tetromino =
    t match
      case t: I => t.copy(rotationState = state)
      case t: J => t.copy(rotationState = state)
      case t: L => t.copy(rotationState = state)
      case t: O => t.copy(rotationState = state)
      case t: S => t.copy(rotationState = state)
      case t: T => t.copy(rotationState = state)
      case t: Z => t.copy(rotationState = state)

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

  val i = at(List((-1, 0), (1, 0), (2, 0))) andThen (I(_, rotation))
  val j = at(List((-1, 1), (-1, 0), (1, 0))) andThen (J(_, rotation))
  val l = at(List((-1, 0), (1, 0), (1, 1))) andThen (L(_, rotation))
  val o = at(List((0, 1), (1, 0), (1, 1))) andThen (O(_, rotation))
  val s = at(List((-1, 0), (0, 1), (1, 1))) andThen (S(_, rotation))
  val t = at(List((-1, 0), (0, 1), (1, 0))) andThen (T(_, rotation))
  val z = at(List((-1, 1), (0, 1), (1, 0))) andThen (Z(_, rotation))

  // todo: unsafe, try using bounded int
  def spawn(side: DiceValue): Point => Tetromino =
    side match
      case 0 => i
      case 1 => j
      case 2 => l
      case 3 => o
      case 4 => s
      case 5 => t
      case 6 => z

  private val rotation = RotationState.Spawn
  private def at(pos: List[(Int, Int)])(center: Point) =
    NonEmptyBatch(
      center,
      pos.map(p => center.moveBy(Point.tuple2ToPoint(p))): _*
    )
