package pureframes.tetrio.app

// import cats.effect.IO
import cats.effect.kernel.Async
import cats.effect.kernel.Sync
import cats.syntax.all.*
import indigo.shared.collections.NonEmptyBatch
import org.scalajs.dom
import org.scalajs.dom.*
import org.scalajs.dom.document
import pureframes.tetrio.app.Observers.*
import pureframes.tetrio.game.Tetrio.*
import pureframes.tetrio.game.*
import pureframes.tetrio.game.core.*
import pureframes.tetrio.game.scenes.gameplay.model.Progress
import tyrian.*
import tyrian.cmds.*

case class AppModel[F[_]: Async](
    bridge: TyrianIndigoBridge[F, ExternalCommand],
    gameInProgress: Boolean,
    gameProgress: Option[Progress],
    gameNode: Option[Element],
    controls: Controls.Model
):
  @SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
  val update: AppMsg => (AppModel[F], Cmd[F, AppMsg]) =
    case AppMsg.StartGame =>
      (
        this,
        Cmd.Run(
          waitForNodeToMount[F, AppMsg, Element](gameNodeId, dom.document.body)
        )(AppMsg.GameNodeMounted(_))
      )

    case AppMsg.Pause =>
      (
        this,
        bridge.publish(IndigoGameId(gameNodeId), ExternalCommand.Pause)
      )
    case AppMsg.UpdateProgress(progress, inProgress) =>
      (
        copy(
          gameProgress = Some(progress),
          gameInProgress = inProgress
        ),
        Cmd.None
      )

    case AppMsg.ControlsUpdate(msg) =>
      (
        copy(
          controls = Controls.update(msg, controls)
        ),
        Cmd.None
      )

    case AppMsg.GameNodeMounted(gameNode) =>
      (
        copy(
          gameNode = Some(gameNode)
        ),
        Cmd.SideEffect {
          // TODO: use node directly after new Indigo release
          Tetrio(bridge.subSystem(IndigoGameId(gameNodeId)))
            .launch(
              gameNodeId,
              // TODO: why this have to be unsafe from the Scala side?
              "width"  -> gameNode.clientWidth.toString,
              "height" -> gameNode.clientHeight.toString
            )
        }
      )

    case AppMsg.Resize(canvasSize) =>
      (
        this,
        Cmd.merge(
          Cmd.SideEffect {
            gameNode
              .mapNullable(_.firstChild.asInstanceOf[HTMLCanvasElement])
              .foreach { canvas =>
                canvas.width = canvasSize.drawingBufferWidth
                canvas.height = canvasSize.drawingBufferHeight
              }
          },
          bridge.publish(
            IndigoGameId(gameNodeId),
            ExternalCommand.CanvasResize(canvasSize)
          )
        )
      )

    case AppMsg.Noop => (this, Cmd.None)

object AppModel:
  def init[F[_]: Async]: AppModel[F] = AppModel[F](
    bridge = TyrianIndigoBridge[F, ExternalCommand](),
    gameInProgress = false,
    gameProgress = None,
    gameNode = None,
    controls = Controls.init
  )

extension [A](underlying: Option[A])
  // fp-ts like method
  def mapNullable[B <: A](fn: A => B): Option[B] =
    underlying.flatMap(a => Option(fn(a)))
