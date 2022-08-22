package pureframes.tetrio
package game.scenes.gameplay.view

import indigo.*
import indigo.scenes.*
import indigoextras.geometry.Vertex
import pureframes.tetrio.game.core.*
import pureframes.tetrio.game.scenes.gameplay.model.*
import pureframes.tetrio.game.scenes.gameplay.viewmodel.*

import GameplayModel.GameplayState

object GameplayView:
  def present(
      ctx: GameContext,
      model: GameplayModel,
      viewModel: GameplayViewModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment.empty.addLayers(
        Layer(
          BindingKey("game"),
          drawGame(model, viewModel, ctx)
        )
          .withMagnification( ctx.startUpData.bootData.magnificationLevel),
        Layer(
          BindingKey("overlay"),
          drawOverlay(model.state, ctx)
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
      drawMap(model.state, ctx),
      drawTetromino(model.state, viewModel, ctx)
    ).withScale(ctx.startUpData.bootData.scale)

  // todo: separate scene ?
  def drawOverlay(state: GameplayState, ctx: GameContext): SceneNode =
    val point = state.map.grid.position.toPoint
    val scale = ctx.startUpData.bootData.scale

    state match
      case s: GameplayState.Paused =>
        drawTextBox("Paused", point).withScale(scale)
      case s: GameplayState.GameOver =>
        drawTextBox("Game Over", point).withScale(scale)
      case _ => Group.empty

  def drawTextBox(text: String, p: Point) =
    TextBox(text)
      .moveTo(p)
      .withColor(RGBA.White)
      .withFontSize(Pixels(30))

  def drawMap(state: GameplayState, ctx: GameContext) =
    Group(
      state.map.mapElements.map {
        case e: MapElement.Debris =>
          drawDebris(e,  ctx.startUpData.bootData)
        case e: (MapElement.Wall | MapElement.Floor) =>
          drawBoundries(e,  ctx.startUpData.bootData)
      }
    )

  def drawTetromino(
      state: GameplayState,
      viewModel: GameplayViewModel,
      ctx: GameContext
  ): SceneNode =
    val bootData = ctx.startUpData.bootData
    // val halfSquareShift = bootData.gridSquareSize / 2
    val halfSquareShift = 0

    state match
      case state: GameplayState.InProgress =>
        val positions =
          viewModel.tetrominoPositions(ctx)
        val graphic = blockGraphic(
          state.tetromino.extractOrdinal,
          bootData.gameAssets.tetrominos
        )

        Group(
          positions
            .map(p => 
              graphic.moveTo(p - halfSquareShift)
            )
        )

      case s: GameplayState.Paused =>
        drawTetromino(s.pausedState, viewModel, ctx)
      case _ => Group.empty

  def drawDebris(e: MapElement.Debris, bootData: BootData) =
    blockGraphic(e.tetrominoOrdinal, bootData.gameAssets.tetrominos)
      .moveTo(
        gridVectorToPoint(bootData.gridSquareSize)(e.point)
      )

  def drawBoundries(e: MapElement, bootData: BootData) =
    bootData.gameAssets.tetrominos.wall.moveTo(
      gridVectorToPoint(bootData.gridSquareSize)(e.point)
    )

  def gridVectorToPoint(gridSquareSize: Int)(gridVector: Vector2): Point =
    // val halfSquareShift = gridSquareSize / 2
    val halfSquareShift = 0
    (gridVector * gridSquareSize - halfSquareShift).toPoint

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
