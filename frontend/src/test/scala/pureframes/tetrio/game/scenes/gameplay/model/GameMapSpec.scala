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
import pureframes.tetrio.game.core.BootData

class GameMapSpec extends FunSuite:
  test("fullLinesWith - empty map") {
    assert(
      standardMap.fullLinesWith(Tetromino.i(Vector2(9, 1))).isEmpty
    )
  }

  test("fullLinesWith - map with debris") {
    val tetromino =
      Tetromino.I(
        NonEmptyBatch(
          Vector2(10, 25),
          Vector2(10, 23),
          Vector2(10, 24),
          Vector2(10, 22)
        ),
        RotationState.Clockwise
      )

    pprint.pprintln(
      standardMap.walls.toJSArray.minBy(_.point.x) ->  standardMap.walls.toJSArray.maxBy(_.point.x)
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

  def standardMap = GameMap.walled(BoundingBox(
    x = 0,
    y = 2,
    width = 11,
    height = 25
  ))
