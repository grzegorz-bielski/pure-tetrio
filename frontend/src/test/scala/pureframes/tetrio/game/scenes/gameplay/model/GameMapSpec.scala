package pureframes.tetrio
package game.scenes.gameplay.model

import indigo.shared.collections.Batch
import indigo.shared.collections.NonEmptyBatch
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.RGBA
import indigo.shared.datatypes.Vector2
import indigoextras.geometry.BoundingBox
import indigoextras.geometry.BoundingBox.apply
import indigoextras.geometry.Vertex
import munit.FunSuite
import pureframes.tetrio.game.core.*

import MapElement._

class GameMapSpec extends FunSuite:
  test("fullLinesWith - empty map") {
    assert(
      standardMap.fullLinesWith(Tetromino.i(Vector2(9, 1))).isEmpty
    )
  }

  test("fullLinesWith - map with debris") {
    val tetromino =
      Tetromino.I(
        // format: off
        NonEmptyBatch(Vector2(10, 25), Vector2(10, 23), Vector2(10, 24), Vector2(10, 22)),
        // format: on
        RotationState.Clockwise
      )

    val lines = standardMap
      .insertDebris(
        // TODO: better way of describing maps?
        // 2 lines from left wall to right wall with a hole at the end
        Batch(
          Vector2(9, 25),
          Vector2(9, 24),
          Vector2(8, 25),
          Vector2(8, 24),
          Vector2(7, 25),
          Vector2(7, 24),
          Vector2(6, 25),
          Vector2(6, 24),
          Vector2(5, 25),
          Vector2(5, 24),
          Vector2(4, 25),
          Vector2(4, 24),
          Vector2(3, 25),
          Vector2(3, 24),
          Vector2(2, 25),
          Vector2(2, 24),
          Vector2(1, 25),
          Vector2(1, 24)
        ),
        0
      )
      .insertTetromino(tetromino)
      .fullLinesWith(tetromino)

    assertEquals(
      lines,
      Batch(24, 25)
    )
  }

  test("fromGamePlan") {
    val gameMap = GameMap.fromGamePlan(
      """|..........
         |..........
         |..........
         |..........
         |..........
         |..........
         |..........
         |..........
         |..........
         |..........
         |..........
         |..........
         |..........
         |..........
         |..........
         |.........I
         |DDDDDDD..I
         |DDDDDDDD.I
         |DDDDDDDDDI
      """
    )

    assertEquals(
      gameMap.elements,
      // format: off
      Batch(Wall(Vector2(11,19)), Wall(Vector2(11,18)), Debris(Vector2(10,19),Some(0)), Debris(Vector2(10,18),Some(0)), Debris(Vector2(7,19),None), Debris(Vector2(7,18),None), Debris(Vector2(6,19),None), Debris(Vector2(6,18),None), Wall(Vector2(11,15)), Wall(Vector2(11,14)), Wall(Vector2(11,12)), Wall(Vector2(11,11)), Debris(Vector2(5,19),None), Debris(Vector2(5,18),None), Debris(Vector2(4,19),None), Debris(Vector2(4,18),None), Debris(Vector2(3,19),None), Debris(Vector2(3,18),None), Debris(Vector2(2,19),None), Debris(Vector2(2,18),None), Debris(Vector2(1,19),None), Debris(Vector2(1,18),None), Wall(Vector2(0,19)), Wall(Vector2(0,18)), Wall(Vector2(0,15)), Wall(Vector2(0,14)), Wall(Vector2(0,12)), Wall(Vector2(0,11)), Wall(Vector2(11,8)), Wall(Vector2(11,7)), Wall(Vector2(11,4)), Wall(Vector2(11,3)), Wall(Vector2(0,8)), Wall(Vector2(0,7)), Wall(Vector2(0,4)), Wall(Vector2(0,3)), Floor(Vector2(11,21)), Wall(Vector2(11,20)), Floor(Vector2(10,21)), Floor(Vector2(9,21)), Debris(Vector2(10,20),Some(0)), Debris(Vector2(9,20),None), Wall(Vector2(11,17)), Debris(Vector2(10,17),Some(0)), Floor(Vector2(8,21)), Debris(Vector2(8,20),None), Floor(Vector2(7,21)), Floor(Vector2(6,21)), Debris(Vector2(7,20),None), Debris(Vector2(6,20),None), Wall(Vector2(11,16)), Wall(Vector2(11,13)), Floor(Vector2(5,21)), Debris(Vector2(5,20),None), Floor(Vector2(4,21)), Floor(Vector2(3,21)), Debris(Vector2(4,20),None), Debris(Vector2(3,20),None), Floor(Vector2(2,21)), Debris(Vector2(2,20),None), Floor(Vector2(1,21)), Wall(Vector2(0,21)), Debris(Vector2(1,20),None), Wall(Vector2(0,20)), Wall(Vector2(0,17)), Wall(Vector2(0,16)), Wall(Vector2(0,13)), Wall(Vector2(11,10)), Wall(Vector2(11,9)), Wall(Vector2(11,6)), Wall(Vector2(11,5)), Wall(Vector2(0,10)), Wall(Vector2(0,9)), Wall(Vector2(0,6)), Wall(Vector2(0,5)), Debris(Vector2(8,19),None), Wall(Vector2(11,2)), Wall(Vector2(0,2)))
       // format: on
      )
  }

  test("removeFullLines - separated lines") {
    val withRemovedLines = 
      GameMap
        .fromElements(
          // format: off
          Batch(Floor(Vector2(11,27)), Wall(Vector2(11,26)), Floor(Vector2(10,27)), Debris(Vector2(10,26),Some(0)), Wall(Vector2(11,25)), Wall(Vector2(11,24)), Debris(Vector2(10,25),Some(0)), Debris(Vector2(10,24),Some(3)), Floor(Vector2(9,27)), Debris(Vector2(9,26),Some(5)), Debris(Vector2(9,25),Some(5)), Debris(Vector2(9,24),Some(3)), Wall(Vector2(11,22)), Wall(Vector2(11,21)), Debris(Vector2(9,22),Some(2)), Debris(Vector2(9,21),Some(2)), Floor(Vector2(8,27)), Debris(Vector2(8,26),Some(5)), Debris(Vector2(8,25),Some(1)), Debris(Vector2(8,24),Some(1)), Floor(Vector2(6,27)), Debris(Vector2(6,26),Some(3)), Debris(Vector2(6,25),Some(1)), Debris(Vector2(6,24),Some(1)), Debris(Vector2(8,22),Some(4)), Debris(Vector2(8,21),Some(4)), Debris(Vector2(7,22),Some(3)), Debris(Vector2(7,21),Some(4)), Debris(Vector2(6,22),Some(3)), Debris(Vector2(6,21),Some(4)), Wall(Vector2(11,19)), Wall(Vector2(11,18)), Debris(Vector2(9,19),Some(4)), Debris(Vector2(9,18),Some(0)), Wall(Vector2(11,16)), Wall(Vector2(11,15)), Debris(Vector2(10,16),Some(2)), Debris(Vector2(10,15),Some(2)), Debris(Vector2(9,16),Some(5)), Debris(Vector2(9,15),Some(2)), Debris(Vector2(8,19),Some(0)), Debris(Vector2(8,18),Some(0)), Debris(Vector2(7,19),Some(5)), Debris(Vector2(7,18),Some(0)), Debris(Vector2(8,16),Some(5)), Debris(Vector2(8,15),Some(5)), Debris(Vector2(7,16),Some(5)), Debris(Vector2(7,15),Some(3)), Debris(Vector2(6,16),Some(3)), Debris(Vector2(6,15),Some(3)), Floor(Vector2(5,27)), Debris(Vector2(5,26),Some(3)), Floor(Vector2(4,27)), Debris(Vector2(4,26),Some(6)), Debris(Vector2(5,22),Some(4)), Debris(Vector2(5,21),Some(4)), Debris(Vector2(3,22),Some(1)), Debris(Vector2(3,21),Some(4)), Floor(Vector2(2,27)), Debris(Vector2(2,26),Some(3)), Debris(Vector2(2,25),Some(3)), Debris(Vector2(2,24),Some(1)), Floor(Vector2(1,27)), Debris(Vector2(1,26),Some(3)), Wall(Vector2(0,27)), Wall(Vector2(0,26)), Debris(Vector2(1,25),Some(3)), Debris(Vector2(1,24),Some(1)), Wall(Vector2(0,25)), Wall(Vector2(0,24)), Debris(Vector2(2,22),Some(1)), Debris(Vector2(2,21),Some(4)), Wall(Vector2(0,22)), Wall(Vector2(0,21)), Debris(Vector2(5,19),Some(0)), Debris(Vector2(5,18),Some(3)), Debris(Vector2(4,19),Some(2)), Debris(Vector2(4,18),Some(0)), Debris(Vector2(3,19),Some(4)), Debris(Vector2(3,18),Some(3)), Debris(Vector2(5,16),Some(3)), Debris(Vector2(5,15),Some(1)), Debris(Vector2(4,16),Some(0)), Debris(Vector2(4,15),Some(1)), Debris(Vector2(2,19),Some(4)), Debris(Vector2(2,18),Some(3)), Debris(Vector2(1,19),Some(0)), Debris(Vector2(1,18),Some(0)), Wall(Vector2(0,19)), Wall(Vector2(0,18)), Debris(Vector2(2,16),Some(5)), Debris(Vector2(2,15),Some(5)), Debris(Vector2(1,16),Some(0)), Debris(Vector2(1,15),Some(5)), Wall(Vector2(0,16)), Wall(Vector2(0,15)), Wall(Vector2(11,14)), Wall(Vector2(11,13)), Wall(Vector2(11,11)), Wall(Vector2(11,10)), Wall(Vector2(11,8)), Wall(Vector2(11,7)), Wall(Vector2(11,5)), Wall(Vector2(11,4)), Wall(Vector2(11,3)), Wall(Vector2(11,2)), Wall(Vector2(0,14)), Wall(Vector2(0,13)), Wall(Vector2(0,11)), Wall(Vector2(0,10)), Wall(Vector2(0,8)), Wall(Vector2(0,7)), Wall(Vector2(0,5)), Wall(Vector2(0,4)), Wall(Vector2(0,3)), Wall(Vector2(0,2)), Wall(Vector2(11,23)), Debris(Vector2(10,23),Some(3)), Debris(Vector2(10,22),Some(2)), Debris(Vector2(9,23),Some(3)), Floor(Vector2(7,27)), Debris(Vector2(7,25),Some(1)), Debris(Vector2(8,23),Some(1)), Debris(Vector2(7,23),Some(3)), Debris(Vector2(6,23),Some(3)), Wall(Vector2(11,20)), Debris(Vector2(10,20),Some(4)), Debris(Vector2(10,19),Some(4)), Debris(Vector2(9,20),Some(2)), Wall(Vector2(11,17)), Debris(Vector2(10,17),Some(2)), Debris(Vector2(9,17),Some(0)), Debris(Vector2(8,20),Some(0)), Debris(Vector2(7,20),Some(4)), Debris(Vector2(8,17),Some(0)), Debris(Vector2(7,17),Some(0)), Debris(Vector2(6,17),Some(3)), Debris(Vector2(5,24),Some(1)), Floor(Vector2(3,27)), Debris(Vector2(4,24),Some(1)), Debris(Vector2(5,23),Some(1)), Debris(Vector2(4,23),Some(1)), Debris(Vector2(3,23),Some(1)), Debris(Vector2(4,22),Some(4)), Debris(Vector2(2,23),Some(1)), Wall(Vector2(0,23)), Debris(Vector2(5,20),Some(0)), Debris(Vector2(4,20),Some(4)), Debris(Vector2(3,20),Some(4)), Debris(Vector2(5,17),Some(3)), Debris(Vector2(4,17),Some(0)), Debris(Vector2(3,17),Some(3)), Debris(Vector2(3,15),Some(1)), Debris(Vector2(2,20),Some(4)), Debris(Vector2(1,20),Some(4)), Wall(Vector2(0,20)), Debris(Vector2(2,17),Some(3)), Debris(Vector2(1,17),Some(0)), Wall(Vector2(0,17)), Wall(Vector2(11,12)), Wall(Vector2(11,9)), Wall(Vector2(11,6)), Wall(Vector2(0,12)), Wall(Vector2(0,9)), Wall(Vector2(0,6)), Debris(Vector2(6,18),Some(3)), Debris(Vector2(7,14),Some(3)), Debris(Vector2(6,14),Some(3)), Debris(Vector2(2,14),Some(5)), Debris(Vector2(3,14),Some(1)))
          // format: on
          )
        .removeFullLines(Batch(15, 17))


    assertEquals(
      withRemovedLines.elements,
      // format: off
      Batch(Floor(Vector2(11,27)), Wall(Vector2(11,26)), Floor(Vector2(10,27)), Debris(Vector2(10,26),Some(0)), Wall(Vector2(11,25)), Wall(Vector2(11,24)), Debris(Vector2(10,25),Some(0)), Debris(Vector2(10,24),Some(3)), Floor(Vector2(9,27)), Debris(Vector2(9,26),Some(5)), Debris(Vector2(9,25),Some(5)), Debris(Vector2(9,24),Some(3)), Wall(Vector2(11,22)), Wall(Vector2(11,21)), Debris(Vector2(9,22),Some(2)), Debris(Vector2(9,21),Some(2)), Floor(Vector2(8,27)), Debris(Vector2(8,26),Some(5)), Debris(Vector2(8,25),Some(1)), Debris(Vector2(8,24),Some(1)), Floor(Vector2(6,27)), Debris(Vector2(6,26),Some(3)), Debris(Vector2(6,25),Some(1)), Debris(Vector2(6,24),Some(1)), Debris(Vector2(8,22),Some(4)), Debris(Vector2(8,21),Some(4)), Debris(Vector2(7,22),Some(3)), Debris(Vector2(7,21),Some(4)), Debris(Vector2(6,22),Some(3)), Debris(Vector2(6,21),Some(4)), Wall(Vector2(11,19)), Wall(Vector2(11,18)), Debris(Vector2(9,19),Some(4)), Debris(Vector2(9,18),Some(0)), Wall(Vector2(11,16)), Wall(Vector2(11,15)), Debris(Vector2(8,19),Some(0)), Debris(Vector2(8,18),Some(0)), Debris(Vector2(7,19),Some(5)), Debris(Vector2(7,18),Some(0)), Floor(Vector2(5,27)), Debris(Vector2(5,26),Some(3)), Floor(Vector2(4,27)), Debris(Vector2(4,26),Some(6)), Debris(Vector2(5,22),Some(4)), Debris(Vector2(5,21),Some(4)), Debris(Vector2(3,22),Some(1)), Debris(Vector2(3,21),Some(4)), Floor(Vector2(2,27)), Debris(Vector2(2,26),Some(3)), Debris(Vector2(2,25),Some(3)), Debris(Vector2(2,24),Some(1)), Floor(Vector2(1,27)), Debris(Vector2(1,26),Some(3)), Wall(Vector2(0,27)), Wall(Vector2(0,26)), Debris(Vector2(1,25),Some(3)), Debris(Vector2(1,24),Some(1)), Wall(Vector2(0,25)), Wall(Vector2(0,24)), Debris(Vector2(2,22),Some(1)), Debris(Vector2(2,21),Some(4)), Wall(Vector2(0,22)), Wall(Vector2(0,21)), Debris(Vector2(5,19),Some(0)), Debris(Vector2(5,18),Some(3)), Debris(Vector2(4,19),Some(2)), Debris(Vector2(4,18),Some(0)), Debris(Vector2(3,19),Some(4)), Debris(Vector2(3,18),Some(3)), Debris(Vector2(2,19),Some(4)), Debris(Vector2(2,18),Some(3)), Debris(Vector2(1,19),Some(0)), Debris(Vector2(1,18),Some(0)), Wall(Vector2(0,19)), Wall(Vector2(0,18)), Wall(Vector2(0,16)), Wall(Vector2(0,15)), Wall(Vector2(11,14)), Wall(Vector2(11,13)), Wall(Vector2(11,11)), Wall(Vector2(11,10)), Wall(Vector2(11,8)), Wall(Vector2(11,7)), Wall(Vector2(11,5)), Wall(Vector2(11,4)), Wall(Vector2(11,3)), Wall(Vector2(11,2)), Wall(Vector2(0,14)), Wall(Vector2(0,13)), Wall(Vector2(0,11)), Wall(Vector2(0,10)), Wall(Vector2(0,8)), Wall(Vector2(0,7)), Wall(Vector2(0,5)), Wall(Vector2(0,4)), Wall(Vector2(0,3)), Wall(Vector2(0,2)), Wall(Vector2(11,23)), Debris(Vector2(10,23),Some(3)), Debris(Vector2(10,22),Some(2)), Debris(Vector2(9,23),Some(3)), Floor(Vector2(7,27)), Debris(Vector2(7,25),Some(1)), Debris(Vector2(8,23),Some(1)), Debris(Vector2(7,23),Some(3)), Debris(Vector2(6,23),Some(3)), Wall(Vector2(11,20)), Debris(Vector2(10,20),Some(4)), Debris(Vector2(10,19),Some(4)), Debris(Vector2(9,20),Some(2)), Wall(Vector2(11,17)), Debris(Vector2(10,17),Some(2)), Debris(Vector2(8,20),Some(0)), Debris(Vector2(7,20),Some(4)), Debris(Vector2(8,17),Some(5)), Debris(Vector2(7,17),Some(5)), Debris(Vector2(7,16),Some(3)), Debris(Vector2(6,17),Some(3)), Debris(Vector2(6,16),Some(3)), Debris(Vector2(5,24),Some(1)), Floor(Vector2(3,27)), Debris(Vector2(4,24),Some(1)), Debris(Vector2(5,23),Some(1)), Debris(Vector2(4,23),Some(1)), Debris(Vector2(3,23),Some(1)), Debris(Vector2(4,22),Some(4)), Debris(Vector2(2,23),Some(1)), Wall(Vector2(0,23)), Debris(Vector2(5,20),Some(0)), Debris(Vector2(4,20),Some(4)), Debris(Vector2(3,20),Some(4)), Debris(Vector2(4,17),Some(0)), Debris(Vector2(3,16),Some(1)), Debris(Vector2(2,20),Some(4)), Debris(Vector2(1,20),Some(4)), Wall(Vector2(0,20)), Debris(Vector2(2,17),Some(5)), Debris(Vector2(2,16),Some(5)), Debris(Vector2(1,17),Some(0)), Wall(Vector2(0,17)), Wall(Vector2(11,12)), Wall(Vector2(11,9)), Wall(Vector2(11,6)), Wall(Vector2(0,12)), Wall(Vector2(0,9)), Wall(Vector2(0,6)), Debris(Vector2(9,17),Some(5)), Debris(Vector2(6,18),Some(3)), Debris(Vector2(5,17),Some(3)))
      // format: on
    )
  }

  // Batch(15, 17)
  // (prevMap,Batch(Floor(Vector2(11,27)), Wall(Vector2(11,26)), Floor(Vector2(10,27)), Debris(Vector2(10,26),Some(0)), Wall(Vector2(11,25)), Wall(Vector2(11,24)), Debris(Vector2(10,25),Some(0)), Debris(Vector2(10,24),Some(3)), Floor(Vector2(9,27)), Debris(Vector2(9,26),Some(5)), Debris(Vector2(9,25),Some(5)), Debris(Vector2(9,24),Some(3)), Wall(Vector2(11,22)), Wall(Vector2(11,21)), Debris(Vector2(9,22),Some(2)), Debris(Vector2(9,21),Some(2)), Floor(Vector2(8,27)), Debris(Vector2(8,26),Some(5)), Debris(Vector2(8,25),Some(1)), Debris(Vector2(8,24),Some(1)), Floor(Vector2(6,27)), Debris(Vector2(6,26),Some(3)), Debris(Vector2(6,25),Some(1)), Debris(Vector2(6,24),Some(1)), Debris(Vector2(8,22),Some(4)), Debris(Vector2(8,21),Some(4)), Debris(Vector2(7,22),Some(3)), Debris(Vector2(7,21),Some(4)), Debris(Vector2(6,22),Some(3)), Debris(Vector2(6,21),Some(4)), Wall(Vector2(11,19)), Wall(Vector2(11,18)), Debris(Vector2(9,19),Some(4)), Debris(Vector2(9,18),Some(0)), Wall(Vector2(11,16)), Wall(Vector2(11,15)), Debris(Vector2(10,16),Some(2)), Debris(Vector2(10,15),Some(2)), Debris(Vector2(9,16),Some(5)), Debris(Vector2(9,15),Some(2)), Debris(Vector2(8,19),Some(0)), Debris(Vector2(8,18),Some(0)), Debris(Vector2(7,19),Some(5)), Debris(Vector2(7,18),Some(0)), Debris(Vector2(8,16),Some(5)), Debris(Vector2(8,15),Some(5)), Debris(Vector2(7,16),Some(5)), Debris(Vector2(7,15),Some(3)), Debris(Vector2(6,16),Some(3)), Debris(Vector2(6,15),Some(3)), Floor(Vector2(5,27)), Debris(Vector2(5,26),Some(3)), Floor(Vector2(4,27)), Debris(Vector2(4,26),Some(6)), Debris(Vector2(5,22),Some(4)), Debris(Vector2(5,21),Some(4)), Debris(Vector2(3,22),Some(1)), Debris(Vector2(3,21),Some(4)), Floor(Vector2(2,27)), Debris(Vector2(2,26),Some(3)), Debris(Vector2(2,25),Some(3)), Debris(Vector2(2,24),Some(1)), Floor(Vector2(1,27)), Debris(Vector2(1,26),Some(3)), Wall(Vector2(0,27)), Wall(Vector2(0,26)), Debris(Vector2(1,25),Some(3)), Debris(Vector2(1,24),Some(1)), Wall(Vector2(0,25)), Wall(Vector2(0,24)), Debris(Vector2(2,22),Some(1)), Debris(Vector2(2,21),Some(4)), Wall(Vector2(0,22)), Wall(Vector2(0,21)), Debris(Vector2(5,19),Some(0)), Debris(Vector2(5,18),Some(3)), Debris(Vector2(4,19),Some(2)), Debris(Vector2(4,18),Some(0)), Debris(Vector2(3,19),Some(4)), Debris(Vector2(3,18),Some(3)), Debris(Vector2(5,16),Some(3)), Debris(Vector2(5,15),Some(1)), Debris(Vector2(4,16),Some(0)), Debris(Vector2(4,15),Some(1)), Debris(Vector2(2,19),Some(4)), Debris(Vector2(2,18),Some(3)), Debris(Vector2(1,19),Some(0)), Debris(Vector2(1,18),Some(0)), Wall(Vector2(0,19)), Wall(Vector2(0,18)), Debris(Vector2(2,16),Some(5)), Debris(Vector2(2,15),Some(5)), Debris(Vector2(1,16),Some(0)), Debris(Vector2(1,15),Some(5)), Wall(Vector2(0,16)), Wall(Vector2(0,15)), Wall(Vector2(11,14)), Wall(Vector2(11,13)), Wall(Vector2(11,11)), Wall(Vector2(11,10)), Wall(Vector2(11,8)), Wall(Vector2(11,7)), Wall(Vector2(11,5)), Wall(Vector2(11,4)), Wall(Vector2(11,3)), Wall(Vector2(11,2)), Wall(Vector2(0,14)), Wall(Vector2(0,13)), Wall(Vector2(0,11)), Wall(Vector2(0,10)), Wall(Vector2(0,8)), Wall(Vector2(0,7)), Wall(Vector2(0,5)), Wall(Vector2(0,4)), Wall(Vector2(0,3)), Wall(Vector2(0,2)), Wall(Vector2(11,23)), Debris(Vector2(10,23),Some(3)), Debris(Vector2(10,22),Some(2)), Debris(Vector2(9,23),Some(3)), Floor(Vector2(7,27)), Debris(Vector2(7,25),Some(1)), Debris(Vector2(8,23),Some(1)), Debris(Vector2(7,23),Some(3)), Debris(Vector2(6,23),Some(3)), Wall(Vector2(11,20)), Debris(Vector2(10,20),Some(4)), Debris(Vector2(10,19),Some(4)), Debris(Vector2(9,20),Some(2)), Wall(Vector2(11,17)), Debris(Vector2(10,17),Some(2)), Debris(Vector2(9,17),Some(0)), Debris(Vector2(8,20),Some(0)), Debris(Vector2(7,20),Some(4)), Debris(Vector2(8,17),Some(0)), Debris(Vector2(7,17),Some(0)), Debris(Vector2(6,17),Some(3)), Debris(Vector2(5,24),Some(1)), Floor(Vector2(3,27)), Debris(Vector2(4,24),Some(1)), Debris(Vector2(5,23),Some(1)), Debris(Vector2(4,23),Some(1)), Debris(Vector2(3,23),Some(1)), Debris(Vector2(4,22),Some(4)), Debris(Vector2(2,23),Some(1)), Wall(Vector2(0,23)), Debris(Vector2(5,20),Some(0)), Debris(Vector2(4,20),Some(4)), Debris(Vector2(3,20),Some(4)), Debris(Vector2(5,17),Some(3)), Debris(Vector2(4,17),Some(0)), Debris(Vector2(3,17),Some(3)), Debris(Vector2(3,15),Some(1)), Debris(Vector2(2,20),Some(4)), Debris(Vector2(1,20),Some(4)), Wall(Vector2(0,20)), Debris(Vector2(2,17),Some(3)), Debris(Vector2(1,17),Some(0)), Wall(Vector2(0,17)), Wall(Vector2(11,12)), Wall(Vector2(11,9)), Wall(Vector2(11,6)), Wall(Vector2(0,12)), Wall(Vector2(0,9)), Wall(Vector2(0,6)), Debris(Vector2(6,18),Some(3)), Debris(Vector2(7,14),Some(3)), Debris(Vector2(6,14),Some(3)), Debris(Vector2(2,14),Some(5)), Debris(Vector2(3,14),Some(1))))
  // (nextMap,Batch(Floor(Vector2(11,27)), Wall(Vector2(11,26)), Floor(Vector2(10,27)), Debris(Vector2(10,26),Some(0)), Wall(Vector2(11,25)), Wall(Vector2(11,24)), Debris(Vector2(10,25),Some(0)), Debris(Vector2(10,24),Some(3)), Floor(Vector2(9,27)), Debris(Vector2(9,26),Some(5)), Debris(Vector2(9,25),Some(5)), Debris(Vector2(9,24),Some(3)), Wall(Vector2(11,22)), Wall(Vector2(11,21)), Debris(Vector2(9,22),Some(2)), Debris(Vector2(9,21),Some(2)), Floor(Vector2(8,27)), Debris(Vector2(8,26),Some(5)), Debris(Vector2(8,25),Some(1)), Debris(Vector2(8,24),Some(1)), Floor(Vector2(6,27)), Debris(Vector2(6,26),Some(3)), Debris(Vector2(6,25),Some(1)), Debris(Vector2(6,24),Some(1)), Debris(Vector2(8,22),Some(4)), Debris(Vector2(8,21),Some(4)), Debris(Vector2(7,22),Some(3)), Debris(Vector2(7,21),Some(4)), Debris(Vector2(6,22),Some(3)), Debris(Vector2(6,21),Some(4)), Wall(Vector2(11,19)), Wall(Vector2(11,18)), Debris(Vector2(9,19),Some(4)), Debris(Vector2(9,18),Some(0)), Wall(Vector2(11,16)), Wall(Vector2(11,15)), Debris(Vector2(8,19),Some(0)), Debris(Vector2(8,18),Some(0)), Debris(Vector2(7,19),Some(5)), Debris(Vector2(7,18),Some(0)), Floor(Vector2(5,27)), Debris(Vector2(5,26),Some(3)), Floor(Vector2(4,27)), Debris(Vector2(4,26),Some(6)), Debris(Vector2(5,22),Some(4)), Debris(Vector2(5,21),Some(4)), Debris(Vector2(3,22),Some(1)), Debris(Vector2(3,21),Some(4)), Floor(Vector2(2,27)), Debris(Vector2(2,26),Some(3)), Debris(Vector2(2,25),Some(3)), Debris(Vector2(2,24),Some(1)), Floor(Vector2(1,27)), Debris(Vector2(1,26),Some(3)), Wall(Vector2(0,27)), Wall(Vector2(0,26)), Debris(Vector2(1,25),Some(3)), Debris(Vector2(1,24),Some(1)), Wall(Vector2(0,25)), Wall(Vector2(0,24)), Debris(Vector2(2,22),Some(1)), Debris(Vector2(2,21),Some(4)), Wall(Vector2(0,22)), Wall(Vector2(0,21)), Debris(Vector2(5,19),Some(0)), Debris(Vector2(5,18),Some(3)), Debris(Vector2(4,19),Some(2)), Debris(Vector2(4,18),Some(0)), Debris(Vector2(3,19),Some(4)), Debris(Vector2(3,18),Some(3)), Debris(Vector2(2,19),Some(4)), Debris(Vector2(2,18),Some(3)), Debris(Vector2(1,19),Some(0)), Debris(Vector2(1,18),Some(0)), Wall(Vector2(0,19)), Wall(Vector2(0,18)), Wall(Vector2(0,16)), Wall(Vector2(0,15)), Wall(Vector2(11,14)), Wall(Vector2(11,13)), Wall(Vector2(11,11)), Wall(Vector2(11,10)), Wall(Vector2(11,8)), Wall(Vector2(11,7)), Wall(Vector2(11,5)), Wall(Vector2(11,4)), Wall(Vector2(11,3)), Wall(Vector2(11,2)), Wall(Vector2(0,14)), Wall(Vector2(0,13)), Wall(Vector2(0,11)), Wall(Vector2(0,10)), Wall(Vector2(0,8)), Wall(Vector2(0,7)), Wall(Vector2(0,5)), Wall(Vector2(0,4)), Wall(Vector2(0,3)), Wall(Vector2(0,2)), Wall(Vector2(11,23)), Debris(Vector2(10,23),Some(3)), Debris(Vector2(10,22),Some(2)), Debris(Vector2(9,23),Some(3)), Floor(Vector2(7,27)), Debris(Vector2(7,25),Some(1)), Debris(Vector2(8,23),Some(1)), Debris(Vector2(7,23),Some(3)), Debris(Vector2(6,23),Some(3)), Wall(Vector2(11,20)), Debris(Vector2(10,20),Some(4)), Debris(Vector2(10,19),Some(4)), Debris(Vector2(9,20),Some(2)), Wall(Vector2(11,17)), Debris(Vector2(10,16),Some(2)), Debris(Vector2(8,20),Some(0)), Debris(Vector2(7,20),Some(4)), Debris(Vector2(8,16),Some(5)), Debris(Vector2(7,16),Some(3)), Debris(Vector2(5,24),Some(1)), Floor(Vector2(3,27)), Debris(Vector2(4,24),Some(1)), Debris(Vector2(5,23),Some(1)), Debris(Vector2(4,23),Some(1)), Debris(Vector2(3,23),Some(1)), Debris(Vector2(4,22),Some(4)), Debris(Vector2(2,23),Some(1)), Wall(Vector2(0,23)), Debris(Vector2(5,20),Some(0)), Debris(Vector2(4,20),Some(4)), Debris(Vector2(3,20),Some(4)), Debris(Vector2(4,16),Some(0)), Debris(Vector2(3,16),Some(1)), Debris(Vector2(2,20),Some(4)), Debris(Vector2(1,20),Some(4)), Wall(Vector2(0,20)), Wall(Vector2(0,17)), Debris(Vector2(1,16),Some(0)), Wall(Vector2(11,12)), Wall(Vector2(11,9)), Wall(Vector2(11,6)), Wall(Vector2(0,12)), Wall(Vector2(0,9)), Wall(Vector2(0,6)), Debris(Vector2(9,16),Some(5)), Debris(Vector2(6,18),Some(3)), Debris(Vector2(6,16),Some(3)), Debris(Vector2(5,16),Some(3)), Debris(Vector2(2,16),Some(5))))

  def standardMap = GameMap.walled(
    BoundingBox(
      x = 0,
      y = 2,
      width = 11,
      height = 25
    )
  )

