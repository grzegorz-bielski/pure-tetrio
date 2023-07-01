package pureframes.tetrio.app

import cats.effect.kernel.Async
import cats.effect.kernel.Sync
import cats.syntax.all.*
import indigo.shared.collections.NonEmptyBatch
import org.scalajs.dom
import org.scalajs.dom.*
import org.scalajs.dom.document
import pureframes.tetrio.app.Observers.*
import pureframes.tetrio.app.components.*
import pureframes.tetrio.game.Tetrio.*
import pureframes.tetrio.game.*
import pureframes.tetrio.game.core.*
import pureframes.tetrio.game.scenes.gameplay.GameState
import pureframes.tetrio.game.scenes.gameplay.model.Progress
import tyrian.*
import tyrian.cmds.*


case class AppModel[F[_]: Async](
    bridge: TyrianIndigoBridge[F, ExternalMsg],
    gameState: GameState,
    gameProgress: Option[Progress],
    gameNode: Option[Element],
    view: RouterView,
    gameInstance: Option[Tetrio[F]]
):
  @SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
  val update: AppMsg => (AppModel[F], Cmd[F, AppMsg]) =
    case AppMsg.StartGame =>
      (
        this.copy(
          gameInstance = Some(Tetrio(bridge.subSystem(IndigoGameId(gameNodeId))))
        ),
        Cmd.Run(
          waitForNodeToMount[F, AppMsg, Element](gameNodeId, dom.document.body)
        )(AppMsg.GameNodeMounted(_))
      )

    case AppMsg.StopGame => 
      (
        this.copy(gameInstance = None),
        (
          gameNode.mapNullable(_.firstChild.asInstanceOf[HTMLCanvasElement]), 
          gameInstance
        )
          .mapN: (node, instance) => 
            Cmd.SideEffect:
              instance.halt()
              node.remove()
          .getOrElse(Cmd.None)
      )

    case AppMsg.Pause =>
      (
        this,
        bridge.publish(IndigoGameId(gameNodeId), ExternalCommand.Pause)
      )
    case AppMsg.UpdateProgress(gameState, gameProgress) =>
      (
        copy(
          gameState = gameState,
          gameProgress =  gameProgress.orElse(this.gameProgress),
        ),
        (this.gameState, gameState) match
          case GameState.InProgress -> GameState.Paused     => PauseMenu.show[F]
          case GameState.Paused     -> GameState.InProgress => PauseMenu.hide[F]
          case _                                            => Cmd.None
      )

    case AppMsg.GameNodeMounted(gameNode) =>
      (
        copy(
          gameNode = Some(gameNode)
        ),
        Cmd.SideEffect:
          gameInstance.foreach:
            _.launch(
              gameNodeId,
              // TODO: why this have to be unsafe from the Scala side?
              "width"  -> gameNode.clientWidth.toString,
              "height" -> gameNode.clientHeight.toString
            )
      )

    case AppMsg.Resize(canvasSize) =>
      (
        this,
        Cmd.merge(
          Cmd.SideEffect {
            gameNode
              .mapNullable(_.firstChild.asInstanceOf[HTMLCanvasElement])
              .foreach: canvas =>
                canvas.width = canvasSize.drawingBufferWidth
                canvas.height = canvasSize.drawingBufferHeight
          },
          bridge.publish(
            IndigoGameId(gameNodeId),
            ExternalCommand.CanvasResize(canvasSize)
          )
        )
      )


    case AppMsg.Input(cmd) =>
      (
        this,
        bridge.publish(IndigoGameId(gameNodeId), ExternalCommand.Input(cmd))
      )

    case AppMsg.Noop => (this, Cmd.None)

    case AppMsg.FollowLink(href, isExternal) => 
      // see: https://github.com/PurpleKingdomGames/tyrian/pull/195#issuecomment-1564979780
      if isExternal then (this, Nav.loadUrl(href)) else
        href match 
          case RouterView(view) => 
            (
              this.copy(view = view), 
                (
                  view match 
                    case view @ RouterView.Home =>  Cmd.Emit(AppMsg.StopGame)
                    case view @ RouterView.Game =>  Cmd.Emit(AppMsg.StartGame)
                ) |+| Nav.pushUrl(view.fullPath)
            )
            
          case _ => (this, Cmd.None)

object AppModel:
  def init[F[_]: Async]: AppModel[F] = 
    val bridge = TyrianIndigoBridge[F, ExternalMsg]()

    AppModel[F](
      bridge = bridge,
      gameState = GameState.UnStarted,
      gameProgress = None,
      gameNode = None,
      view = RouterView.Home,
      gameInstance = None
    )

extension [A](underlying: Option[A])
  // fp-ts like method
  def mapNullable[B <: A](fn: A => B): Option[B] =
    underlying.flatMap(a => Option(fn(a)))
