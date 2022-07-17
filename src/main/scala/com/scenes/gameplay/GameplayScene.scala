package com.scenes.gameplay

import com.core.BootData
import com.core.GameContext
import com.core.GameModel
import com.core.GameViewModel
import com.core.SetupData
import com.scenes.gameplay.model.*
import com.scenes.gameplay.view.*
import com.scenes.gameplay.viewmodel.*
import indigo.*
import indigo.scenes.*
import indigo.shared.events.InputEvent
import indigo.shared.events.*
import indigoextras.geometry.BoundingBox
import indigoextras.geometry.Vertex

object GameplayScene extends Scene[SetupData, GameModel, GameViewModel]:
  type SceneModel     = GameplayModel
  type SceneViewModel = GameplayViewModel

  val name: SceneName =
    SceneName("game")

  val modelLens: Lens[GameModel, SceneModel] =
    Lens(
      _.gameplay,
      (m, sm) => m.copy(gameplay = sm)
    )

  val viewModelLens: Lens[GameViewModel, SceneViewModel] =
    Lens(
      _.gameplay,
      (m, svm) => m.copy(gameplay = svm)
    )

  val eventFilters: EventFilters =
    EventFilters.Permissive

  val subSystems: Set[SubSystem] =
    Set()

  def updateModel(
      context: GameContext,
      model: SceneModel
  ): GlobalEvent => Outcome[SceneModel] = {

    // model.update(keyboard events  => frame ticks) .... viewmodel.update

    case e: InputEvent => model.onInput(context, e)
    case FrameTick        => model.onFrameTick(context)
    case _                => Outcome(model)
  }

  def updateViewModel(
      context: GameContext,
      model: SceneModel,
      viewModel: SceneViewModel
  ): GlobalEvent => Outcome[SceneViewModel] = {
    case FrameTick => viewModel.onFrameTick(context, model)
    case _ => Outcome(viewModel)
  }

  def present(
      ctx: GameContext,
      model: SceneModel,
      viewModel: SceneViewModel
  ): Outcome[SceneUpdateFragment] =
    GameplayView.present(ctx, model, viewModel)
