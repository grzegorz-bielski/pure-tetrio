package pureframes.tetrio.game.scenes.gameplay.viewmodel

import indigo.shared.FrameContext
import indigo.shared.Outcome
import indigo.shared.collections.Batch
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.events.GlobalEvent
import indigo.shared.events.PointerEvent
import indigo.shared.input.Pointers
import indigo.shared.time.GameTime
import indigo.shared.time.Millis
import indigo.shared.time.Seconds
import indigoextras.geometry.Polygon
import indigoextras.geometry.Vertex
import ultraviolet.datatypes.ShaderAST.DataTypes.external

import TapGestureArea.*

final case class TapGestureArea(
    area: Polygon.Closed,
    handler: Handler,
    state: State
) derives CanEqual:
  def update(using ctx: FrameContext[?]): Outcome[TapGestureArea] =
    update(ctx.inputState.pointers, ctx.gameTime)
  def update(pointers: Pointers, time: GameTime): Outcome[TapGestureArea] =
    val fn: PartialFunction[PointerEvent, Outcome[State]] =
      state match
        case state: State.Initial.type => {
          case e: PointerEvent.PointerDown if e.isFrom(area) =>
            Outcome(State.AwaitingUp(e, time.running))
        }
        case state @ State.AwaitingUp(downEvent, downAt) => {
          case e: PointerEvent.PointerUp if e.isFrom(area) =>
            val onTime = time.running < downAt + TimeBuffer
            val withoutTravel =
              e.position.distanceTo(downEvent.position) <= PointerTravelBuffer

            if onTime && withoutTravel then
              Outcome(State.Initial).addGlobalEvents(handler())
            else Outcome(State.Initial)
        }

    pointers.pointerEvents
      .collectFirst(fn)
      .getOrElse(Outcome(state))
      .map(s => copy(state = s))

object TapGestureArea:
  def apply(
      area: Polygon.Closed,
      onTap: GlobalEvent*
  ): TapGestureArea =
    val events = Batch.fromSeq(onTap)
    TapGestureArea(area, () => events, State.Initial)

  type Handler = () => Batch[GlobalEvent]

  val TimeBuffer          = Seconds(0.25)
  val PointerTravelBuffer = 10

  enum State:
    case Initial
    case AwaitingUp(down: PointerEvent.PointerDown, running: Seconds)

  extension (e: PointerEvent)
    def isFrom(polygon: Polygon): Boolean =
      polygon.contains(Vertex.fromPoint(e.position))
