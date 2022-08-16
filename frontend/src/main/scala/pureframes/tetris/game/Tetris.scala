package pureframes.tetris
package game

import cats.effect.IO
import indigo.*
import indigo.scenes.*
import indigo.shared.datatypes.*
import pureframes.tetris.game.core.*
import pureframes.tetris.game.scenes.gameplay.*
import tyrian.TyrianSubSystem

final case class Tetris(tyrianSubSystem: TyrianSubSystem[IO, ExternalCommand])
    extends IndigoGame[BootData, SetupData, GameModel, GameViewModel]:

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
        // TODO: take it from config / Tyrian ?
        // clearColor = RGBA.Black,
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
    Outcome(GameViewModel.initial)

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
        tyrianSubSystem.send(ExternalCommand.UpdateProgress(e.progress, e.inProgress))
      )
    case _ => Outcome(model)

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

  private def onExternalCommand(
      cmd: ExternalCommand,
      model: GameModel
  ): Outcome[GameModel] =
    cmd match
      case ExternalCommand.Pause =>
        Outcome(
          // TODO: don't do it here  :vomit ... or maybe use some lenses
          model.copy(
            gameplay = model.gameplay.copy(
              input = model.gameplay.input.copy(
                cmds = model.gameplay.input.cmds :+ GameplayCommand.Pause
              )
            )
          )
        )
      case _ => Outcome(model)
