package com.scenes.gameplay.model

import com.core.*
import indigo.IndigoLogger.*
import indigo.*
import indigo.shared.Outcome
import indigo.shared.datatypes.Point
import indigo.shared.events.GlobalEvent
import indigoextras.geometry.BoundingBox
import indigoextras.geometry.Vertex

import scala.collection.immutable.Queue

import Tetromino.*
import GameplayModel.*
import Command.*
import RotationDirection.*

case class GameplayModel(
    state: GameplayState,
    private val cmds: Queue[Command]
):
  def onInput(ctx: GameContext, e: InputEvent): Outcome[GameplayModel] =
    Outcome(
      copy(cmds = produceCommands(ctx))
    )

  def onFrameTick(ctx: GameContext): Outcome[GameplayModel] =
    for
      state <- state.onFrameTickPreCmd(ctx)
      state <- state.consumeCommands(ctx, cmds)
      state <- state.onFrameTickPostCmd(ctx, cmds)
    yield GameplayModel(state = state, cmds = Queue.empty[Command])

  private def produceCommands(ctx: GameContext): Queue[Command] =
    ctx.inputState
      .mapInputsOption(Command.allMappings)
      .map(cmds.enqueue)
      .getOrElse(cmds)

object GameplayModel:
  def initial(grid: BoundingBox): GameplayModel =
    GameplayModel(
      state = GameplayState.Initial(GameMap.walled(grid), 0, Batch.empty[Int]),
      cmds = Queue.empty[Command]
    )

  // TODO: extract to controller?
  sealed trait Command
  object Command:
    enum GameCommand extends Command:
      case Move(point: Point)
      case Rotate(direction: RotationDirection)
      case HardDrop
      case Pause
    export GameCommand.*

    // TODO: use Batch ?
    // format: off

    val gameMappings = List(
        Combo.withKeyInputs(Key.LEFT_ARROW, Key.KEY_Q)  -> Composite(Move(Point(-1, 0)), Rotate(CounterClockwise)),
        Combo.withKeyInputs(Key.LEFT_ARROW, Key.KEY_W)  -> Composite(Move(Point(-1, 0)), Rotate(Clockwise)),

        Combo.withKeyInputs(Key.RIGHT_ARROW, Key.KEY_Q) -> Composite(Move(Point(1, 0)), Rotate(CounterClockwise)),
        Combo.withKeyInputs(Key.RIGHT_ARROW, Key.KEY_W) -> Composite(Move(Point(1, 0)), Rotate(Clockwise)),

        Combo.withKeyInputs(Key.DOWN_ARROW,  Key.KEY_Q) -> Composite(Move(Point(0, 1)), Rotate(CounterClockwise)),
        Combo.withKeyInputs(Key.DOWN_ARROW,  Key.KEY_W) -> Composite(Move(Point(0, 1)), Rotate(Clockwise)),

        Combo.withKeyInputs(Key.LEFT_ARROW, Key.SPACE)  -> Composite(Move(Point(-1, 0)), HardDrop),
        Combo.withKeyInputs(Key.RIGHT_ARROW, Key.SPACE) -> Composite(Move(Point(1, 0)), HardDrop),
        Combo.withKeyInputs(Key.DOWN_ARROW, Key.SPACE)  -> Composite(Move(Point(0, 1)), HardDrop),
        Combo.withKeyInputs(Key.SPACE) -> HardDrop,

        Combo.withKeyInputs(Key.LEFT_ARROW)  -> Move(Point(-1, 0)),
        Combo.withKeyInputs(Key.RIGHT_ARROW) -> Move(Point(1, 0)),
        Combo.withKeyInputs(Key.DOWN_ARROW)  -> Move(Point(0, 1)),

        Combo.withKeyInputs(Key.KEY_Q) -> Rotate(CounterClockwise),
        Combo.withKeyInputs(Key.KEY_W) -> Rotate(Clockwise),
        Combo.withKeyInputs(Key.KEY_P) -> Pause
    )
    // format: on

    enum DebugCommand extends Command:
      case Reset
      case SpawnTetromino(t: Tetromino)
    export DebugCommand.*

    // format: off
    val debugMappings = List(
      Combo.withKeyInputs(Key.KEY_I) -> SpawnTetromino(Tetromino.i(spawnPoint)),
      Combo.withKeyInputs(Key.KEY_J) ->  SpawnTetromino(Tetromino.j(spawnPoint)),
      Combo.withKeyInputs(Key.KEY_L) ->  SpawnTetromino(Tetromino.l(spawnPoint)),
      Combo.withKeyInputs(Key.KEY_O) ->  SpawnTetromino(Tetromino.o(spawnPoint)),
      Combo.withKeyInputs(Key.KEY_S) ->  SpawnTetromino(Tetromino.s(spawnPoint)),
      Combo.withKeyInputs(Key.KEY_T) ->  SpawnTetromino(Tetromino.t(spawnPoint)),
      Combo.withKeyInputs(Key.KEY_Z) ->  SpawnTetromino(Tetromino.z(spawnPoint)),
      Combo.withKeyInputs(Key.KEY_R) ->  Reset
    )
    // format: on

    case class Composite(cmds: Batch[Command]) extends Command
    object Composite:
      def apply(cmds: Command*): Composite = Composite(Batch.fromSeq(cmds))

    val allMappings = InputMapping(gameMappings) add debugMappings

  enum GameplayState:
    case Initial(
        map: GameMap,
        score: Int,
        fullLines: Batch[Int]
    )
    case InProgress(
        map: GameMap,
        tetromino: Tetromino,
        lastUpdatedFalling: Seconds,
        fallDelay: Seconds,
        score: Int
    )
    case Paused(
        pausedState: GameplayState
    )

    case GameOver(
        finishedState: GameplayState
    )

  val spawnPoint = Point(9, 1)

  extension (state: GameplayState)
    def onFrameTickPreCmd(ctx: GameContext): Outcome[GameplayState] =
      state match
        case s: GameplayState.Initial =>
          // TODO: add score + anmations
          s.removeFullLines.flatMap(_.spawnTetromino(ctx, None))
        case s => Outcome(state)

    def onFrameTickPostCmd(ctx: GameContext, cmds: Queue[Command]): Outcome[GameplayState] =
      state match
        case s: GameplayState.InProgress => 
          s.autoTetrominoDescent(ctx, isMovingDown(ctx))
        case s                           => Outcome(state)

    def isMovingDown(ctx: GameContext): Boolean =
        // TODO: less typesafe that I would like
        // depends on pure info from the controller, ingoring Model's the game logic, so:
        // ignoring: potential Moves with -y, not applied Moves, multiple Moves that cancels out, .etc
        // The game doesn't support those at the moment, but once it will - this will break
        ctx.inputState.keyboard.keysAreDown(Key.DOWN_ARROW)

    def consumeCommands(
        ctx: GameContext,
        cmds: Queue[Command]
    ): Outcome[GameplayState] =
      cmds.foldLeft(Outcome(state)) { (acc, cmd) =>
        acc.flatMap(_.onCommand(ctx, cmd))
      }

    def onCommand(ctx: GameContext, cmd: Command): Outcome[GameplayState] =
      state match
        case s: GameplayState.InProgress => s.onCommand(ctx, cmd)
        case s: GameplayState.Paused     => s.onCommand(ctx, cmd)
        case s: GameplayState.GameOver   => s.onCommand(ctx, cmd)
        case _                           => Outcome(state)

    def map: GameMap =
      state match
        case s: GameplayState.Initial    => s.map
        case s: GameplayState.InProgress => s.map
        case s: GameplayState.Paused     => s.pausedState.map
        case s: GameplayState.GameOver   => s.finishedState.map

    def score: Int =
      state match
        case s: GameplayState.Initial    => s.score
        case s: GameplayState.InProgress => s.score
        case s: GameplayState.Paused     => s.pausedState.score
        case s: GameplayState.GameOver   => s.finishedState.score

    def spawnTetromino(
        ctx: GameContext,
        t: Option[Tetromino]
    ): Outcome[GameplayState.InProgress] =
      val tetromino = t.getOrElse {
        Tetromino.spawn(
          side = ctx.dice.rollFromZero(7)
        )(spawnPoint)
      }

      Outcome[GameplayState.InProgress](
        GameplayState.InProgress(
          state.map,
          tetromino,
          ctx.gameTime.running,
          Seconds(1),
          state.score
        )
      )

    def reset(ctx: GameContext, t: Option[Tetromino]): Outcome[GameplayState] =
      GameplayState
        .Initial(
          map = state.map.reset,
          score = state.score,
          fullLines = Batch.empty[Int]
        )
        .spawnTetromino(ctx, t)

  extension (state: GameplayState.Initial)
    def removeFullLines: Outcome[GameplayState] =
      Outcome(
        state.copy(
          map = state.map.removeFullLines(state.fullLines)
        )
      )

  extension (state: GameplayState.GameOver)
    def onCommand(ctx: GameContext, cmd: Command): Outcome[GameplayState] =
      cmd match
        case Reset => state.reset(ctx, None)
        case _     => Outcome(state)

  extension (state: GameplayState.Paused)
    def onCommand(ctx: GameContext, cmd: Command): Outcome[GameplayState] =
      cmd match
        case Pause => state.continue
        case _     => Outcome(state)

    def continue: Outcome[GameplayState] = Outcome(state.pausedState)

  extension (state: GameplayState.InProgress)
    def onCommand(ctx: GameContext, cmd: Command): Outcome[GameplayState] =
      cmd match
        case Move(point)       => state.moveTetrominoBy(point, ctx)
        case Rotate(direction) => state.rotateTetromino(ctx, direction)
        case HardDrop          => state.hardDrop(ctx)
        case SpawnTetromino(tetromino) =>
          state.spawnTetromino(ctx, Some(tetromino))
        case Reset => state.reset(ctx, None)
        case Pause => state.pause
        case Composite(cmds) =>
          cmds.foldLeft(Outcome[GameplayState](state)) { (state, cmd) =>
            state.flatMap(_.onCommand(ctx, cmd))
          }

    def hardDrop(ctx: GameContext): Outcome[GameplayState] =
      val lineBeforeFloor = state.map.bottomInternal
      val linesToBottom   = lineBeforeFloor - state.tetromino.lowestPoint.y

      val intersection = (0 to linesToBottom).find { y =>
        state.map.intersects(state.tetromino.moveBy(Point(0, y)).positions)
      }

      val movement = Point(0, intersection.map(_ - 1) getOrElse linesToBottom)
      val movedTetromino = state.tetromino.moveBy(movement)
      val sticksOutOfTheMap =
        // movement decreased by 1 on intersections, so it can't be `<=`
        movedTetromino.positions.exists(_.y < state.map.topInternal)

      if sticksOutOfTheMap then
        Outcome(GameplayState.GameOver(finishedState = state))
      else
        val nextMap = state.map.insertTetromino(movedTetromino)
        Outcome(
          GameplayState.Initial(
            map = nextMap,
            score = state.score,
            fullLines = nextMap.fullLinesWith(movedTetromino)
          )
        )

    def moveTetrominoBy(
        point: Point,
        ctx: GameContext
    ): Outcome[GameplayState] =
      val movedTetromino = state.tetromino.moveBy(point)
      val intersections  = state.map.intersectsWith(movedTetromino.positions)

      val horizontalMovement = point.x != 0
      val verticalMovement   = point.y != 0

      val noIntersections = intersections.isEmpty
      lazy val stackIntersections = intersections.collect {
        case e: MapElement.Floor  => e.point
        case e: MapElement.Debris => e.point
      }

      lazy val intersectedStack =
        !horizontalMovement && !stackIntersections.isEmpty
      lazy val sticksOutOfTheMap =
        intersectedStack && movedTetromino.positions.exists(
          _.y <= state.map.topInternal
        )

      if sticksOutOfTheMap then
        Outcome(GameplayState.GameOver(finishedState = state))
      else if intersectedStack then
        val nextMap = state.map.insertTetromino(state.tetromino)
        Outcome(
          GameplayState.Initial(
            map = nextMap,
            score = state.score,
            fullLines = nextMap.fullLinesWith(state.tetromino)
          )
        )
      else if noIntersections then
        Outcome(
          state.copy(
            tetromino = movedTetromino
          )
        )
      else Outcome(state)

    def rotateTetromino(
        ctx: GameContext,
        direction: RotationDirection
    ): Outcome[GameplayState] =
      Outcome(
        state.tetromino
          .rotate(direction)(state.map.intersects)
          .map(t => state.copy(tetromino = t))
          .getOrElse(state)
      )

    def autoTetrominoDescent(ctx: GameContext, isMovingDown: Boolean): Outcome[GameplayState] =
      val running = ctx.gameTime.running

      if running > state.lastUpdatedFalling + state.fallDelay then
        val nextState: GameplayState.InProgress =
          state.copy(lastUpdatedFalling = running)

        if isMovingDown 
        then Outcome(nextState) // reseting the counter for the next frame
        else nextState.moveTetrominoBy(Point(0, 1), ctx)
      else Outcome(state)

    def pause: Outcome[GameplayState] =
      Outcome(GameplayState.Paused(pausedState = state))
