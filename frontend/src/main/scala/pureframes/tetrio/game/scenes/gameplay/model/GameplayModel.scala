package pureframes.tetrio
package game.scenes.gameplay.model

import indigo.IndigoLogger.*
import indigo.*
import indigo.shared.Outcome
import indigo.shared.events.GlobalEvent
import indigo.shared.events.KeyboardEvent.KeyDown
import indigo.shared.utils.Lens
import indigoextras.geometry.BoundingBox
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
    Outcome(
      copy(input = input.onInput(e, ctx))
    )

  def onFrameTick(ctx: GameContext): Outcome[GameplayModel] =
    for
      state <- state.onFrameTickPreCmd(ctx)
      state <- state.consumeCommands(ctx, input)
      state <- state.onFrameTickPostCmd(ctx, input)
    yield GameplayModel(state = state, input = input.onFrameEnd)

object GameplayModel:
  def initial(setupData: SetupData): GameplayModel =
    val grid = setupData.bootData.gridSize

    GameplayModel(
      state = GameplayState
        .Initial(GameMap.walled(grid), Progress.initial, Batch.empty[Int]),
      input = GameplayInput.initial(setupData.spawnPoint)
    )

  enum GameplayState:
    case Initial(
        map: GameMap,
        progress: Progress,
        fullLines: Batch[Int]
    )
    case InProgress(
        map: GameMap,
        tetromino: Tetromino,
        lastUpdatedFalling: Seconds,
        fallDelay: Seconds,
        progress: Progress,
        lastMovement: Option[Vector2]
    )
    case Paused(
        pausedState: GameplayState
    )

    case GameOver(
        finishedState: GameplayState
    )
  extension (state: GameplayState)
    def onFrameTickPreCmd(ctx: GameContext): Outcome[GameplayState] =
      state match
        case s: GameplayState.Initial =>
          s.removeFullLines.flatMap(_.spawnTetromino(ctx, None))

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
        t: Option[Tetromino]
    ): Outcome[GameplayState.InProgress] =
      val tetromino = t.getOrElse {
        Tetromino.spawn(
          side = ctx.dice.rollFromZero(7)
        )(ctx.startUpData.spawnPoint)
      }

      Outcome[GameplayState.InProgress](
        GameplayState.InProgress(
          map = state.map,
          tetromino = tetromino,
          lastUpdatedFalling = ctx.gameTime.running,
          fallDelay = Seconds(1),
          progress = state.progress,
          lastMovement = None
        )
      ).addGlobalEvents(
        GameplayEvent.ProgressUpdated(state.progress, inProgress = true)
      )

    def reset(ctx: GameContext, t: Option[Tetromino]): Outcome[GameplayState] =
      GameplayState
        .Initial(
          map = state.map.reset,
          progress = Progress.initial,
          fullLines = Batch.empty[Int]
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
          GameplayEvent.ProgressUpdated(nextProgress, inProgress = true)
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

    def continue: Outcome[GameplayState] = Outcome(state.pausedState)

  extension (state: GameplayState.InProgress)
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
        case Reset => state.reset(ctx, None)
        case Pause => state.pause
        case Composite(cmds) =>
          cmds.foldLeft(Outcome[GameplayState](state)) { (state, cmd) =>
            state.flatMap(_.onCommand(ctx, cmd))
          }

    def hardDrop(ctx: GameContext): Outcome[GameplayState] =
      val movement =
        Movement.closestMovement(Vector2(0, state.map.bottomInternal), state)

      lazy val nextState =
        val nextTetromino = movement.movedTetromino
        val nextMap       = state.map.insertTetromino(nextTetromino)
        val fullLines     = nextMap.fullLinesWith(nextTetromino)

        Outcome(
          GameplayState.Initial(
            map = nextMap,
            progress = state.progress,
            fullLines = fullLines
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
            GameplayEvent.ProgressUpdated(state.progress, inProgress = false)
          )
      else if movement.intersectedStack then
        val nextMap = state.map.insertTetromino(state.tetromino)
        Outcome(
          GameplayState.Initial(
            map = nextMap,
            progress = state.progress,
            fullLines = nextMap.fullLinesWith(state.tetromino)
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
