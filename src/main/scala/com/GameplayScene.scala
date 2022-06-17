package com

import com.init.*
import com.model.*
import indigo.*
import indigo.scenes.*
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
    case FrameTick => model.update(context)
    case _         => Outcome(model)
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
      // SceneUpdateFragment(
      //   Shape
      //     .Box(
      //       Rectangle(0, 0, 60, 199),
      //       Fill.LinearGradient(Point(0), RGBA.Magenta, Point(45), RGBA.Cyan)
      //     )
      //     .withRef(30, 30)
      //     .moveTo(100, 100)
      //     .rotateTo(Radians.fromSeconds(context.running * 0.25))
      // )
    )

  def drawGame(model: GameModel): SceneNode =
    logger.debugOnce(model.map.mapElements.toString)

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
        model.map.mapElements
          .map { e =>
            drawMapElement(e, RGBA.Silver)
          }
      ),

    drawTetramino(model.state),
    )

  def drawTetramino(state: GameState) = 
    state match
     case s: GameState.InProgress => 
          Group(
            s.tetramino.positions.map { p => 
              Shape.Box(
                 Rectangle(p.x.toInt, p.y.toInt, 1, 1),
                 Fill.Color(RGBA.Pink)
              )
            }
          )

     case _  => Group.empty

  def drawMapElement(e: MapElement, color: RGBA) =
    Shape.Box(
      Rectangle(e.point.x.toInt, e.point.y.toInt, 1, 1),
      Fill.Color(color)
    )
