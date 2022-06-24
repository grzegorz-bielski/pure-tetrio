package com.model

import indigo.shared.collections.Batch
import indigo.shared.collections.NonEmptyBatch
import indigo.shared.datatypes.Point
import munit.FunSuite

class TetrominoSpec extends FunSuite:
  test("spawns tetrominos correctly") {
    val center = Point(9, 1)

    Vector(
      Tetromino.i -> NonEmptyBatch(
        center,
        center moveBy Point(-1, 0),
        center moveBy Point(1, 0),
        center moveBy Point(2, 0)
      ),
      Tetromino.j -> NonEmptyBatch(
        center,
        center moveBy Point(-1, 1),
        center moveBy Point(-1, 0),
        center moveBy Point(1, 0)
      ),
      Tetromino.l -> NonEmptyBatch(
        center,
        center moveBy Point(-1, 0),
        center moveBy Point(1, 0),
        center moveBy Point(1, 1)
      ),
      Tetromino.o -> NonEmptyBatch(
        // format: off
        Point(9,1),   Point(9,2),
        Point(10, 1), Point(10,2)
        // format: on
      ),
      Tetromino.s -> NonEmptyBatch(
        center,
        center moveBy Point(-1, 0),
        center moveBy Point(0, 1),
        center moveBy Point(1, 1)
      ),
      Tetromino.t -> NonEmptyBatch(
        center,
        center moveBy Point(-1, 0),
        center moveBy Point(0, 1),
        center moveBy Point(1, 0)
      ),
      Tetromino.z -> NonEmptyBatch(
        center,
        center moveBy Point(-1, 1),
        center moveBy Point(0, 1),
        center moveBy Point(1, 0)
      )
    ).foreach { (fn, expected) =>
      assertEquals(
        fn(center).positions,
        expected
      )
    }
  }
