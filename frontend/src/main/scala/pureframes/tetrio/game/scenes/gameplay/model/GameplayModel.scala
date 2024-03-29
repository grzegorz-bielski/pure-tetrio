package pureframes.tetrio.game.scenes.gameplay.model

import indigo.IndigoLogger.*
import indigo.*
import indigo.shared.Outcome
import indigo.shared.events.GlobalEvent
import indigo.shared.events.KeyboardEvent.KeyDown
import indigo.shared.utils.Lens
import indigoextras.geometry.BoundingBox
import indigoextras.gestures.GestureEvent
import pureframes.tetrio.game.core.*
import pureframes.tetrio.game.scenes.gameplay.*

import scala.collection.immutable.Queue

import Tetromino.*
import GameplayModel.*
import GameplayCommand.*
import RotationDirection.*

final case class GameplayModel(
    state: GameplayState,
    input: GameplayInput
):
  def onInput(e: InputEvent, ctx: GameContext): Outcome[GameplayModel] =
    input.onInput(e, ctx).map(i => copy(input = i))

  def onFrameTick(ctx: GameContext): Outcome[GameplayModel] =
    for
      state <- state.onFrameTickPreCmd(ctx)
      state <- state.consumeCommands(ctx, input)
      state <- state.onFrameTickPostCmd(ctx, input)
    yield GameplayModel(state = state, input = input.onFrameEnd)

  def onCanvasResize(nextCanvasSize: CanvasSize): GameplayModel =
    copy(input = input.onCanvasResize(nextCanvasSize))

  def onGesture(e: GestureEvent): Outcome[GameplayModel] = 
    input.onGesture(e).map(i => copy(input = i))

object GameplayModel:
  def initial(setupData: SetupData): GameplayModel =
    val grid = setupData.bootData.gridSize

    GameplayModel(
      state = GameplayState
        .Initial(
          GameMap.walled(grid),
          Progress.initial,
          Batch.empty[Int],
          Option.empty[Tetromino],
          Option.empty[Tetromino]
        ),
      input = GameplayInput.initial(setupData.spawnPoint, setupData.bootData.initialCanvasSize)
    )

  def spawnTetrominoPiece(ctx: GameContext): Tetromino =
    Tetromino.spawn(
      side = ctx.dice.rollFromZero(7)
    )(ctx.startUpData.spawnPoint)

  def spawnTetrominoPiece(ctx: GameContext, side: Int): Tetromino =
    Tetromino.spawn(side)(ctx.startUpData.spawnPoint)

  enum GameplayState:
    case Initial(
        map: GameMap,
        progress: Progress,
        fullLines: Batch[Int],
        held: Option[Tetromino],
        next: Option[Tetromino]
    )
    case InProgress(
        map: GameMap,
        tetromino: Tetromino,
        lastUpdatedFalling: Seconds,
        fallDelay: Seconds,
        progress: Progress,
        lastMovement: Option[Vector2],
        held: Option[Tetromino],
        next: Tetromino
    )
    case Paused(
        pausedState: GameplayState
    )

    case GameOver(
        finishedState: GameplayState
    )
  extension (state: GameplayState)
    def heldTetromino: Option[Tetromino] =
      state match
        case s: GameplayState.Initial    => s.held
        case s: GameplayState.InProgress => s.held
        case _                           => None

    def onFrameTickPreCmd(ctx: GameContext): Outcome[GameplayState] =
      state match
        case s: GameplayState.Initial =>
          s.removeFullLines.flatMap(_.spawnTetromino(ctx, s.next))

        case s => Outcome(state)

    def onFrameTickPostCmd(
        ctx: GameContext,
        input: GameplayInput
    ): Outcome[GameplayState] =
      state match
        case s: GameplayState.InProgress =>
          if !input.isMoving(ctx) then
            s.copy(lastMovement = None)
              .autoTetrominoDescent(ctx, isMovingDown = false)
          else
            s.autoTetrominoDescent(ctx, isMovingDown = input.isMovingDown(ctx))
        case s => Outcome(state)

    def consumeCommands(
        ctx: GameContext,
        input: GameplayInput
    ): Outcome[GameplayState] =
      input.cmds.foldLeft(Outcome(state)) { (acc, cmd) =>
        acc.flatMap(_.onCommand(ctx, cmd))
      }

    def onCommand(
        ctx: GameContext,
        cmd: GameplayCommand
    ): Outcome[GameplayState] =
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

    def progress: Progress =
      state match
        case s: GameplayState.Initial    => s.progress
        case s: GameplayState.InProgress => s.progress
        case s: GameplayState.Paused     => s.pausedState.progress
        case s: GameplayState.GameOver   => s.finishedState.progress

    def spawnTetromino(
        ctx: GameContext,
        provided: Option[Tetromino]
    ): Outcome[GameplayState.InProgress] =
      Outcome(
        GameplayState.InProgress(
          map = state.map,
          lastUpdatedFalling = ctx.gameTime.running,
          fallDelay = state.progress.fallDelay,
          progress = state.progress,
          lastMovement = None,
          held = state.heldTetromino,
          tetromino = provided.getOrElse(spawnTetrominoPiece(ctx)),
          next = spawnTetrominoPiece(ctx)
        )
      ).addGlobalEvents(
        GameplayEvent.ProgressUpdated(GameState.InProgress, Some(state.progress))
      )

    def reset(ctx: GameContext, t: Option[Tetromino]): Outcome[GameplayState] =
      GameplayState
        .Initial(
          map = state.map.reset,
          progress = Progress.initial,
          fullLines = Batch.empty[Int],
          held = None,
          next = Some(spawnTetrominoPiece(ctx))
        )
        .spawnTetromino(ctx, t)

  extension (state: GameplayState.Initial)
    def removeFullLines: Outcome[GameplayState] =
      val nextProgress = state.progress.addFullLines(state.fullLines.size)
      val nextMap      = state.map.removeFullLines(state.fullLines)

      Outcome(
        state.copy(
          map = nextMap,
          progress = nextProgress
        )
      )
        .addGlobalEvents(
          GameplayEvent.ProgressUpdated(GameState.InProgress, Some(nextProgress))
        )

  extension (state: GameplayState.GameOver)
    def onCommand(
        ctx: GameContext,
        cmd: GameplayCommand
    ): Outcome[GameplayState] =
      cmd match
        case Reset => state.reset(ctx, None)
        case _     => Outcome(state)

  extension (state: GameplayState.Paused)
    def onCommand(
        ctx: GameContext,
        cmd: GameplayCommand
    ): Outcome[GameplayState] =
      cmd match
        case Pause => state.continue
        case _     => Outcome(state)

    def continue: Outcome[GameplayState] = 
        Outcome(state.pausedState)
          .addGlobalEvents(
            GameplayEvent.ProgressUpdated(GameState.InProgress, None)
          )

  extension (state: GameplayState.InProgress)
    // TODO: call once in lazy val?
    def movementClosestToBottom: Movement = 
      Movement.closestMovement(Vector2(0, state.map.bottomInternal), state)

    def onCommand(
        ctx: GameContext,
        cmd: GameplayCommand
    ): Outcome[GameplayState] =
      cmd match
        case Move(point)       => state.shiftTetrominoBy(point, ctx)
        case Rotate(direction) => state.rotateTetromino(ctx, direction)
        case HardDrop          => state.hardDrop(ctx)
        case SpawnTetromino(tetromino) =>
          state.spawnTetromino(ctx, Some(tetromino))
        case Reset    => state.reset(ctx, None)
        case SwapHeld => state.swapHeld(ctx)
        case Pause    => state.pause
        case Composite(cmds) =>
          cmds.foldLeft(Outcome[GameplayState](state)) { (state, cmd) =>
            state.flatMap(_.onCommand(ctx, cmd))
          }

    def swapHeld(ctx: GameContext): Outcome[GameplayState] =
      val toHold = spawnTetrominoPiece(ctx, state.tetromino.ordinal)

      Outcome(
        state.held.fold(
          state.copy(
            held = Some(toHold),
            tetromino = spawnTetrominoPiece(ctx)
          )
        )(held =>
          state.copy(
            held = Some(toHold),
            tetromino = held
          )
        )
      )

    def hardDrop(ctx: GameContext): Outcome[GameplayState] =
      val movement = movementClosestToBottom

      lazy val nextState =
        val nextTetromino = movement.movedTetromino
        val nextMap       = state.map.insertTetromino(nextTetromino)
        val fullLines     = nextMap.fullLinesWith(nextTetromino)

        Outcome(
          GameplayState.Initial(
            map = nextMap,
            progress = state.progress,
            fullLines = fullLines,
            held = state.held,
            next = Some(state.next)
          )
        )

      state.moveTetrominoBy(ctx, movement, nextState)

    def shiftTetrominoBy(
        baseMovement: Vector2,
        ctx: GameContext
    ): Outcome[GameplayState] =
      val blocksPerQuickShift = 2

      val movementVector = state.lastMovement
        .filter(_.sameDirectionAs(baseMovement))
        .map(_.mapCoords(a => a + (a.sign * blocksPerQuickShift)))
        .getOrElse(baseMovement)

      val movement = Movement.closestMovement(movementVector, state)

      state.moveTetrominoBy(
        ctx,
        movement,
        Outcome(
          state.copy(
            tetromino = movement.movedTetromino,
            lastMovement = Some(movement.point)
          )
        )
      )

    def moveTetrominoBy(
        ctx: GameContext,
        movement: Movement,
        onMove: => Outcome[GameplayState]
    ): Outcome[GameplayState] =
      import ctx.startUpData.bootData.gameOverLine

      if movement.sticksOutOfTheMap(gameOverLine) then
        Outcome(GameplayState.GameOver(finishedState = state))
          .addGlobalEvents(
            GameplayEvent.ProgressUpdated(GameState.Lost, Some(state.progress))
          )
      else if movement.intersectedStack then
        val nextMap = state.map.insertTetromino(state.tetromino)
        Outcome(
          GameplayState.Initial(
            map = nextMap,
            progress = state.progress,
            fullLines = nextMap.fullLinesWith(state.tetromino),
            held = state.held,
            next = Some(state.next)
          )
        )
      else if movement.intersections.isEmpty then onMove
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
          Outcome(nextState) // resetting the counter for the next frame
        else
          val movement = Movement.closestMovement(Vector2(0, 1), state)
          nextState.moveTetrominoBy(
            ctx,
            movement,
            Outcome(
              nextState.copy(
                tetromino = movement.movedTetromino,
                lastMovement = None
              )
            )
          )
      else Outcome(state)

    def pause: Outcome[GameplayState] =
      Outcome(GameplayState.Paused(pausedState = state))
          .addGlobalEvents(GameplayEvent.ProgressUpdated(GameState.Paused, None))
