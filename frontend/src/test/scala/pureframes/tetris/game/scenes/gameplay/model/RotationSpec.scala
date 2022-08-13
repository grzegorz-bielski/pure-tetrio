package pureframes.tetris
package game.scenes.gameplay.model

import pureframes.tetris.game.core.*
import indigo.shared.collections.NonEmptyBatch
import indigo.shared.collections.NonEmptyBatch.apply
import indigo.shared.datatypes.Vector2
import munit.FunSuite

class SRSSpec extends FunSuite:
  val center = Vector2(9, 1)

  test("rotates the state correctly clockwise") {
    Vector(
      RotationState.Clockwise,
      RotationState.InvertedSpawn,
      RotationState.CounterClockwise,
      RotationState.Spawn
    ).foldLeft(RotationState.Spawn) { (state, expected) =>
      val next = RotationState.rotate(state, RotationDirection.Clockwise)
      assertEquals(next, expected)
      next
    }
  }

  test("rotates the state correctly counter clockwise") {
    Vector(
      RotationState.CounterClockwise,
      RotationState.InvertedSpawn,
      RotationState.Clockwise,
      RotationState.Spawn
    ).foldLeft(RotationState.Spawn) { (state, expected) =>
      val next = RotationState.rotate(state, RotationDirection.CounterClockwise)
      assertEquals(next, expected)
      next
    }
  }

  // todo: property testing ?

  test("applies base rotation") {
    val result = SRS.clockwiseBaseRotation(Tetromino.o(center))

    assertEquals(
      result,
      // format: off
      NonEmptyBatch(
        Vector2(9, 1), Vector2(10, 1),
        Vector2(9, 2), Vector2(10, 2)
      )
      // format: on
    )
  }

  test("rotates the tetromino clockwise with first offset") {
    val atSpawn = Tetromino.o(center)

    for {
      t <- atSpawn.rotateClockwise(
        NonEmptyBatch(
          Vector2(9, 0),
          Vector2(10, 0),
          Vector2(9, 1),
          Vector2(10, 1)
        )
      )
      t <- t.rotateClockwise(
        NonEmptyBatch(
          Vector2(10, 0),
          Vector2(10, 1),
          Vector2(9, 0),
          Vector2(9, 1)
        )
      )
      t <- t.rotateClockwise(
        NonEmptyBatch(
          Vector2(10, 1),
          Vector2(9, 1),
          Vector2(10, 0),
          Vector2(9, 0)
        )
      )
      t <- t.rotateClockwise(
        atSpawn.positions
      )
    } yield ()
  }

  extension (t: Tetromino)
    def rotateClockwise(expected: => NonEmptyBatch[Vector2]) =
      val next = t.rotate(RotationDirection.Clockwise)(const(false))
      assertEquals(next.map(_.positions), Some(expected))
      next
