package pureframes.tetrio

import cats.effect.IO
import cats.effect.kernel.Sync
import cats.syntax.all.*
import org.scalajs.dom
import org.scalajs.dom.document
import pureframes.tetrio.game.ExternalCommand
import pureframes.tetrio.game.*
import pureframes.tetrio.game.scenes.gameplay.model.Progress
import tyrian.Html.*
import tyrian.*
import tyrian.cmds.*

import scala.scalajs.js
import scala.scalajs.js.annotation.*

enum Msg:
  case StartGame
  case Pause
  case Resize(entries: js.Array[dom.ResizeObserverEntry])
  case UpdateProgress(progress: Progress, inProgress: Boolean)

@SuppressWarnings(Array("scalafix:DisableSyntax.null"))
def waitForNodeToLaunch(
    gameNodeId: String,
    root: dom.Element,
    onDone: => Unit
) =
  if dom.document.getElementById(gameNodeId) != null then onDone
  else
    dom
      .MutationObserver { (_, observer) =>
        if dom.document.getElementById(gameNodeId) != null then
          observer.disconnect()
          onDone
      }
      .observe(
        root,
        new dom.MutationObserverInit:
          subtree = true
          childList = true
      )

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
          waitForNodeToLaunch(
            gameNodeId = gameDivId,
            root = dom.document.body,
            onDone = Tetrio(model.bridge.subSystem(IndigoGameId(gameDivId)))
              .launch(gameDivId)
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

    case Msg.Resize(entries) => 
      println("entries")
        
      (
        model,
        Cmd.None
      )

  def view(model: Model): Html[Msg] =
    div(`class` := "main")(
      div(`class` := "game", id := gameDivId)(),
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

    // val resizer = resize[IO]

    Sub.Batch[IO, Msg](
      indigoBridge,
      resize[IO].map((entries, _) => Msg.Resize(entries) )
    )

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

type Entries = js.Array[dom.ResizeObserverEntry]
type ResizeParams = (Entries, dom.ResizeObserver)

def resize[F[_]: Sync]: Sub[F, ResizeParams] = 
  resize("[resize-observer]")
def resize[F[_]: Sync](id: String): Sub[F, ResizeParams] =
  val acquire
      : F[(Either[Throwable, ResizeParams] => Unit) => dom.ResizeObserver] =
    Sync[F].delay { (cb: Either[Throwable, ResizeParams] => Unit) =>
      println("ResizeObserverInit")
      dom.ResizeObserver {(entries, params) => 
        println("resize")
        cb(Right((entries, params)))
      }
    }

  val release: F[dom.ResizeObserver => F[Option[F[Unit]]]] =
    Sync[F].delay { observer =>
      Sync[F].delay {
        Option(
          Sync[F].delay {
            observer.disconnect()
          }
        )
      }
    }

  val task =
    for
      a <- acquire
      r <- release
    yield a andThen r

  Sub.Observe(id, task)
