package pureframes.tetrio
package game.scenes.gameplay.view

import indigo.*
import indigo.scenes.*
import indigo.shared.datatypes.RGBA
import indigo.shared.datatypes.Rectangle.apply
import indigo.shared.datatypes.Size
import indigo.shared.scenegraph.Shape
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
    given GameContext = ctx

    Outcome(
      SceneUpdateFragment.empty.addLayers(
        Layer(
          BindingKey("game"),
          drawGame(model, viewModel)
        )
          .withMagnification(ctx.startUpData.bootData.magnificationLevel),
        Layer(
          BindingKey("overlay"),
          drawOverlay(model.state).toBatch
        )
      )
    )

  def drawGame(
      model: GameplayModel,
      viewModel: GameplayViewModel
  )(using GameContext): SceneNode =
    Group(
      drawMap(model.state) ++ drawTetromino(model.state, viewModel)
    )
      .scaleBy(viewModel.gameMapScale)
      .moveBy(
        viewModel.gameMapCoords
      )

  // todo: separate scene ?
  def drawOverlay(state: GameplayState)(using ctx: GameContext): Option[SceneNode] =
    import ctx.startUpData.bootData.scale
    val point = state.map.grid.position.toPoint

    state match
      case s: GameplayState.Paused =>
        Some(drawTextBox("Paused", point, scale))
      case s: GameplayState.GameOver =>
        Some(drawTextBox("Game Over", point, scale))
      case _ => None

  def drawTextBox(text: String, p: Point, scale: Vector2): SceneNode =
    TextBox(text)
      .scaleBy(scale)
      .moveTo(p)
      .withColor(RGBA.White)
      .withFontSize(Pixels(30))

  def drawMap(state: GameplayState)(using GameContext): Batch[SceneNode] =
    state.map.elements.map {
      case e: MapElement.Debris                    => drawDebris(e)
      case e: (MapElement.Wall | MapElement.Floor) => drawBoundaries(e)
    }

  def drawTetromino(
      state: GameplayState,
      viewModel: GameplayViewModel
  )(using ctx: GameContext): Batch[SceneNode] =
    val bootData = ctx.startUpData.bootData

    state match
      case state: GameplayState.InProgress =>
        val graphic = blockGraphic(
          state.tetromino.extractOrdinal,
          bootData.gameAssets.tetrominos
        )

        viewModel.currentTetrominoPositions
          .map(graphic.moveTo)

      case s: GameplayState.Paused =>
        drawTetromino(s.pausedState, viewModel)
      case _ => Batch.empty[SceneNode]

  def drawDebris(e: MapElement.Debris)(using ctx: GameContext) =
    import ctx.startUpData.bootData.{gameAssets, gridSquareSize}

    e.tetrominoOrdinal
      .map(blockGraphic(_, gameAssets.tetrominos))
      .getOrElse(
        Shape.Box(
          Rectangle(Size(gridSquareSize)),
          Fill.Color(RGBA.SlateGray)
        )
      )
      .moveTo(
        GameplayViewModel.toGridPoint(e.point).toPoint
      )

  def drawBoundaries(e: MapElement)(using ctx: GameContext) =
    ctx.startUpData.bootData.gameAssets.tetrominos.wall.moveTo(
      GameplayViewModel.toGridPoint(e.point).toPoint
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
