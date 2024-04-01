package indigoextras.gestures

import indigo.*
import indigo.shared.FrameContext
import indigo.shared.Outcome
import indigo.shared.collections.Batch
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.events.GlobalEvent
import indigo.shared.events.PointerEvent
import indigo.shared.events.PointerEvent.*
import indigo.shared.time.GameTime
import indigo.shared.time.Millis
import indigo.shared.time.Seconds

import SwipeGestureArea.*

final case class SwipeGestureArea private (
    area: Polygon.Closed,
    handler: Handler,
    state: State,
    options: Options
) derives CanEqual:
  def resize(nextArea: Polygon.Closed): SwipeGestureArea = 
    copy(area = nextArea)
  def update(e: PointerEvent, ctx: FrameContext[?]): Outcome[SwipeGestureArea] =
    update(e, ctx.gameTime)
  def update(e: PointerEvent, time: GameTime): Outcome[SwipeGestureArea] =
    val fn: PartialFunction[PointerEvent, Outcome[State]] =
      state match
        case State.Initial => {
          case e: PointerDown if e.isFrom(area) =>
            Outcome(State.Moving(e.position, time.running))
        }
        case s: State.Moving => {
          case e: PointerMove if e.isFrom(area) =>
            val Point(x1, y1) = e.position
            val Point(x2, y2) = s.startPos
            val dx            = x2 - x1
            val dy            = y2 - y1
            val deltaX        = math.sqrt((dx * dx) + (dy * dy))
            val deltaT        = (time.running - s.startAt).toMillis.toDouble
            val velocity      = deltaX / deltaT

            if velocity >= options.velocity then
              val direction =
                math.abs(dx) > math.abs(dy) match
                  case true =>
                    if dx > 0 then Direction.Left else Direction.Right
                  case false =>
                    if dy > 0 then Direction.Up else Direction.Down

              Outcome(State.Initial).addGlobalEvents(handler(direction))
            else Outcome(s)

          case e: PointerUp if e.isFrom(area) =>
            Outcome(State.Initial)
        }

    fn
      .andThen(_.map(s => copy(state = s)))
      .applyOrElse(e, _ => Outcome(this))

object SwipeGestureArea:
  /** @param velocity
    *   minimum velocity in [px/ms] for recognizing the gesture
    */
  final case class Options(velocity: Double)
  object Options:
    val default = Options(velocity = 0.3)

  def apply(
      area: Polygon.Closed,
      options: Options,
      onSwipe: (Direction => GlobalEvent)*
  ): SwipeGestureArea =
    val onSwipes    = Batch.fromSeq(onSwipe)
    val fn: Handler = direction => onSwipes.map(_(direction))

    SwipeGestureArea(area, fn, State.Initial, options)

  def apply(
      area: Polygon.Closed,
      onSwipe: (Direction => GlobalEvent)*
  ): SwipeGestureArea =
    apply(area, Options.default, onSwipe*)

  def apply(area: Polygon.Closed): SwipeGestureArea =
    apply(area, Options.default, GestureEvent.Swiped(_))

  type Handler = Direction => Batch[GlobalEvent]

  enum State:
    case Initial
    case Moving(startPos: Point, startAt: Seconds)

  extension (e: PointerEvent)
    def isFrom(polygon: Polygon): Boolean =
      polygon.contains(Vertex.fromPoint(e.position))
