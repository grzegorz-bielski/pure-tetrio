package com

import com.init.*
import indigo.*
import indigo.scenes.*


object GameplayScene extends GameScene:

  type SceneModel     = GameModel
  type SceneViewModel = GameViewModel

  val name: SceneName =
    SceneName("game")

  val modelLens: Lens[GameModel, SceneModel] =
    Lens.keepLatest

  val viewModelLens: Lens[GameViewModel, SceneViewModel] =
    Lens.keepLatest

  val eventFilters: EventFilters =
    EventFilters.Permissive

  val subSystems: Set[SubSystem] =
    Set()

  def updateModel(
      context: GameContext,
      model: GameModel
  ): GlobalEvent => Outcome[GameModel] =
    _ => Outcome(model)

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
      SceneUpdateFragment(
        Shape
          .Box(
            Rectangle(0, 0, 60, 199),
            Fill.LinearGradient(Point(0), RGBA.Magenta, Point(45), RGBA.Cyan)
          )
          .withRef(30, 30)
          .moveTo(100, 100)
          .rotateTo(Radians.fromSeconds(context.running * 0.25))
      )
    )
