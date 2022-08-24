package pureframes.tetrio.game.core

import indigo.shared.config.GameViewport
import org.scalajs.dom
import org.scalajs.dom.*

import scala.scalajs.js
import scala.scalajs.js.annotation.*

/** Calcs drawing buffer size. Display size is controlled by the CSS
  * @param displayWidth
  * @param displayHeight
  * @param dpr
  *   device pixel ratio. 1 for most displays, 2 for retina-like
  */
case class CanvasSize(width: Double, height: Double, dpr: Double):
  val drawingBufferWidth  = displaySize(width)
  val drawingBufferHeight = displaySize(height)

  def toViewport: GameViewport = 
    GameViewport(drawingBufferWidth, drawingBufferHeight)

  private def displaySize(size: Double): Int = math.round(size * dpr).toInt

object CanvasSize:
  def unsafeFromClientSizes(width: Int, heigt: Int): CanvasSize =
    CanvasSize(
      width,
      heigt,
      window.devicePixelRatio
    )

  def unsafFromResizeEntry(entry: ResizeObserverEntry): CanvasSize =
    // TODO: no entry.devicePixelContentBoxSize on scala-js dom

    // CanvasSize(
    //   entry.devicePixelContentBoxSize.head.inlineSize,
    //   entry.devicePixelContentBoxSize.head.blockSize,
    //   1
    // )

    if !js.isUndefined(entry.contentBoxSize.head) then
      CanvasSize(
        entry.contentBoxSize.head.inlineSize,
        entry.contentBoxSize.head.blockSize,
        window.devicePixelRatio
      )
    else
      CanvasSize(
        entry.contentRect.width,
        entry.contentRect.height,
        window.devicePixelRatio
      )
