package com

import com.core.*
import com.scenes.gameplay.*
import indigo.*
import indigo.scenes.*
import indigo.shared.datatypes.*

import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("IndigoGame")
object IndigoTetris extends IndigoGame[BootData, SetupData, GameModel, GameViewModel]:

  // todo: trigger on event?
  // val `blackify the screen` =
  //    org.scalajs.dom.document.body.style = "background: black"


  def initialScene(bootData: BootData): Option[SceneName] =
    None

  def scenes(bootData: BootData) =
    NonEmptyList(GameplayScene)

  val eventFilters: EventFilters =
    EventFilters.Permissive

  def boot(flags: Map[String, String]): Outcome[BootResult[BootData]] =
    Outcome {
      val bootData = BootData.default
      val gameConfig = GameConfig(
        viewport = bootData.viewport,
        clearColor = RGBA.Black,
        magnification = bootData.magnificationLevel
      )

      BootResult(gameConfig, bootData).withAssets(Assets.assets)
    }

  def initialModel(startupData: SetupData): Outcome[GameModel] =
    Outcome {
      GameModel.initial(startupData.bootData.gridSize)
    }

  def initialViewModel(
      startupData: SetupData,
      model: GameModel
  ): Outcome[GameViewModel] =
    Outcome(GameViewModel.initial)

  def setup(
      bootData: BootData,
      assetCollection: AssetCollection,
      dice: Dice
  ): Outcome[Startup[SetupData]] =
    Outcome(Startup.Success(SetupData(bootData)))

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
    Outcome {
      SceneUpdateFragment.empty
        .addLayer(Layer(BindingKey("game")))
    }
