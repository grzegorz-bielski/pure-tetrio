package com.scenes.gameplay.view

import com.core.*
import com.scenes.gameplay.model.*
import com.scenes.gameplay.viewmodel.*
import indigo.*
import indigo.scenes.*

object GameplayView:
  def present(
      context: GameContext,
      model: GameplayModel,
      viewModel: GameplayViewModel
  ): Outcome[SceneUpdateFragment] =
    val bootData = context.startUpData.bootData

    Outcome(
      SceneUpdateFragment.empty.addLayers(
        Layer(
          BindingKey("game"),
          drawGame(model, bootData)
        )
          .withMagnification(bootData.magnificationLevel),
        Layer(
          BindingKey("overlay"),
          drawOverlay(model, bootData)
        )
      )
    )

  // todo: convert to Batch of SceneNodes ?
  def drawGame(model: GameplayModel, bootData: BootData): SceneNode =
    Group(
      drawMap(model, bootData),
      drawTetromino(model, bootData)
    ).withScale(bootData.scale)

  // todo: separate scene ?
  def drawOverlay(state: GameplayModel, bootData: BootData): SceneNode =
    val point = state.map.grid.position.toPoint
    val scale = bootData.scale

    state match
      case s: GameplayModel.Paused =>
        drawTextBox("Paused", point).withScale(scale)
      case s: GameplayModel.GameOver =>
        drawTextBox("Game Over", point).withScale(scale)
      case _ => Group.empty

  def drawTextBox(text: String, p: Point) =
    TextBox(text)
      .moveTo(p)
      .withColor(RGBA.White)
      .withFontSize(Pixels(30))

  def drawMap(state: GameplayModel, bootData: BootData) =
    Group(
      state.map.mapElements.map {
        case e: MapElement.Debris =>
          drawDebris(e, bootData)
        case e: (MapElement.Wall | MapElement.Floor) =>
          drawBoundries(e, bootData)
      }
    )

  def drawTetromino(state: GameplayModel, bootData: BootData): SceneNode =
    state match
      case s: GameplayModel.InProgress =>
        val graphic = blockGraphic(
          s.tetromino.extractOrdinal,
          bootData.gameAssets.tetrominos
        )

        Group(
          s.tetromino.positions
            .map(
              gridPointToPoint(bootData.gridSquareSize) andThen graphic.moveTo
            )
            .toBatch
        )

      case s: GameplayModel.Paused => drawTetromino(s.pausedState, bootData)
      case _                       => Group.empty

  def drawDebris(e: MapElement.Debris, bootData: BootData) =
    blockGraphic(e.tetrominoOrdinal, bootData.gameAssets.tetrominos)
      .moveTo(
        gridPointToPoint(bootData.gridSquareSize)(e.point.toPoint)
      )

  def drawBoundries(e: MapElement, bootData: BootData) =
    bootData.gameAssets.tetrominos.wall.moveTo(
      gridPointToPoint(bootData.gridSquareSize)(e.point.toPoint)
    )

  def gridPointToPoint(gridSquareSize: Int)(gridPoint: Point): Point =
    Point(
      x = gridPoint.x * gridSquareSize,
      y = gridPoint.y * gridSquareSize
    )

  def blockGraphic(
      ord: Tetromino.Ordinal,
      graphics: Assets.Tetrominos
  ): Graphic[Material.Bitmap] =
    ord match
      case 0 => graphics.i
      case 1 => graphics.j
      case 2 => graphics.l
      case 3 => graphics.o
      case 4 => graphics.s
      case 5 => graphics.t
      case 6 => graphics.z
