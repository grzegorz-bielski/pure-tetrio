package pureframes.tetrio.game.scenes.gameplay.model

import indigo.IndigoLogger.*
import indigo.*
import indigo.scenes.*
import indigo.shared.datatypes.Vector2
import indigo.shared.events.KeyboardEvent.KeyDown
import indigo.shared.events.PointerEvent
import indigo.shared.utils.Lens
import indigoextras.geometry.Polygon
import indigoextras.gestures.*
import pureframes.tetrio.game.core.*
import pureframes.tetrio.game.scenes.gameplay.*
import pureframes.tetrio.game.scenes.gameplay.model.*

import scala.collection.immutable.Queue

import GameplayCommand.*
import RotationDirection.*

final case class GameplayInput(
    spawnPoint: Vector2,
    cmds: Queue[GameplayCommand],
    tapGestureArea: TapGestureArea,
    swipeGestureArea: SwipeGestureArea,
    panGestureArea: PanGestureArea
):
  def onCanvasResize(nextCanvasSize: CanvasSize): GameplayInput =
    val gestureArea = nextCanvasSize.toPolygon

    copy(
      tapGestureArea = tapGestureArea.resize(gestureArea),
      swipeGestureArea = swipeGestureArea.resize(gestureArea),
      panGestureArea = panGestureArea.resize(gestureArea)
    )

  def appendCmd(cmd: GameplayCommand): GameplayInput =
    copy(cmds = cmds :+ cmd)

  def onFrameEnd: GameplayInput =
    copy(cmds = Queue.empty[GameplayCommand])

  def onGesture(e: GestureEvent): Outcome[GameplayInput] =
    e match
      case GestureEvent.Tapped(_) =>
        Outcome(appendCmd(GameplayCommand.Rotate(RotationDirection.Clockwise)))
      case GestureEvent.Swiped(Direction.Up) =>
        Outcome(appendCmd(GameplayCommand.SwapHeld))
      case GestureEvent.Swiped(Direction.Down) =>
        Outcome(appendCmd(GameplayCommand.HardDrop))
      case GestureEvent.Panned(Direction.Left) =>
        Outcome(appendCmd(GameplayCommand.MoveLeft))
      case GestureEvent.Panned(Direction.Right) =>
        Outcome(appendCmd(GameplayCommand.MoveRight))
      case GestureEvent.Panned(Direction.Down) =>
        Outcome(appendCmd(GameplayCommand.MoveDown))
      case _ =>
        Outcome(this)

  def onInput(e: InputEvent, ctx: GameContext): Outcome[GameplayInput] =
    e match
      case KeyDown(key)    => onKeyDown(key)
      case e: PointerEvent => onPointerEvent(e, ctx)
      case _               => Outcome(this)

  private def onKeyDown(key: Key): Outcome[GameplayInput] =
    Outcome(copy(cmds = produceKeyboardCommands(key)))

  private def onPointerEvent(
      e: PointerEvent,
      ctx: GameContext
  ): Outcome[GameplayInput] =
    val gestureAreas =
      (
        tapGestureArea.update(e, ctx),
        swipeGestureArea.update(e, ctx),
        panGestureArea.update(e, ctx)
      ).combine

    gestureAreas.map { (tg, sg, pg) =>
      copy(tapGestureArea = tg, swipeGestureArea = sg, panGestureArea = pg)
    }

  def isMoving(ctx: GameContext): Boolean =
    ctx.inputState.keyboard.keysDown.collect(gameMappings).exists {
      case m: Move => true
      case _       => false
    }

  def isMovingDown(ctx: GameContext): Boolean =
    ctx.inputState.keyboard.keysDown.collect(gameMappings).exists {
      case m: Move => m.point.y > 0
      case _       => false
    }

  private lazy val produceKeyboardCommands =
    inputMappings.andThen(cmds.enqueue).orElse(_ => cmds)

  lazy val inputMappings =
    debugMappings orElse gameMappings

  lazy val gameMappings: PartialFunction[Key, GameplayCommand] =
    case Key.SPACE              => HardDrop
    case Key.LEFT_ARROW         => MoveLeft
    case Key.RIGHT_ARROW        => MoveRight
    case Key.DOWN_ARROW         => MoveDown
    case Key.UP_ARROW           => Rotate(Clockwise)
    case Key.KEY_Q              => Rotate(CounterClockwise)
    case Key.KEY_W              => Rotate(Clockwise)
    case Key.KEY_P | Key.ESCAPE => Pause
    case Key.KEY_H              => SwapHeld
    case Key.SHIFT              => SwapHeld

  lazy val debugMappings: PartialFunction[Key, GameplayCommand] =
    case Key.KEY_I => SpawnTetromino(Tetromino.i(spawnPoint))
    case Key.KEY_J => SpawnTetromino(Tetromino.j(spawnPoint))
    case Key.KEY_L => SpawnTetromino(Tetromino.l(spawnPoint))
    case Key.KEY_O => SpawnTetromino(Tetromino.o(spawnPoint))
    case Key.KEY_S => SpawnTetromino(Tetromino.s(spawnPoint))
    case Key.KEY_T => SpawnTetromino(Tetromino.t(spawnPoint))
    case Key.KEY_Z => SpawnTetromino(Tetromino.z(spawnPoint))
    case Key.KEY_R => Reset

object GameplayInput:
  val lens: Lens[GameplayModel, GameplayInput] = Lens(
    _.input,
    (m, i) => m.copy(input = i)
  )

  def initial(spawnPoint: Vector2, canvasSize: CanvasSize): GameplayInput =
    val gestureArea = canvasSize.toPolygon

    GameplayInput(
      spawnPoint = spawnPoint,
      cmds = Queue.empty[GameplayCommand],
      tapGestureArea = TapGestureArea(gestureArea),
      swipeGestureArea = SwipeGestureArea(gestureArea),
      panGestureArea = PanGestureArea(gestureArea)
    )
