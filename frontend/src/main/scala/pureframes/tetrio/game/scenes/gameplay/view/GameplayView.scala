package pureframes.tetrio.game.scenes.gameplay.view

import indigo.*
import indigo.scenes.*
import indigo.shared.datatypes.RGBA
import indigo.shared.datatypes.Rectangle.apply
import indigo.shared.datatypes.Size
import indigo.shared.scenegraph.Shape
import indigo.shared.scenegraph.Shape.Circle
import pureframes.tetrio.game.core.*
import pureframes.tetrio.game.scenes.gameplay.model.*
import pureframes.tetrio.game.scenes.gameplay.viewmodel.*

import GameplayModel.GameplayState
import GameplayViewModel.toGridPoint

object GameplayView:
  def present(
      ctx: GameContext,
      model: GameplayModel,
      viewModel: GameplayViewModel
  ): Outcome[SceneUpdateFragment] =
    given GameContext = ctx

    Outcome:
      SceneUpdateFragment.empty.addLayers:
        Layer(
          BindingKey("game"),
          drawGame(model, viewModel)
        )
          .withMagnification(ctx.startUpData.bootData.magnificationLevel)


  def drawGame(
      model: GameplayModel,
      viewModel: GameplayViewModel
  )(using GameContext): SceneNode =
      Group(
          drawPreview(model.state) ++
          drawMap(model.state) ++
          drawTetromino(model.state, viewModel) ++
          drawHeld(model.state) ++
          drawNext(model.state)
      )
        .scaleBy(viewModel.gameMapScale)
        .moveBy(viewModel.gameMapCoords)

  def drawMap(state: GameplayState)(using GameContext): Batch[SceneNode] =
    state.map.elements.map {
      case e: MapElement.Debris                    => drawDebris(e)
      case e: (MapElement.Wall | MapElement.Floor) => drawBoundaries(e)
    }

  def drawHeld(state: GameplayState)(using ctx: GameContext): Batch[SceneNode] =
    state match
      case state: GameplayState.InProgress =>
        state.held.map(tetrominoGraphic).toBatch
      case _ =>
        Batch.empty

  def drawNext(state: GameplayState)(using ctx: GameContext): Batch[SceneNode] =
    import ctx.startUpData.bootData.{gridSize, gridSquareSize}

    state match
      case state: GameplayState.InProgress =>
        Batch:
          tetrominoGraphic(state.next).moveBy:
            Point(gridSize.width.toInt * gridSquareSize, 0)
      case _ => 
        Batch.empty

  def drawTetromino(
      state: GameplayState,
      viewModel: GameplayViewModel
  )(using ctx: GameContext): Batch[SceneNode] =
    state match
      case s: GameplayState.InProgress =>
        viewModel.currentTetrominoPositions
          .map(tetrominoGraphic(s.tetromino).moveTo)

      case s: GameplayState.Paused =>
        drawTetromino(s.pausedState, viewModel)
      case _ => Batch.empty[SceneNode]

  def drawPreview(
      state: GameplayState
  )(using ctx: GameContext): Batch[SceneNode] =
    import ctx.startUpData.bootData.{gameAssets, gridSize, gridSquareSize}

    state match
      case s: GameplayState.InProgress =>
        val movement = s.movementClosestToBottom

        if movement.intersectedStack then Batch.empty[SceneNode]
        else
          movement.movedTetromino.positions.map: p =>
            gameAssets.tetrominos.preview.moveTo(toGridPoint(p))
          .toBatch
      case _ => Batch.empty[SceneNode]

  def tetrominoGraphic(tetromino: Tetromino)(using ctx: GameContext) =
    blockGraphic(
      tetromino.extractOrdinal,
      ctx.startUpData.bootData.gameAssets.tetrominos
    )

  def drawDebris(e: MapElement.Debris)(using ctx: GameContext) =
    import ctx.startUpData.bootData.{gameAssets, gridSquareSize}

    e.tetrominoOrdinal
      .map(blockGraphic(_, gameAssets.tetrominos))
      .getOrElse:
        Shape.Box(
          Rectangle(Size(gridSquareSize)),
          Fill.Color(RGBA.SlateGray)
        )
      .moveTo:
        GameplayViewModel.toGridPoint(e.point)

  def drawBoundaries(e: MapElement)(using ctx: GameContext) =
    ctx.startUpData.bootData.gameAssets.tetrominos.wall.moveTo(toGridPoint(e.point))

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
