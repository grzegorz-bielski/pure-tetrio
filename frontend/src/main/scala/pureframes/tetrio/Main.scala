package pureframes.tetrio

import cats.effect.IO
import cats.effect.kernel.Sync
import cats.syntax.all.*
import indigo.shared.collections.NonEmptyBatch
import org.scalajs.dom
import org.scalajs.dom.*
import org.scalajs.dom.document
import pureframes.tetrio.game.ExternalCommand
import pureframes.tetrio.game.*
import pureframes.tetrio.game.scenes.gameplay.model.Progress
import pureframes.tetrio.ui.Observers.*
import tyrian.Html.*
import tyrian.*
import tyrian.cmds.*

import scala.scalajs.js
import scala.scalajs.js.annotation.*

enum Msg:
  case StartGame
  case Pause
  case Noop
  case GameNodeMounted(element: Element)
  case Resize(entries: NonEmptyBatch[ResizeObserverEntry])
  case UpdateProgress(progress: Progress, inProgress: Boolean)

@JSExportTopLevel("TyrianApp")
object Main extends TyrianApp[Msg, Model]:
  val gameNodeId = "game-container"

  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    (Model.init, Cmd.Emit(Msg.StartGame))

  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
    case Msg.StartGame =>
      (
        model,
        Cmd.Run(
          waitForNodeToMount[IO, Msg](gameNodeId, dom.document.body)
        )(Msg.GameNodeMounted(_))
      )

    case Msg.Pause =>
      (
        model,
        model.bridge.publish(IndigoGameId(gameNodeId), ExternalCommand.Pause)
      )
    case Msg.UpdateProgress(progress, inProgress) =>
      (
        model.copy(
          gameProgress = Some(progress),
          gameInProgress = inProgress
        ),
        Cmd.None
      )

    case Msg.Noop                  => (model, Cmd.None)
    case Msg.GameNodeMounted(node) =>
      // TODO: use node after new Indigo release
      (
        model,
        Cmd.SideEffect {
          Tetrio(model.bridge.subSystem(IndigoGameId(gameNodeId)))
            .launch(gameNodeId)
        }
      )

    case Msg.Resize(entries) =>
      val entry = entries.head

      (
        model,
        Cmd.SideEffect {
          // window.devicePixelRatio
          // entry.devicePixelContentBoxSize ???
        }
      )

  def view(model: Model): Html[Msg] =
    div(`class` := "main")(
      div(`class` := "game", id := gameNodeId)(),
      div(`class` := "ui")(
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
    )

  def subscriptions(model: Model): Sub[IO, Msg] =
    val indigoBridge = model.bridge.subscribe {
      case m: ExternalCommand.UpdateProgress =>
        Some(Msg.UpdateProgress(m.progress, m.inProgress))
      case _ => None
    }

    val resizer = resizeAwaitNode[IO, Msg](gameNodeId, dom.document.body).map {
      (entries, _) =>  Msg.Resize(entries)
    }

    indigoBridge combine resizer

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
