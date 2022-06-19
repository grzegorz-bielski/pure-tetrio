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
    logger.debugOnce(model.state.map.mapElements.toString)

    Group(
      // Group(
      //   drawMapElement(MapElement.Wall(Vertex(4, 0)), c),
      //    drawMapElement(MapElement.Wall(Vertex(5, 0)), c),
      //     drawMapElement(MapElement.Wall(Vertex(6, 0)), c),
      //      drawMapElement(MapElement.Wall(Vertex(7, 0)), c),
      //        drawMapElement(MapElement.Wall(Vertex(8, 0)), c),
      //        drawMapElement(MapElement.Wall(Vertex(9, 0)), c),
      //        drawMapElement(MapElement.Wall(Vertex(10, 0)), c),
      //        drawMapElement(MapElement.Wall(Vertex(11, 0)), c),
      //        drawMapElement(MapElement.Wall(Vertex(12, 0)), c),
      //   drawMapElement(MapElement.Wall(Vertex(13, 0)), c)
      // ),
      // Group(
      // ),

      Group(
        model.state.map.mapElements
          .map { e =>
            drawMapElement(e, RGBA.Silver)
          }
      ),
      drawTetromino(model.state)
    )

  def drawTetromino(state: GameState) =
    state match
      case s: GameState.InProgress =>
        Group(
          s.tetromino.positions.map { p =>
            Shape.Box(
              Rectangle(p.x.toInt, p.y.toInt, 1, 1),
              Fill.Color(RGBA.Pink)
            )
          }.toBatch
        )

      case _ => Group.empty

  def drawMapElement(e: MapElement, color: RGBA) =
    Shape.Box(
      Rectangle(e.point.x.toInt, e.point.y.toInt, 1, 1),
      Fill.Color(color)
    )
