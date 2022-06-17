package com.model

import cats.syntax.apply
import indigo.*
import indigo.shared.collections.Batch

// https://tetris.fandom.com/wiki/SRS
// https://harddrop.com/wiki/SRS

trait Block:
  def positions: Batch[Point]

enum Tetramino extends Block:
  case I(positions: Batch[Point])
  case J(positions: Batch[Point])
  case L(positions: Batch[Point])
  case O(positions: Batch[Point])
  case S(positions: Batch[Point])
  case T(positions: Batch[Point])
  case Z(positions: Batch[Point])
import Tetramino.*

extension (tetramino: Tetramino)
  def moveBy(point: Point): Tetramino = 
    def move(t: Tetramino) = t.positions.map(_.moveBy(point))

    tetramino match 
        case t: I => t.copy(positions = move(t))
        case t: J => t.copy(positions = move(t))
        case t: L => t.copy(positions = move(t))
        case t: O => t.copy(positions = move(t))
        case t: S => t.copy(positions = move(t))
        case t: T => t.copy(positions = move(t))
        case t: Z => t.copy(positions = move(t))

object Tetramino:
  type DiceValue = Int

  def spawn(center: Point, side: DiceValue): Tetramino =
    side match
      case 0 =>
        I(
          Batch(
            center.moveBy(-1, 0),
            center,
            center.moveBy(1, 0),
            center.moveBy(2, 0)
          )
        )
      case 1 =>
        J(
          Batch(
            center.moveBy(-1, 1),
            center.moveBy(-1, 0),
            center,
            center.moveBy(1, 0)
          )
        )
      case 2 =>
        L(
          Batch(
            center.moveBy(-1, 0),
            center,
            center.moveBy(1, 0),
            center.moveBy(1, 1)
          )
        )
      case 3 =>
        O(
          Batch(
            center.moveBy(0, 1),
            center,
            center.moveBy(1, 0),
            center.moveBy(1, 1)
          )
        )
      case 4 =>
        S(
          Batch(
            center.moveBy(-1, 0),
            center,
            center.moveBy(0, 1),
            center.moveBy(1, 1)
          )
        )
      case 5 =>
        T(
          Batch(
            center.moveBy(1, 0),
            center,
            center.moveBy(0, 1),
            center.moveBy(1, 1)
          )
        )
      case 6 =>
        Z(
          Batch(
            center.moveBy(-1, 1),
            center,
            center.moveBy(0, 1),
            center.moveBy(1, 0)
          )
        )
