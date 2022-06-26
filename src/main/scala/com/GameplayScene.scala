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
      SceneUpdateFragment.empty.addLayer(
        Layer(
          BindingKey("game"),
          drawGame(model)
        )
      )
    )

  def drawGame(model: GameModel): SceneNode =
    // logger.debugOnce(model.state.map.mapElements.toString)

    Group(
      Group(
        model.state.map.mapElements
          .map {
            case e: MapElement.Debris =>
              drawMapElement(e, e.color)
            case e: (MapElement.Wall | MapElement.Floor) =>
              drawMapElement(e, RGBA.Silver)
          }
      ),
      drawTetromino(model.state),
      // Shape.Circle(
      //   Point(11, 21),
      //   1,
      //   Fill.Color(RGBA.Tomato)
      // ),
      //  Shape.Circle(
      //   Point(16, 24),
      //   1,
      //   Fill.Color(RGBA.White)
      // )
    )

  def drawTetromino(state: GameState) =
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

      case _ => Group.empty

  def drawMapElement(e: MapElement, color: RGBA) =
    Shape.Box(
      Rectangle(e.point.x.toInt, e.point.y.toInt, 1, 1),
      Fill.Color(color)
    )
