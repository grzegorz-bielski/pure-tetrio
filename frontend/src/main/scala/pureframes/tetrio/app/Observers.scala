package pureframes.tetrio.app

import cats.effect.kernel.Async
import cats.effect.kernel.Sync
import cats.syntax.all.*
import indigo.shared.collections.NonEmptyBatch
import org.scalajs.dom.*
import tyrian.Html.*
import tyrian.*
import tyrian.cmds.*
import cats.effect.kernel.Resource

object Observers:
  def resize[F[_]: Sync](nodeId: String): Sub[F, ResizeParams] =
    given ResizeObserverOptions = defaultResizeOptions

    resize(
      id = s"<ResizeObserver-$nodeId>",
      node = document.getElementById(nodeId)
    )

  // TODO: add debounce
  def resize[F[_]: Sync, E <: Element](
      id: String,
      node: => E
  )(using ResizeObserverOptions): Sub[F, ResizeParams] =
    Sub.make[F, ResizeParams, ResizeObserver](id) { cb =>
      Sync[F].delay(unafeResizeObserver(node, cb))

    }(ob => Sync[F].delay(ob.disconnect()))

  @SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
  def resizeAwaitNode[F[_]: Async, Msg, Node <: Element](
      nodeId: String,
      root: => Element
  ): Sub[F, ResizeParams] =
    given MutationObserverInit  = defaultMutationOptions
    given ResizeObserverOptions = defaultResizeOptions

    resizeAwaitNode[F, Msg, Node](
      id = s"AwaitResizeObserver-$nodeId",
      // TODO: asInstanceOf are ugly, use custom DOM wrapper ?
      node = document.getElementById(nodeId).asInstanceOf[Node],
      root = root
    )

  def resizeAwaitNode[F[_]: Async, Msg, Node <: Element](
      id: String,
      node: => Node,
      root: => Element
  )(using
      MutationObserverInit,
      ResizeObserverOptions
  ): Sub[F, ResizeParams] =
    Sub.make[F, ResizeParams, ResizeObserver](id) { cb =>
      waitForNodeToMount(node, root) *>
        Sync[F].delay(unafeResizeObserver(node, cb))
    }(ob => Sync[F].delay(ob.disconnect()))

  @SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
  def waitForNodeToMount[F[_]: Async, Msg, Node <: Element](
      nodeId: String,
      root: => Element
  ): F[Node] =
    given MutationObserverInit = defaultMutationOptions

    waitForNodeToMount[F, Msg, Node](
      document.getElementById(nodeId).asInstanceOf[Node],
      root
    )

  // TODO: cancel on timeout if node is not found
  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def waitForNodeToMount[F[_]: Async, Msg,  Node <: Element](node: => Node, root: => Element)(
      using MutationObserverInit
  ): F[Node] =
    Async[F].async[Node] { cb =>
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
    new:
      subtree = true
      childList = true

  val defaultResizeOptions: ResizeObserverOptions =
    new:
      box = ResizeObserverBoxOption.`content-box`

  private def unafeResizeObserver[E <: Element](
      node: => E,
      cb: AsyncCb[ResizeParams]
  )(using ResizeObserverOptions) =
    val ob = ResizeObserver { (entries, params) =>
      if !entries.isEmpty then 
        cb(Right((NonEmptyBatch.point(entries.head), params)))
    }
    ob.observe(node, summon[ResizeObserverOptions])
    ob


  def resizeObserver[E <: Element, F[_]: Async](
      node: => E,
      cb: AsyncCb[ResizeParams]
  )(using ResizeObserverOptions): Resource[F, ResizeObserver] =
    Resource.make(
      Sync[F].delay {
        val ob = ResizeObserver { (entries, params) =>
          if !entries.isEmpty then
            cb(Right((NonEmptyBatch.point(entries.head), params)))
        }
        ob.observe(node, summon[ResizeObserverOptions])
        ob
      }
    )(ob => Sync[F].delay(ob.disconnect()))

// TODO: update cats-effect
// https://fs2.io/#/guide?id=asynchronous-effects-callbacks-invoked-multiple-times