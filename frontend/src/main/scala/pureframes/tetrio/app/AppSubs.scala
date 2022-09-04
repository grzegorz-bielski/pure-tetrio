package pureframes.tetrio

import cats.effect.IO
import cats.effect.kernel.Async
import cats.syntax.all.*
import indigo.shared.collections.NonEmptyBatch
import org.scalajs.dom
import org.scalajs.dom.*
import org.scalajs.dom.document
import pureframes.tetrio.app.*
import pureframes.tetrio.game.Tetrio.*
import pureframes.tetrio.game.*
import pureframes.tetrio.game.core.*
import tyrian.Html.*
import tyrian.*
import tyrian.cmds.*

import scala.scalajs.js
import scala.scalajs.js.annotation.*

object AppSubs:
  def all[F[_]: Async](model: AppModel[F]): Sub[F, AppMsg] =
    val indigoBridge = model.bridge.subscribe {
      case m: ExternalCommand.UpdateProgress =>
        Some(AppMsg.UpdateProgress(m.progress, m.inProgress))
      case _ => None
    }

    val resizer =
      // TODO: we also need to listen for dpr changes and debounce this whole mess
      Observers
        .resizeAwaitNode[F, AppMsg, HTMLCanvasElement](
          gameNodeId,
          dom.document.body
        )
        .map { (entries, _) =>
          // TODO: this should be in suspended and handled in flatMap...
          val size = CanvasSize.unsafFromResizeEntry(entries.head)

          AppMsg.Resize(size)
        }

    indigoBridge combine resizer
