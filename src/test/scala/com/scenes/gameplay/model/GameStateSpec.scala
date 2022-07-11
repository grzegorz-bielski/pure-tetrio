package com.scenes.gameplay.model

import com.core.*
import indigo.*
import indigoextras.geometry.BoundingBox
import indigoextras.geometry.Vertex
import munit.FunSuite

class GameplayModelSpec extends FunSuite:
  test("moveDown works correctly") {
    val testTable = Batch(
      // format: off
      gameInProgress(Tetromino.o(Point(9, 1))) -> Batch(Vertex(10, 22), Vertex(10, 21), Vertex(9, 22), Vertex(9, 21)),
      // format: on
    )

    testTable.foreach { (game, expected) =>
      assertEquals(
        game.moveDown.map(
          _.map.debris.map(_.point)
        ),
        Outcome(expected)
      )
    }
  }

  val standardMap = GameMap.walled(BootData.default.gridSize)

  def gameInProgress(t: Tetromino): GameplayModel.InProgress =
    GameplayModel.InProgress(
      map = standardMap,
      tetromino = Tetromino.o(Point(9, 1)),
      lastUpdated = Seconds(0.5),
      fallDelay = Seconds(1),
      score = 0
    )
