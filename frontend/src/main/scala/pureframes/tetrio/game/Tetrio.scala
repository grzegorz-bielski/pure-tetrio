package pureframes.tetrio
package game

import cats.effect.IO
import indigo.*
import indigo.scenes.*
import indigo.shared.datatypes.*
import indigoextras.geometry.BoundingBox
import pureframes.tetrio.game.core.*
import pureframes.tetrio.game.scenes.gameplay.*
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
    Outcome {
      val width  = flags.get("width").flatMap(_.toIntOption)
      val height = flags.get("height").flatMap(_.toIntOption)

      val bootData = (
        for
          w <- width
          h <- height
        yield BootData.fromBoundingBox(
          BoundingBox(
            x = 0,
            y = 3,
            // TODO: wrong spawn point, wrong height
            // x = (w / 2 - BootData.gridWidthExternal) / BootData.gridSquareSize,
            // y = (w / 2 - BootData.gridHeightExternal) /  BootData.gridSquareSize,
            width = w,
            height = h
          )
        )
      ).getOrElse(BootData.default)

      val gameConfig = GameConfig(
        viewport = bootData.viewport,
        // TODO: take it from flags ?
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
        tyrianSubSystem.send(
          ExternalCommand.UpdateProgress(e.progress, e.inProgress)
        )
      )
    case e: ViewportResize =>
      println(e)
      Outcome(model)
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
