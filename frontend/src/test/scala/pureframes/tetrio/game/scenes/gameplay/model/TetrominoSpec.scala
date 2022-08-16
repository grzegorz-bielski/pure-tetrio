package pureframes.tetrio
package game.scenes.gameplay.model

import indigo.shared.collections.Batch
import indigo.shared.collections.NonEmptyBatch
import indigo.shared.datatypes.Vector2
import munit.FunSuite

class TetrominoSpec extends FunSuite:
  test("spawns tetrominos correctly") {
    val center = Vector2(9, 1)

    Vector(
      Tetromino.i -> NonEmptyBatch(
        center,
        center moveBy Vector2(-1, 0),
        center moveBy Vector2(1, 0),
        center moveBy Vector2(2, 0)
      ),
      Tetromino.j -> NonEmptyBatch(
        center,
        center moveBy Vector2(-1, -1),
        center moveBy Vector2(-1, 0),
        center moveBy Vector2(1, 0)
      ),
      Tetromino.l -> NonEmptyBatch(
        center,
        center moveBy Vector2(-1, 0),
        center moveBy Vector2(1, 0),
        center moveBy Vector2(1, -1)
      ),
      Tetromino.o -> NonEmptyBatch(
        center,
        center moveBy Vector2(0, -1),
        center moveBy Vector2(1, 0),
        center moveBy Vector2(1, -1)
      ),
      Tetromino.s -> NonEmptyBatch(
        center,
        center moveBy Vector2(-1, 0),
        center moveBy Vector2(0, -1),
        center moveBy Vector2(1, -1)
      ),
      Tetromino.t -> NonEmptyBatch(
        center,
        center moveBy Vector2(-1, 0),
        center moveBy Vector2(0, -1),
        center moveBy Vector2(1, 0)
      ),
      Tetromino.z -> NonEmptyBatch(
        center,
        center moveBy Vector2(-1, -1),
        center moveBy Vector2(0, -1),
        center moveBy Vector2(1, 0)
      )
    ).foreach { (fn, expected) =>
      assertEquals(
        fn(center).positions,
        expected
      )
    }
  }
