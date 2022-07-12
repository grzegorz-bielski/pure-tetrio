package com.scenes.gameplay.view

import com.core.*
import com.scenes.gameplay.model.*
import com.scenes.gameplay.viewmodel.*
import indigo.*
import indigo.scenes.*

object GameplayView:
  def present(
      ctx: GameContext,
      model: GameplayModel,
      viewModel: GameplayViewModel
  ): Outcome[SceneUpdateFragment] =
    // val bootData = context.startUpData.bootData

    Outcome(
      SceneUpdateFragment.empty.addLayers(
        Layer(
          BindingKey("game"),
          drawGame(model, viewModel, ctx)
        )
          .withMagnification( ctx.startUpData.bootData.magnificationLevel),
        Layer(
          BindingKey("overlay"),
          drawOverlay(model, ctx)
        )
      )
    )

  // todo: convert to Batch of SceneNodes ?
  def drawGame(
      model: GameplayModel,
      viewModel: GameplayViewModel,
      ctx: GameContext
  ): SceneNode =
    Group(
      drawMap(model, ctx),
      drawTetromino(model, viewModel, ctx)
    ).withScale(ctx.startUpData.bootData.scale)

  // todo: separate scene ?
  def drawOverlay(state: GameplayModel, ctx: GameContext): SceneNode =
    val point = state.map.grid.position.toPoint
    val scale = ctx.startUpData.bootData.scale

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

  def drawMap(state: GameplayModel, ctx: GameContext) =
    Group(
      state.map.mapElements.map {
        case e: MapElement.Debris =>
          drawDebris(e,  ctx.startUpData.bootData)
        case e: (MapElement.Wall | MapElement.Floor) =>
          drawBoundries(e,  ctx.startUpData.bootData)
      }
    )

  def drawTetromino(
      state: GameplayModel,
      viewModel: GameplayViewModel,
      ctx: GameContext
  ): SceneNode =
    state match
      case model: GameplayModel.InProgress =>
        val positions =
          // model.tetromino.positions.map(gridPointToPoint(bootData.gridSquareSize))
          viewModel.tetrominoPositions(ctx)
        val graphic = blockGraphic(
          model.tetromino.extractOrdinal,
          ctx.startUpData.bootData.gameAssets.tetrominos
        )

        //  gridPointToPoint(bootData.gridSquareSize) andThen 

        Group(
          positions
            .map(
              // gridPointToPoint(bootData.gridSquareSize) andThen graphic.moveTo
              graphic.moveTo
            )
        )

      case s: GameplayModel.Paused =>
        drawTetromino(s.pausedState, viewModel, ctx)
      case _ => Group.empty

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
    gridPoint * gridSquareSize

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
