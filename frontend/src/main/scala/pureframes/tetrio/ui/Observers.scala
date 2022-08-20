package pureframes.tetrio.ui

import cats.Monad
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

object Observers:
  type ResizeParams = (js.Array[dom.ResizeObserverEntry], dom.ResizeObserver)

  // TODO: needs waitForNodeToLaunch
  def resize[F[_]: Sync](nodeId: String): Sub[F, ResizeParams] =
    resize(
      s"<ResizeObserver-$nodeId>",
      new dom.ResizeObserverOptions:
        box = dom.ResizeObserverBoxOption.`content-box`
      ,
      dom.document.getElementById(nodeId)
    )

  def resize[F[_]: Sync](
      id: String,
      options: dom.ResizeObserverOptions,
      node: => dom.Element
  ): Sub[F, ResizeParams] =
    Sub.make[F, ResizeParams, dom.ResizeObserver](id) { cb =>
      Sync[F].delay {
        val observer = dom.ResizeObserver { (entries, params) =>
          cb(Right((entries, params)))
        }

        observer.observe(node, options)
        observer
      }
    }(observer => Sync[F].delay(observer.disconnect()))

  // TODO: make generic + Sub version ?
  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def waitForNodeToLaunch[F[_]: Sync, Msg](
      gameNodeId: String,
      root: => dom.Element,
      onDone: => Unit
  ): Cmd[F, Msg] =
    Cmd.SideEffect {
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
    }
