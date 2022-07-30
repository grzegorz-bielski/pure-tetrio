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
      state <- state.onFrameTickPreCmd(ctx, cmds)
      state <- state.consumeCommands(ctx, cmds)
      state <- state.onFrameTickPostCmd(ctx, cmds)
    yield GameplayModel(state = state, cmds = Queue.empty[Command])

  private def produceCommands(ctx: GameContext): Queue[Command] =
    ctx.inputState
      .mapInputsOption(Command.keyDownMappings(ctx))
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

    def gameMappings(ctx: GameContext): List[(Combo, Command)] = 
      val leftForce =  Point(-1, 0)
      val rightForce = Point(1, 0)
      val downForce = Point(0, 1)
      
      List(
          Combo.withKeyInputs(Key.LEFT_ARROW, Key.KEY_Q)  -> Composite(Move(leftForce), Rotate(CounterClockwise)),
          Combo.withKeyInputs(Key.LEFT_ARROW, Key.KEY_W)  -> Composite(Move(leftForce), Rotate(Clockwise)),

          Combo.withKeyInputs(Key.RIGHT_ARROW, Key.KEY_Q) -> Composite(Move(rightForce), Rotate(CounterClockwise)),
          Combo.withKeyInputs(Key.RIGHT_ARROW, Key.KEY_W) -> Composite(Move(rightForce), Rotate(Clockwise)),

          Combo.withKeyInputs(Key.DOWN_ARROW,  Key.KEY_Q) -> Composite(Move(downForce), Rotate(CounterClockwise)),
          Combo.withKeyInputs(Key.DOWN_ARROW,  Key.KEY_W) -> Composite(Move(downForce), Rotate(Clockwise)),

          // Combo.withKeyInputs(Key.LEFT_ARROW, Key.SPACE)  -> Composite(Move(leftForce), HardDrop),
          // Combo.withKeyInputs(Key.RIGHT_ARROW, Key.SPACE) -> Composite(Move(rightForce), HardDrop),
          // Combo.withKeyInputs(Key.DOWN_ARROW, Key.SPACE)  -> Composite(Move(downForce), HardDrop),
          Combo.withKeyInputs(Key.SPACE) -> HardDrop,

          Combo.withKeyInputs(Key.LEFT_ARROW)  -> Move(leftForce),
          Combo.withKeyInputs(Key.RIGHT_ARROW) -> Move(rightForce),
          Combo.withKeyInputs(Key.DOWN_ARROW)  -> Move(downForce),

          Combo.withKeyInputs(Key.KEY_Q) -> Rotate(CounterClockwise),
          Combo.withKeyInputs(Key.KEY_W) -> Rotate(Clockwise),
          Combo.withKeyInputs(Key.KEY_P) -> Pause
      )

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

    def keyDownMappings(ctx: GameContext) =
      InputMapping(gameMappings(ctx)) add debugMappings
  // val keyUpMappings: PartialFunction[] =
  //     case Combo.withKeyInputs(Key.KEY_Q) => Rotate(CounterClockwise)
  //     case Combo.withKeyInputs(Key.KEY_W) => Rotate(Clockwise)

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
        lastMovement: Option[Point]
    )
    case Paused(
        pausedState: GameplayState
    )

    case GameOver(
        finishedState: GameplayState
    )

  val spawnPoint = Point(9, 1)

  case class Intersection(
      movedTetromino: Tetromino,
      intersections: Batch[MapElement],
      point: Point
  ):
    lazy val minimalMovement    = 
      val p = point.abs.max(1) 
      p == Point.zero || p == Point(1, 1)
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
          // TODO: add score + anmations

          s.removeFullLines.flatMap(_.spawnTetromino(ctx, None))
        // case s: GameplayState.InProgress =>

        //   println("move" ->  cmds
        //       .collectFirst { case m: Command.Move => m })

        //   Outcome(
        //     cmds
        //       .collectFirst { case m: Command.Move => s }
        //       .getOrElse(s.copy(lastMovement = Point.zero))
        //   )

        case s => Outcome(state)

    def onFrameTickPostCmd(
        ctx: GameContext,
        cmds: Queue[Command]
    ): Outcome[GameplayState] =
      state match
        case s: GameplayState.InProgress =>
          // cmds
          // .collectFirst { case m: Command.Move => s }
          // .getOrElse(s.copy(lastMovement = Point.zero))
          s.autoTetrominoDescent(ctx, isMovingDown(ctx))
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

    def intersectionsAt(point: Point): Intersection =
      val movedTetromino = state.tetromino.moveBy(point)
      val intersections  = state.map.intersectsWith(movedTetromino.positions)

      Intersection(movedTetromino, intersections, point)

    def closestIntersections(point: Point): Intersection =
      @scala.annotation.tailrec
      def go(intersection: Intersection): Intersection =
        if intersection.intersections.isEmpty || intersection.minimalMovement then
          intersection
        else
          go(
            state.intersectionsAt(
              point.moveBy(intersection.point.invert.clamp(-1, 1))
            )
          )

      go(state.intersectionsAt(point))

    def shiftTetrominoBy(
        baseMovement: Point,
        ctx: GameContext
    ): Outcome[GameplayState] =
      val inputForce = (a: Int) =>
        if a < 0 then - 2
        else if a > 0 then 2
        else a
      val movement = state.lastMovement
        .map { last => 
          lazy val pforce = Point(inputForce(last.x), inputForce(last.y)) 
          println("pforce" -> pforce)
          println("last" -> last)
          println("baseMovement" -> baseMovement)

          val res = 
            if last.x < 0 && baseMovement.x < 0 || last.x > 0 && baseMovement.x > 0 then pforce 
            else if last.y < 0 && baseMovement.y < 0 || last.y > 0 && baseMovement.y > 0 then pforce 
            else baseMovement

          println("res" -> res)
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
        point: Point,
        fn: Intersection => Outcome[GameplayState]
        // ctx: GameContext
    ): Outcome[GameplayState] =
      val intersection = state.closestIntersections(point)
      pprint.pprintln("intersection.point" -> intersection.point)

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
        // println(" running > state.lastUpdatedFalling + state.fallDelay" -> ( running > state.lastUpdatedFalling + state.fallDelay))
        val nextState: GameplayState.InProgress =
          state.copy(lastUpdatedFalling = running)

        if isMovingDown then
          Outcome(nextState) // reseting the counter for the next frame
        else
          nextState.moveTetrominoBy(
            Point(0, 1),
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
