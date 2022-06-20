package com.model

import munit.FunSuite

class RotationSpec extends FunSuite:
  test("rotates the state correctly clockwise") {
    Vector(
      Rotation.State.Clockwise,
      Rotation.State.InvertedSpawn,
      Rotation.State.CounterClockwise,
      Rotation.State.Spawn
    ).foldLeft(Rotation.State.Spawn) { (state, expected) =>
      val next = state.rotateClockwise
      assertEquals(next, expected)
      next
    }
  }

  test("rotates the state correctly counter clockwise") {
    Vector(
      Rotation.State.CounterClockwise,
      Rotation.State.InvertedSpawn,
      Rotation.State.Clockwise,
      Rotation.State.Spawn
    ).foldLeft(Rotation.State.Spawn) { (state, expected) =>
      val next = state.rotateCounterClockwise
      assertEquals(next, expected)
      next
    }
  }

  // test("rotates the tetromino clockwise") {
  //   Rotation.rotate(
  //   //  Tetromino.i()
  //   )
  // }
