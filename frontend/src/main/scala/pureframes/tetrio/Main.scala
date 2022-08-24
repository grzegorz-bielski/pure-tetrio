package pureframes.tetrio

import cats.effect.IO
import cats.effect.kernel.Sync
import cats.syntax.all.*
import indigo.shared.collections.NonEmptyBatch
import org.scalajs.dom
import org.scalajs.dom.*
import org.scalajs.dom.document
import pureframes.tetrio.game.*
import pureframes.tetrio.game.core.*
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
  case GameNodeMounted(e: Element)
  case Resize(entries: NonEmptyBatch[ResizeObserverEntry])
  case UpdateProgress(progress: Progress, inProgress: Boolean)

@JSExportTopLevel("TyrianApp")
object Main extends TyrianApp[Msg, Model]:
  val gameNodeId = "game-container"

  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    (Model.init, Cmd.Emit(Msg.StartGame))

  @SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
    case Msg.StartGame =>
      (
        model,
        Cmd.Run(
          waitForNodeToMount[IO, Msg, Element](gameNodeId, dom.document.body)
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
    case Msg.GameNodeMounted(gameNode) =>
      (
        model.copy(
          gameNode = Some(gameNode)
        ),
        Cmd.SideEffect {
          // TODO: use node directly after new Indigo release
          Tetrio(model.bridge.subSystem(IndigoGameId(gameNodeId)))
            .launch(
              gameNodeId,
              // TODO: why this have to be unsafe from the Scala side?
              "width"  -> gameNode.clientWidth.toString,
              "height" -> gameNode.clientHeight.toString
            )
        }
      )

    case Msg.Resize(entries) =>
      (
        model,
        Cmd.SideEffect {
          model.gameNode
            .mapNullable(_.firstChild.asInstanceOf[HTMLCanvasElement])
            .foreach { canvas =>
              val canvasSize = CanvasSize.unsafFromResizeEntry(entries.head)

              println("canvasSize" -> canvasSize)

              canvas.width = canvasSize.drawingBufferWidth
              canvas.height = canvasSize.drawingBufferHeight
            }
        }
      )

    case Msg.Noop => (model, Cmd.None)

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

    val resizer =
      resizeAwaitNode[IO, Msg, HTMLCanvasElement](gameNodeId, dom.document.body)
        .map { (entries, _) =>
          Msg.Resize(entries)
        }

    indigoBridge combine resizer

case class Model(
    bridge: TyrianIndigoBridge[IO, ExternalCommand],
    gameInProgress: Boolean,
    gameProgress: Option[Progress],
    gameNode: Option[Element]
)
object Model:
  val init: Model = Model(
    bridge = TyrianIndigoBridge(),
    gameInProgress = false,
    gameProgress = None,
    gameNode = None
  )

extension [A](underlying: Option[A])
  // fp-ts like method
  def mapNullable[B <: A](fn: A => B): Option[B] =
    underlying.flatMap(a => Option(fn(a)))
