package pureframes.tetrio.ui

import cats.Monad
import cats.effect.IO
import cats.effect.kernel.Async
import cats.effect.kernel.Sync
import cats.syntax.all.*
import indigo.shared.collections.NonEmptyBatch
import org.scalajs.dom.*
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
  def resize[F[_]: Sync](nodeId: String): Sub[F, ResizeParams] =
    given ResizeObserverOptions = defaultResizeOptions

    resize(
      id = s"<ResizeObserver-$nodeId>",
      node = document.getElementById(nodeId)
    )

  def resize[F[_]: Sync](
      id: String,
      node: => Element
  )(using ResizeObserverOptions): Sub[F, ResizeParams] =
    Sub.make[F, ResizeParams, ResizeObserver](id) { cb =>
      Sync[F].delay(unafeResizeObserver(node, cb))

    }(ob => Sync[F].delay(ob.disconnect()))

  def resizeAwaitNode[F[_]: Async, Msg](
      nodeId: String,
      root: => Element
  ): Sub[F, ResizeParams] =
    given MutationObserverInit  = defaultMutationOptions
    given ResizeObserverOptions = defaultResizeOptions

    resizeAwaitNode(
      id = s"AwaitResizeObserver-$nodeId",
      node = document.getElementById(nodeId),
      root = root
    )

  def resizeAwaitNode[F[_]: Async, Msg](
      id: String,
      node: => Element,
      root: => Element
  )(using
      MutationObserverInit,
      ResizeObserverOptions
  ): Sub[F, ResizeParams] =
    Sub.make[F, ResizeParams, ResizeObserver](id) { cb =>
      waitForNodeToMount(node, root) *>
        Sync[F].delay(unafeResizeObserver(node, cb))
    }(ob => Sync[F].delay(ob.disconnect()))

  def waitForNodeToMount[F[_]: Async, Msg](
      nodeId: String,
      root: => Element
  ): F[Element] =
    given MutationObserverInit = defaultMutationOptions

    waitForNodeToMount(
      document.getElementById(nodeId),
      root
    )

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def waitForNodeToMount[F[_]: Async, Msg](node: => Element, root: => Element)(
      using MutationObserverInit
  ): F[Element] =
    Async[F].async[Element] { cb =>
      Sync[F].delay {
        if node != null then
          cb(Right(node))
          None
        else
          val ob = MutationObserver { (_, observer) =>
            if node != null then
              observer.disconnect()
              cb(Right(node))
          }

          ob.observe(root, summon[MutationObserverInit])

          Some(Sync[F].delay(ob.disconnect()))
      }
    }

  type AsyncCb[A] = Either[Throwable, A] => Unit
  type ResizeParams = (NonEmptyBatch[ResizeObserverEntry], ResizeObserver)

  val defaultMutationOptions: MutationObserverInit =
    new MutationObserverInit:
      subtree = true
      childList = true

  val defaultResizeOptions: ResizeObserverOptions =
    new ResizeObserverOptions:
      box = ResizeObserverBoxOption.`content-box`

  private def unafeResizeObserver(
      node: => Element,
      cb: AsyncCb[ResizeParams]
  )(using ResizeObserverOptions) =
    val ob = ResizeObserver { (entries, params) =>
      if !entries.isEmpty then 
        cb(Right((NonEmptyBatch.point(entries.head), params)))
    }
    ob.observe(node, summon[ResizeObserverOptions])
    ob
