package pureframes.tetrio.game.core

import indigo.shared.Outcome
import indigo.shared.config.GameViewport
import indigo.shared.datatypes.Vector2
import indigoextras.geometry.Polygon
import org.scalajs.dom
import org.scalajs.dom.*

import scala.scalajs.js
import scala.scalajs.js.annotation.*

/** Calcs drawing buffer size. Display size is controlled by the CSS
  * @param displayWidth
  *   game container width in CSS pixels
  * @param displayHeight
  *   game container height in CSS pixels
  * @param dpr
  *   device pixel ratio. 1 for most displays, 2 for retina-like
  */
case class CanvasSize(displayWidth: Double, displayHeight: Double, dpr: Double):
  val drawingBufferWidth: Int  = drawingBufferSize(displayWidth)
  val drawingBufferHeight: Int = drawingBufferSize(displayHeight)

  def scale: Vector2 =
    val baseScale = if displayWidth < 400 then Vector2(0.5) else Vector2(0.75)
    Vector2(dpr) * baseScale

  def toDrawingBufferViewport: GameViewport =
    GameViewport(drawingBufferWidth, drawingBufferHeight)

  def toPolygon: Polygon.Closed =
    Polygon.fromRectangle(toDrawingBufferViewport.toRectangle)

  private def drawingBufferSize(size: Double): Int =
    math.round(size * dpr).toInt

object CanvasSize:
  def fromClientSize(width: Int, heigth: Int): Outcome[CanvasSize] =
    Outcome(unsafeFromClientSizes(width, heigth))

  def unsafeFromClientSizes(width: Int, heigt: Int): CanvasSize =
    CanvasSize(
      width,
      heigt,
      window.devicePixelRatio
    )

  def unsafFromResizeEntry(entry: ResizeObserverEntry): CanvasSize =
    // TODO: no entry.devicePixelContentBoxSize on scala-js dom :sad

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
