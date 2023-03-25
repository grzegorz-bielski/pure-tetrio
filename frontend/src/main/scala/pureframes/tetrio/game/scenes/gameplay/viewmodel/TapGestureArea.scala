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

import TapGestureArea.*

final case class TapGestureArea private (
    area: Polygon.Closed,
    handler: Handler,
    state: State,
    options: Options
) derives CanEqual:
  def update(using ctx: FrameContext[?]): Outcome[TapGestureArea] =
    update(ctx.inputState.pointers, ctx.gameTime)
  def update(pointers: Pointers, time: GameTime): Outcome[TapGestureArea] =
    val fn: PartialFunction[PointerEvent, Outcome[State]] =
      state match
        case State.Initial => {
          case e: PointerDown if e.isFrom(area) =>
            Outcome(State.AwaitingUp(e, time.running, 0))
        }
        case s: State.Tapped => {
          case e: PointerDown if e.isFrom(area) =>
            val tapCount =
              if time.running < s.upAt + options.tapInterval then s.tapCount
              else 0

            Outcome(State.AwaitingUp(e, time.running, tapCount))
        }
        case s: State.AwaitingUp => {
          case e: PointerUp if e.isFrom(area) =>
            val onTime = time.running < s.downAt + options.time
            val withoutTravel =
              e.position.distanceTo(s.down.position) <= options.pointerTravel

            if onTime && withoutTravel then
              val tapCount = s.tapCount + 1
              Outcome(State.Tapped(time.running, tapCount))
                .addGlobalEvents(handler(tapCount))
            else Outcome(State.Initial)
        }

    pointers.pointerEvents
      .collectFirst(fn)
      .getOrElse(Outcome(state))
      .map(s => copy(state = s))

object TapGestureArea:
  /** @param time
    *   maximum press time
    * @param tapInterval
    *   maximum amount of time between multiple taps
    * @param pointerTravel
    *   maximum pointer travel distance between press and release in px
    */
  final case class Options(
      time: Seconds,
      tapInterval: Seconds,
      pointerTravel: Int
  )
  object Options:
    val default = Options(
      time = Seconds(0.25),
      tapInterval = Seconds(0.30),
      pointerTravel = 10
    )

  def apply(
      area: Polygon.Closed,
      options: Options,
      onTap: (TapCount => GlobalEvent)*
  ): TapGestureArea =
    val onTaps      = Batch.fromSeq(onTap)
    val fn: Handler = tapCount => onTaps.map(_(tapCount))

    TapGestureArea(area, fn, State.Initial, options)

  def apply(
      area: Polygon.Closed,
      onTap: (TapCount => GlobalEvent)*
  ): TapGestureArea =
    apply(area, Options.default, onTap*)

  type TapCount = Int
  type Handler = TapCount => Batch[GlobalEvent]

  enum State:
    case Initial
    case AwaitingUp(down: PointerDown, downAt: Seconds, tapCount: TapCount)
    case Tapped(upAt: Seconds, tapCount: TapCount)

  extension (e: PointerEvent)
    def isFrom(polygon: Polygon): Boolean =
      polygon.contains(Vertex.fromPoint(e.position))
