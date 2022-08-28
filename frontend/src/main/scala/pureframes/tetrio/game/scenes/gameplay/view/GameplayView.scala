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
    Outcome(
      SceneUpdateFragment.empty.addLayers(
        Layer(
          BindingKey("game"),
          drawGame(model, viewModel, ctx)
        )
          .withMagnification(ctx.startUpData.bootData.magnificationLevel),
        Layer(
          BindingKey("overlay"),
          drawOverlay(model.state, ctx).toBatch
        )
      )
    )

  def drawGame(
      model: GameplayModel,
      viewModel: GameplayViewModel,
      ctx: GameContext
  ): SceneNode =
    Group(
      drawMap(model.state, ctx) ++ drawTetromino(model.state, viewModel, ctx)
    )
      .scaleBy(viewModel.gameMapScale)
      .moveBy(
        viewModel.gameMapCoords(ctx)
      )

  // todo: separate scene ?
  def drawOverlay(state: GameplayState, ctx: GameContext): Option[SceneNode] =
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

  def drawMap(state: GameplayState, ctx: GameContext): Batch[SceneNode] =
    state.map.mapElements.map {
      case e: MapElement.Debris =>
        drawDebris(e, ctx)
      case e: (MapElement.Wall | MapElement.Floor) =>
        drawBoundries(e, ctx)
    }

  def drawTetromino(
      state: GameplayState,
      viewModel: GameplayViewModel,
      ctx: GameContext
  ): Batch[SceneNode] =
    val bootData = ctx.startUpData.bootData

    state match
      case state: GameplayState.InProgress =>
        val graphic = blockGraphic(
          state.tetromino.extractOrdinal,
          bootData.gameAssets.tetrominos
        )

        tetrominoPositions(viewModel, ctx)
          .map(graphic.moveTo)

      case s: GameplayState.Paused =>
        drawTetromino(s.pausedState, viewModel, ctx)
      case _ => Batch.empty[SceneNode]

  def drawDebris(e: MapElement.Debris, ctx: GameContext) =
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
        toGridPoint(ctx)(e.point).toPoint
      )

  def drawBoundries(e: MapElement, ctx: GameContext) =
    ctx.startUpData.bootData.gameAssets.tetrominos.wall.moveTo(
      toGridPoint(ctx)(e.point).toPoint
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

  def tetrominoPositions(
      viewModel: GameplayViewModel,
      ctx: GameContext
  ): Batch[Point] =
    lazy val ctxGrindPoint = toGridPoint(ctx).andThen(_.toPoint)
    lazy val targetPositions = (state: GameplayViewModel.State.InProgress) =>
      state.targetTetrominoPositions.map(ctxGrindPoint)

    viewModel.state match
      // format: off
      case vm @ GameplayViewModel.State.InProgress(Some(prevTetrominoPositions), _, _) =>
          (prevTetrominoPositions.map(ctxGrindPoint) zip targetPositions(vm))
              .map(
                Signal
                  .Lerp(_, _, Seconds(0.093))
                  .at(ctx.gameTime.running - vm.from)
              ).toBatch
      // format: on
      case vm: GameplayViewModel.State.InProgress => targetPositions(vm).toBatch
      case _                                      => Batch.empty

  def toGridPoint(ctx: GameContext)(point: Vector2) =
    point * ctx.startUpData.bootData.gridSquareSize
