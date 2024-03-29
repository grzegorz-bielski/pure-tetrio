package pureframes.tetrio.game.scenes.gameplay

import indigo.IndigoLogger.*
import indigo.*
import indigo.scenes.*
import indigo.shared.Outcome
import indigo.shared.events.InputEvent
import indigo.shared.events.KeyboardEvent.KeyDown
import indigoextras.gestures.GestureEvent
import pureframes.tetrio.game.*
import pureframes.tetrio.game.core.*
import pureframes.tetrio.game.scenes.gameplay.*
import pureframes.tetrio.game.scenes.gameplay.model.*
import pureframes.tetrio.game.scenes.gameplay.view.*
import pureframes.tetrio.game.scenes.gameplay.viewmodel.*

import scala.collection.immutable.Queue

import GameplayCommand.*
import RotationDirection.*

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

  val modelInputLens = modelLens andThen GameplayInput.lens

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
      ctx: SceneContext[SetupData],
      model: SceneModel
  ): GlobalEvent => Outcome[SceneModel] =
    case e: InputEvent   => model.onInput(e, ctx.frameContext)
    case e: GestureEvent => model.onGesture(e)
    case FrameTick       => model.onFrameTick(ctx.frameContext)
    case _               => Outcome(model)

  def updateViewModel(
      ctx: SceneContext[SetupData],
      model: SceneModel,
      viewModel: SceneViewModel
  ): GlobalEvent => Outcome[SceneViewModel] =
    case FrameTick => viewModel.onFrameTick(model, ctx.frameContext)
    case _         => Outcome(viewModel)

  def present(
      ctx: SceneContext[SetupData],
      model: SceneModel,
      viewModel: SceneViewModel
  ): Outcome[SceneUpdateFragment] =
    GameplayView.present(ctx.frameContext, model, viewModel)
