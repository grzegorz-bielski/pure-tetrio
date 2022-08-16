package pureframes.tetrio
package game.scenes.gameplay.model

import indigo.shared.collections.Batch
import indigo.shared.collections.NonEmptyBatch
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.RGBA
import indigo.shared.datatypes.Vector2
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
          Vector2(14, 22),
          Vector2(14, 20),
          Vector2(14, 21),
          Vector2(14, 19)
        ),
        RotationState.Clockwise
      )

    val lines = standardMap
      .insertDebris(
        Batch(
          Vector2(13, 22),
          Vector2(13, 20),
          Vector2(13, 21),
          Vector2(13, 19),
          Vector2(12, 22),
          Vector2(12, 21),
          Vector2(11, 22),
          Vector2(11, 21),
          Vector2(10, 22),
          Vector2(10, 21),
          Vector2(9, 22),
          Vector2(9, 21),
          Vector2(8, 22),
          Vector2(8, 21),
          Vector2(7, 22),
          Vector2(7, 21),
          Vector2(6, 22),
          Vector2(6, 21),
          Vector2(5, 22),
          Vector2(5, 21)
        ),
        0
      )
      .insertTetromino(tetromino)
      .fullLinesWith(tetromino)

    assertEquals(
      lines,
      Batch(21, 22)
    )
  }

  def standardMap = GameMap.walled(BootData.default.gridSize)
