package pureframes.tetrio
package game

import cats.effect.IO
import indigo.*
import indigo.scenes.*
import indigo.shared.datatypes.*
import indigoextras.geometry.BoundingBox
import pureframes.tetrio.game.core.*
import pureframes.tetrio.game.scenes.gameplay.*
import snabbdom.h.apply
import tyrian.TyrianSubSystem

final case class Tetrio(tyrianSubSystem: TyrianSubSystem[IO, ExternalCommand])
    extends IndigoGame[BootData, SetupData, GameModel, GameViewModel]:

  def initialScene(bootData: BootData): Option[SceneName] =
    None

  def scenes(bootData: BootData) =
    NonEmptyList(GameplayScene)

  val eventFilters: EventFilters =
    EventFilters.Permissive

  def boot(flags: Map[String, String]): Outcome[BootResult[BootData]] =
    BootData.fromFlags(flags).map { bootData =>
      val gameConfig = GameConfig(
        viewport = bootData.initialViewport,
        clearColor = RGBA.fromHexString("#242424"),
        magnification = bootData.magnificationLevel
      )

      BootResult(gameConfig, bootData)
        .withAssets(Assets.assets)
        .withSubSystems(tyrianSubSystem)
    }

  def initialModel(setupData: SetupData): Outcome[GameModel] =
    Outcome {
      GameModel.initial(setupData)
    }

  def initialViewModel(
      startupData: SetupData,
      model: GameModel
  ): Outcome[GameViewModel] =
    Outcome(GameViewModel.initial(startupData))

  def setup(
      bootData: BootData,
      assetCollection: AssetCollection,
      dice: Dice
  ): Outcome[Startup[SetupData]] =
    Outcome(Startup.Success(SetupData.initial(bootData)))

  def updateModel(
      context: GameContext,
      model: GameModel
  ): GlobalEvent => Outcome[GameModel] =
    case tyrianSubSystem.TyrianEvent.Receive(cmd) =>
      onExternalCommand(cmd, model)
    //  Why can't I use `SceneEvent` as a scrutine ??
    case e: GameplayEvent.ProgressUpdated =>
      Outcome(model).addGlobalEvents(
        tyrianSubSystem.send(
          ExternalCommand.UpdateProgress(e.progress, e.inProgress)
        )
      )
    // case e: ViewportResize =>
    // println(e)
    // Outcome(model)
    case _ => Outcome(model)

  def updateViewModel(
      context: GameContext,
      model: GameModel,
      viewModel: GameViewModel
  ): GlobalEvent => Outcome[GameViewModel] =

    case e: ViewportResize =>
      Outcome(
        GameplayScene.viewModelLens
          .modify(viewModel, _.onViewportResize(e.gameViewPort))
      )
    case _ => Outcome(viewModel)

  def present(
      context: GameContext,
      model: GameModel,
      viewModel: GameViewModel
  ): Outcome[SceneUpdateFragment] =
    Outcome {
      SceneUpdateFragment.empty
        .addLayer(Layer(BindingKey("game")))
    }

  private def onExternalCommand(
      cmd: ExternalCommand,
      model: GameModel
  ): Outcome[GameModel] =
    cmd match
      case ExternalCommand.Pause =>
        Outcome(
          // TODO: don't do it here ?
          GameplayScene.modelInputLens
            .modify(model, _.appendCmd(GameplayCommand.Pause))
        )
      case _ => Outcome(model)
