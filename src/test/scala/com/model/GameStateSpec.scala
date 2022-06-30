package com.model

import com.init.BootData
import indigo.*
import indigoextras.geometry.BoundingBox
import indigoextras.geometry.Vertex
import munit.FunSuite

// todo: test moveDown falling
class GameStateSpec extends FunSuite:
  test("moveDown works correctly") {
    val testTable = Batch(
      // format: off
      gameInProgress(Tetromino.o(Point(9, 1))) -> Batch(Vertex(10, 22), Vertex(10, 21), Vertex(9, 22), Vertex(9, 21)),
      
      // format: on
    )

    testTable.foreach { (game, expected) =>
      assertEquals(
        game.moveDown.map.debris.map(_.point),
        expected
      )
    }
  }

  val standardMap = GameMap.walled(BootData.default.gridSize)

  def gameInProgress(t: Tetromino): GameState.InProgress =
    GameState.InProgress(
      map = standardMap,
      tetromino = Tetromino.o(Point(9, 1)),
      lastUpdated = Seconds(0.5),
      fallDelay = Seconds(1)
    )
