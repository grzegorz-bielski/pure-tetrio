package pureframes.tetrio

import cats.effect.IO
import org.scalajs.dom.document
import pureframes.tetrio.game.ExternalCommand
import pureframes.tetrio.game.*
import pureframes.tetrio.game.scenes.gameplay.model.Progress
import tyrian.Html.*
import tyrian.*
import tyrian.cmds.*

import scala.scalajs.js.annotation.*

enum Msg:
  case StartGame
  case Pause
  case UpdateProgress(progress: Progress, inProgress: Boolean)

@JSExportTopLevel("TyrianApp")
object Main extends TyrianApp[Msg, Model]:
  val gameDivId = "game-container"

  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    (Model.init, Cmd.Emit(Msg.StartGame))

  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
    case Msg.StartGame =>
      (
        model,
        Cmd.SideEffect {
          Tetrio(model.bridge.subSystem(IndigoGameId(gameDivId)))
            .launch(
              gameDivId,
              "width"  -> "550",
              "height" -> "400"
            )
        }
      )
    case Msg.Pause =>
      (
        model,
        model.bridge.publish(IndigoGameId(gameDivId), ExternalCommand.Pause)
      )
    case Msg.UpdateProgress(progress, inProgress) =>
      (
        model.copy(
          gameProgress = Some(progress),
          gameInProgress = inProgress
        ),
        Cmd.None
      )

  def view(model: Model): Html[Msg] =
    div(`class` := "main")(
      div(`class` := "game", id := gameDivId)(),
      div(`class` := "btn")(
        button(onClick(Msg.Pause))("Pause")
      ),
      div()(s"Is in progress ${model.gameInProgress}"),
      model.gameProgress
        .map { progress =>
          ul()(
            li()(s"level: ${progress.level}"),
            li()(s"lines: ${progress.lines}"),
            li()(s"score: ${progress.score}")
          )
        }
        .getOrElse(div()) // TODO: how to present empty elements?
    )

  def subscriptions(model: Model): Sub[IO, Msg] =
    model.bridge.subscribe {
      case m: ExternalCommand.UpdateProgress =>
        Some(Msg.UpdateProgress(m.progress, m.inProgress))
      case _ => None
    }

case class Model(
    bridge: TyrianIndigoBridge[IO, ExternalCommand],
    gameInProgress: Boolean,
    gameProgress: Option[Progress]
)
object Model:
  val init: Model = Model(
    bridge = TyrianIndigoBridge(),
    gameInProgress = false,
    gameProgress = None
  )
