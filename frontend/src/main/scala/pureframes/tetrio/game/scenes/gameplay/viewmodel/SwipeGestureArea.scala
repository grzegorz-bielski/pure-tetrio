package pureframes.tetrio.game.scenes.gameplay.viewmodel

import indigo.shared.FrameContext
import indigo.shared.Outcome
import indigo.shared.collections.Batch
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.events.GlobalEvent
import indigo.shared.events.PointerEvent
import indigo.shared.events.PointerEvent.*
import indigo.shared.input.Pointers
import indigo.shared.time.GameTime
import indigo.shared.time.Millis
import indigo.shared.time.Seconds
import indigoextras.geometry.Polygon
import indigoextras.geometry.Vertex

import SwipeGestureArea.*

final case class SwipeGestureArea private (
    area: Polygon.Closed,
    handler: Handler,
    state: State,
    options: Options
) derives CanEqual:
  def update(using ctx: FrameContext[?]): Outcome[SwipeGestureArea] =
    update(ctx.inputState.pointers, ctx.gameTime)
  def update(pointers: Pointers, time: GameTime): Outcome[SwipeGestureArea] =
    val fn: PartialFunction[PointerEvent, Outcome[State]] =
      state match
        case State.Initial => {
          case e: PointerDown if e.isFrom(area) =>
            Outcome(State.Moving(e, time.running))
        }
        case s: State.Moving => {
          case e: PointerMove if e.isFrom(area) =>
            val Point(x1, y1) = e.position
            val Point(x2, y2) = s.start.position
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

    pointers.pointerEvents
      .collectFirst(fn)
      .getOrElse(Outcome(state))
      .map(s => copy(state = s))

object SwipeGestureArea:
  /** @param velocity
    *   minimum velocity in [px/ms] for recognizing the gesture
    */
  final case class Options(velocity: Double)
  object Options:
    val default = Options(
      velocity = 0.3
    )

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

  enum Direction:
    case Up, Down, Left, Right

  type Handler = Direction => Batch[GlobalEvent]

  enum State:
    case Initial
    case Moving(start: PointerDown, startAt: Seconds)

  extension (e: PointerEvent)
    def isFrom(polygon: Polygon): Boolean =
      polygon.contains(Vertex.fromPoint(e.position))
