package pureframes.tetrio
package game

import cats.effect.IO
import cats.effect.kernel.Async
import indigo.*
import indigo.scenes.*
import indigo.shared.datatypes.*
import indigoextras.geometry.BoundingBox
import pureframes.tetrio.game.core.*
import pureframes.tetrio.game.scenes.gameplay.*
import snabbdom.h.apply
import tyrian.TyrianSubSystem

final case class Tetrio[F[_]: Async](
    tyrianSubSystem: TyrianSubSystem[F, ExternalCommand]
) extends IndigoGame[BootData, SetupData, GameModel, GameViewModel]:
  import tyrianSubSystem.TyrianEvent.Receive as FromTyrian

  def initialScene(bootData: BootData): Option[SceneName] =
    None

  def scenes(bootData: BootData) =
    NonEmptyList(GameplayScene)

  val eventFilters: EventFilters =
    EventFilters.Permissive

  def boot(flags: Map[String, String]): Outcome[BootResult[BootData]] =
    BootData.fromFlags(flags).map { bootData =>
      val gameConfig = GameConfig(
        viewport = bootData.initialCanvasSize.toDrawingBufferViewport,
        clearColor = RGBA.Zero,
        magnification = bootData.magnificationLevel
      ).useTransparentBackground

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
    case FromTyrian(ExternalCommand.Input(cmd)) =>
      Outcome(
        GameplayScene.modelInputLens
          .modify(model, _.appendCmd(cmd))
      )

    case FromTyrian(ExternalCommand.Pause) =>
      Outcome(
        // TODO: don't do it here ?
        GameplayScene.modelInputLens
          .modify(model, _.appendCmd(GameplayCommand.Pause))
      )

    case FromTyrian(cmd: ExternalCommand.CanvasResize) =>
      Outcome(
        GameplayScene.modelInputLens
          .modify(model, _.onCanvasResize(cmd.canvasSize))
      )

    case e: GameplayEvent.ProgressUpdated =>
      Outcome(model).addGlobalEvents(
        tyrianSubSystem.send(
          ExternalCommand.UpdateProgress(e.progress, e.inProgress)
        )
      )

    case _ => Outcome(model)

  def updateViewModel(
      context: GameContext,
      model: GameModel,
      viewModel: GameViewModel
  ): GlobalEvent => Outcome[GameViewModel] =

    case FromTyrian(cmd: ExternalCommand.CanvasResize) =>
      Outcome(
        GameplayScene.viewModelLens
          .modify(viewModel, _.onCanvasResize(cmd.canvasSize))
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
object Tetrio:
  val gameNodeId = "game-container"
