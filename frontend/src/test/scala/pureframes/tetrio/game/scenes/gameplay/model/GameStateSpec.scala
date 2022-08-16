package pureframes.tetrio
package game.scenes.gameplay.model

import indigo.*
import indigo.platform.assets.*
import indigo.shared.*
import indigoextras.geometry.BoundingBox
import indigoextras.geometry.Vertex
import munit.FunSuite
import pureframes.tetrio.game.core.*

import GameplayModel.GameplayState

class GameplayModelSpec extends FunSuite:
  test("hardDrop works correctly") {
    val testTable = Batch(
      // format: off
      gameInProgress(Tetromino.o(Vector2(9, 1))) -> Batch(Vector2(10, 22), Vector2(10, 21), Vector2(9, 22), Vector2(9, 21)),
      // format: on
    )

    val testFrameContext = FrameContext(
      gameTime = GameTime.is(Seconds(2)),
      dice = Dice.fromSeed(1),
      inputState = InputState.default,
      boundaryLocator = BoundaryLocator(
        animationsRegister = AnimationsRegister(),
        fontRegister = FontRegister(),
        dynamicText = DynamicText()
      ),
      _startUpData = SetupData.initial(BootData.default)
    )

    testTable.foreach { (game, expected) =>
      assertEquals(
        game
          .hardDrop(testFrameContext)
          .map(
            _.map.debris.map(_.point)
          ),
        Outcome(expected)
      )
    }
  }

  val standardMap = GameMap.walled(BootData.default.gridSize)

  def gameInProgress(t: Tetromino): GameplayState.InProgress =
    GameplayState.InProgress(
      map = standardMap,
      tetromino = Tetromino.o(Vector2(9, 1)),
      lastUpdatedFalling = Seconds(0.5),
      fallDelay = Seconds(1),
      progress = Progress.initial,
      lastMovement = None
    )
