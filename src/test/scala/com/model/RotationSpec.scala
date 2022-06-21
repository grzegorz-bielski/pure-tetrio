package com.model

import indigo.shared.collections.NonEmptyBatch
import indigo.shared.collections.NonEmptyBatch.apply
import indigo.shared.datatypes.Point
import munit.FunSuite

class SRSSpec extends FunSuite:
  val center = Point(9, 1)

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
      NonEmptyBatch(
        Point(9, 1),
        Point(10, 1),
        Point(9, 0),
        Point(10, 0)
      ) // 1 up
    )
  }

// test("rotates the tetromino clockwise without intersections") {
//   val center = Point(9, 1)

//   val result = Rotation.rotate(
//    Tetromino.o(center),
//    Rotation.Direction.Clockwise
//   )(const(false))

//   assertEquals(
//     result.map(_.positions),
//     Some(
//       Tetromino.o(center).positions
//     )
//   )
// }
