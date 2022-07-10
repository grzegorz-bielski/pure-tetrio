package com

import com.init.Assets.Tetrominos
import com.init.*
import com.model.*
import indigo.*
import indigo.scenes.*
import indigo.shared.events.*
import indigoextras.geometry.BoundingBox
import indigoextras.geometry.Vertex

object GameplayScene extends GameScene:
  type SceneModel     = GameModel
  type SceneViewModel = GameViewModel

  val name: SceneName =
    SceneName("game")

  val modelLens: Lens[GameModel, SceneModel] =
    Lens.keepLatest // passthrough

  val viewModelLens: Lens[GameViewModel, SceneViewModel] =
    Lens.keepLatest

  val eventFilters: EventFilters =
    EventFilters.Permissive

  val subSystems: Set[SubSystem] =
    Set()

  def updateModel(
      context: GameContext,
      model: GameModel
  ): GlobalEvent => Outcome[GameModel] = {
    case FrameTick        => model.onFrameTick(context)
    case e: KeyboardEvent => model.onInput(context, e)
    case _                => Outcome(model)
  }

  def updateViewModel(
      context: GameContext,
      model: GameModel,
      viewModel: GameViewModel
  ): GlobalEvent => Outcome[GameViewModel] =
    _ => Outcome(viewModel)

  def present(
      context: GameContext,
      model: GameModel,
      viewModel: GameViewModel
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
          drawOverlay(model.state, bootData)
        )
      )
    )

  // todo: convert to Batch of SceneNodes ?
  def drawGame(model: GameModel, bootData: BootData): SceneNode =
    Group(
      drawMap(model.state, bootData),
      drawTetromino(model.state, bootData)
    ).withScale(bootData.scale)

  // todo: separate scene ?
  def drawOverlay(state: GameState, bootData: BootData): SceneNode =
    val point = state.map.grid.position.toPoint
    val scale = bootData.scale

    state match
      case s: GameState.Paused =>
        drawTextBox("Paused", point).withScale(scale)
      case s: GameState.GameOver =>
        drawTextBox("Game Over", point).withScale(scale)
      case _ => Group.empty

  def drawTextBox(text: String, p: Point) =
    TextBox(text)
      .moveTo(p)
      .withColor(RGBA.White)
      .withFontSize(Pixels(30))

  def drawMap(state: GameState, bootData: BootData) =
    Group(
      state.map.mapElements.map {
        case e: MapElement.Debris =>
          drawDebris(e, bootData)
        case e: (MapElement.Wall | MapElement.Floor) =>
          drawBoundries(e, bootData)
      }
    )

  def drawTetromino(state: GameState, bootData: BootData): SceneNode =
    state match
      case s: GameState.InProgress =>
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

      case s: GameState.Paused => drawTetromino(s.pausedState, bootData)
      case _                   => Group.empty

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
