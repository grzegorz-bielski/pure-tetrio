package com.scenes.gameplay.model

import com.core.*
import indigo.IndigoLogger.*
import indigo.*
import indigo.shared.Outcome
import indigo.shared.events.GlobalEvent
import indigo.shared.events.KeyboardEvent.KeyDown
import indigoextras.geometry.BoundingBox

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
      copy(cmds = produceCommands(e))
    )

  def onFrameTick(ctx: GameContext): Outcome[GameplayModel] =
    for
      state <- state.onFrameTickPreCmd(ctx, cmds)
      state <- state.consumeCommands(ctx, cmds)
      state <- state.onFrameTickPostCmd(ctx, cmds)
    yield GameplayModel(state = state, cmds = Queue.empty[Command])

  private val produceCommands =
    inputMappings.andThen(cmds.enqueue).orElse(_ => cmds)

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
      case Move(point: Vector2)
      case Rotate(direction: RotationDirection)
      case HardDrop
      case Pause
    export GameCommand.*

    // TODO: use Batch ?
    val gameMappings: PartialFunction[InputEvent, Command] =
      case KeyDown(Key.SPACE)       => HardDrop
      case KeyDown(Key.LEFT_ARROW)  => Move(Vector2(-1, 0))
      case KeyDown(Key.RIGHT_ARROW) => Move(Vector2(1, 0))
      case KeyDown(Key.DOWN_ARROW)  => Move(Vector2(0, 1))
      case KeyDown(Key.KEY_Q)       => Rotate(CounterClockwise)
      case KeyDown(Key.KEY_W)       => Rotate(Clockwise)
      case KeyDown(Key.KEY_P)       => Pause

    enum DebugCommand extends Command:
      case Reset
      case SpawnTetromino(t: Tetromino)
    export DebugCommand.*

    val debugMappings: PartialFunction[InputEvent, Command] =
      case KeyDown(Key.KEY_I) => SpawnTetromino(Tetromino.i(spawnPoint))
      case KeyDown(Key.KEY_J) => SpawnTetromino(Tetromino.j(spawnPoint))
      case KeyDown(Key.KEY_L) => SpawnTetromino(Tetromino.l(spawnPoint))
      case KeyDown(Key.KEY_O) => SpawnTetromino(Tetromino.o(spawnPoint))
      case KeyDown(Key.KEY_S) => SpawnTetromino(Tetromino.s(spawnPoint))
      case KeyDown(Key.KEY_T) => SpawnTetromino(Tetromino.t(spawnPoint))
      case KeyDown(Key.KEY_Z) => SpawnTetromino(Tetromino.z(spawnPoint))
      case KeyDown(Key.KEY_R) => Reset

    case class Composite(cmds: Batch[Command]) extends Command
    object Composite:
      def apply(cmds: Command*): Composite = Composite(Batch.fromSeq(cmds))

    val inputMappings: PartialFunction[InputEvent, Command] =
      debugMappings orElse gameMappings

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
        score: Int,
        lastMovement: Option[Vector2]
    )
    case Paused(
        pausedState: GameplayState
    )

    case GameOver(
        finishedState: GameplayState
    )

  val spawnPoint = Vector2(9, 1)

  case class Intersection(
      movedTetromino: Tetromino,
      intersections: Batch[MapElement],
      point: Vector2
  ):
    lazy val intersects = !intersections.isEmpty

    lazy val minimalMovement =
      point == Vector2.zero || point.abs.max(1) == Vector2(1, 1)
    lazy val horizontalMovement = point.x != 0
    lazy val verticalMovement   = point.y != 0

    lazy val stackIntersections = intersections.collect {
      case e: MapElement.Floor  => e.point
      case e: MapElement.Debris => e.point
    }

    lazy val intersectedStack =
      !horizontalMovement && !stackIntersections.isEmpty

    def sticksOutOfTheMap(topInternal: Int) =
      intersectedStack && movedTetromino.positions.exists(_.y <= topInternal)

  extension (state: GameplayState)
    def onFrameTickPreCmd(
        ctx: GameContext,
        cmds: Queue[Command]
    ): Outcome[GameplayState] =
      state match
        case s: GameplayState.Initial =>
          s.removeFullLines.flatMap(_.spawnTetromino(ctx, None))

        case s => Outcome(state)

    def onFrameTickPostCmd(
        ctx: GameContext,
        cmds: Queue[Command]
    ): Outcome[GameplayState] =
      state match
        case s: GameplayState.InProgress =>
          // TODO: not only moves, those all pressed keys...
          val moves = ctx.inputState.keyboard.keysDown

          if moves.isEmpty then  
            s.copy(lastMovement = None)
            .autoTetrominoDescent(ctx, isMovingDown = false)
          else s.autoTetrominoDescent(ctx, isMovingDown = isMovingDown(ctx))
        case s => Outcome(state)

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
          state.score,
          None
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
        case Move(point)       => state.shiftTetrominoBy(point, ctx)
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
      // TODO: resuse `shiftTetrominoBy`
      val lineBeforeFloor = state.map.bottomInternal
      val linesToBottom   = lineBeforeFloor - state.tetromino.lowestPoint.y

      val intersection = (0 to linesToBottom.toInt).find { y =>
        state.map.intersects(state.tetromino.moveBy(Vector2(0, y)).positions)
      }

      val movement = Vector2(
        0,
        intersection.map(_ - 1).map(_.toDouble).getOrElse(linesToBottom)
      )
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

    def closestIntersections(point: Vector2): Intersection =
      val range = Vector2.zero --> point

      @scala.annotation.tailrec
      def go(i: Int, prev: Option[Intersection]): Intersection =
        val intersection = 
          val point = range(i)
          val movedTetromino = state.tetromino.moveBy(point)
          val intersections  = state.map.intersectsWith(movedTetromino.positions)
          Intersection(movedTetromino, intersections, point)

        prev match
          case _ if intersection.intersects && intersection.minimalMovement => intersection
          case Some(prev) if intersection.intersects => prev
          case _ if i == range.length - 1 => intersection
          case _ => go(i + 1, Some(intersection))

      go(0, None)

    def shiftTetrominoBy(
        baseMovement: Vector2,
        ctx: GameContext
    ): Outcome[GameplayState] =
      // TODO: this is a mess

      // TODO: ease function, signal ?
      val inputForce = (a: Double) =>
        if a < 0 then a - 1
        else if a > 0 then a + 1
        else a
      val movement = state.lastMovement
        .map { last =>
          lazy val pforce = Vector2(inputForce(last.x), inputForce(last.y))
          // println("pforce"       -> pforce)
          // println("last"         -> last)
          // println("baseMovement" -> baseMovement)

          val res =
            if last.x < 0 && baseMovement.x < 0 || last.x > 0 && baseMovement.x > 0 then
              pforce
            else if last.y < 0 && baseMovement.y < 0 || last.y > 0 && baseMovement.y > 0 then
              pforce
            else baseMovement

          // println("res" -> res)
          res
        }
        .getOrElse(baseMovement)

      state.moveTetrominoBy(
        movement,
        intersection =>
          Outcome(
            state.copy(
              tetromino = intersection.movedTetromino,
              lastMovement = Some(intersection.point)
            )
          )
      )

    def moveTetrominoBy(
        point: Vector2,
        fn: Intersection => Outcome[GameplayState]
        // ctx: GameContext
    ): Outcome[GameplayState] =
      val intersection = state.closestIntersections(point)
      // pprint.pprintln(
      //   "intersection.intersectedStack" -> intersection.intersectedStack
      // )
      // pprint.pprintln("intersection.point" -> intersection.point)

      if intersection.sticksOutOfTheMap(state.map.topInternal) then
        Outcome(GameplayState.GameOver(finishedState = state))
      else if intersection.intersectedStack then
        val nextMap = state.map.insertTetromino(state.tetromino)
        Outcome(
          GameplayState.Initial(
            map = nextMap,
            score = state.score,
            fullLines = nextMap.fullLinesWith(state.tetromino)
          )
        )
      else if intersection.intersections.isEmpty then fn(intersection)
      else Outcome(state.copy(lastMovement = None))

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

    def autoTetrominoDescent(
        ctx: GameContext,
        isMovingDown: Boolean
    ): Outcome[GameplayState] =
      val running = ctx.gameTime.running

      if running > state.lastUpdatedFalling + state.fallDelay then
        val nextState: GameplayState.InProgress =
          state.copy(lastUpdatedFalling = running)

        if isMovingDown then
          Outcome(nextState) // reseting the counter for the next frame
        else
          nextState.moveTetrominoBy(
            Vector2(0, 1),
            intersection =>
              Outcome(
                nextState.copy(
                  tetromino = intersection.movedTetromino,
                  lastMovement = None
                )
              )
          )
      else Outcome(state)

    def pause: Outcome[GameplayState] =
      Outcome(GameplayState.Paused(pausedState = state))
