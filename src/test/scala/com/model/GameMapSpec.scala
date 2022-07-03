package com.model

import com.init.BootData
import indigo.shared.collections.Batch
import indigo.shared.collections.NonEmptyBatch
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.RGBA
import indigoextras.geometry.Vertex
import munit.FunSuite

class GameMapSpec extends FunSuite:
  test("fullLinesWith - empty map") {
    assert(
      standardMap.fullLinesWith(Tetromino.i(Point(9, 1))).isEmpty
    )
  }

  test("fullLinesWith - map with debris") {
    val tetromino = 
        Tetromino.I(
          NonEmptyBatch(
            Point(14, 22),
            Point(14, 20),
            Point(14, 21),
            Point(14, 19)
          ),
          RotationState.Clockwise
        )

    val lines = standardMap
      .insertDebris(
        Batch(
           Vertex(13, 22),
            Vertex(13, 20),
            Vertex(13, 21),
            Vertex(13, 19),
          Vertex(12, 22),
          Vertex(12, 21),
          Vertex(11, 22),
          Vertex(11, 21),
          Vertex(10, 22),
          Vertex(10, 21),
          Vertex(9, 22),
          Vertex(9, 21),
          Vertex(8, 22),
          Vertex(8, 21),
          Vertex(7, 22),
          Vertex(7, 21),
          Vertex(6, 22),
          Vertex(6, 21),
          Vertex(5, 22),
          Vertex(5, 21)
        ),
        RGBA.Tomato
      )
      .insertTetromino(tetromino)
      .fullLinesWith(tetromino)

    assertEquals(
      lines,
      Batch(21, 22)
    )
  }

  def standardMap = GameMap.walled(BootData.default.gridSize)
