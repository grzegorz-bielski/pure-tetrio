package com

import com.init.*
import com.model.*
import indigo.*
import indigo.scenes.*
import indigo.shared.events.*
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
    Outcome(
      SceneUpdateFragment.empty.addLayers(
        Layer(
          BindingKey("game"),
          drawGame(model)
        ),
        Layer(
          BindingKey("overlay"),
          drawOverlay(model.state)
        ).withMagnification(1)
      )
    )

  def drawGame(model: GameModel): SceneNode =
    Group(
      drawMap(model.state),
      drawTetromino(model.state)
    )

  // todo: separate scene ?
  def drawOverlay(state: GameState): SceneNode =
    val p = state.map.grid.position.toPoint

    state match
      case s: GameState.Paused   => drawTextBox("Paused", p)
      case s: GameState.GameOver => drawTextBox("Game Over", p)
      case _                     => Group.empty

  def drawTextBox(text: String, p: Point): SceneNode =
    TextBox(text)
      .moveTo(p)
      .withColor(RGBA.White)
      .withFontSize(Pixels(30))

  def drawMap(state: GameState): SceneNode =
    Group(
      state.map.mapElements.map {
        case e: MapElement.Debris =>
          drawMapElement(e, e.color)
        case e: (MapElement.Wall | MapElement.Floor) =>
          drawMapElement(e, RGBA.Silver)
      }
    )

  def drawTetromino(state: GameState): SceneNode =
    state match
      case s: GameState.InProgress =>
        Group(
          s.tetromino.positions.map { p =>
            Shape.Box(
              Rectangle(p.x.toInt, p.y.toInt, 1, 1),
              Fill.Color(s.tetromino.color)
            )
          }.toBatch
        )
      case s: GameState.Paused => drawTetromino(s.pausedState)
      case _                   => Group.empty

  def drawMapElement(e: MapElement, color: RGBA) =
    Shape.Box(
      Rectangle(e.point.x.toInt, e.point.y.toInt, 1, 1),
      Fill.Color(color)
    )
