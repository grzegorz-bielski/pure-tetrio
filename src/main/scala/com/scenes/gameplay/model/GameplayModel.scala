package com.scenes.gameplay.model

import com.core.*
import indigo.*
import indigo.shared.Outcome
import indigo.shared.datatypes.Point
import indigo.shared.events.GlobalEvent
import indigoextras.geometry.BoundingBox
import indigoextras.geometry.Vertex

import Tetromino.*

enum GameplayModel:
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
      pausedState: GameplayModel
  )

  case GameOver(
      finishedState: GameplayModel
  )
object GameplayModel:
  def initial(grid: BoundingBox): GameplayModel =
    GameplayModel.Initial(GameMap.walled(grid), 0, Batch.empty[Int])

  val spawnPoint = Point(9, 1)

  case class TetrominoPositionChanged(
      positions: Tetromino.Positions,
      from: Seconds
      // to: Tetromino.Positions
  ) extends GlobalEvent

  extension (state: GameplayModel)
    def onFrameTick(ctx: GameContext): Outcome[GameplayModel] =
      state match
        case s: GameplayModel.Initial =>
          // todo: add score + anmations
          s.removeFullLines.flatMap(_.spawnTetromino(ctx, None))
        case s: GameplayModel.InProgress =>
          s.autoTetrominoDescent(ctx, input = Point.zero)
        case s => Outcome(s)

    def onInput(ctx: GameContext, e: KeyboardEvent): Outcome[GameplayModel] =
      state match
        case s: GameplayModel.InProgress => s.onInput(ctx, e)
        case s: GameplayModel.Paused     => s.onInput(ctx, e)
        case s: GameplayModel.GameOver   => s.onInput(ctx, e)
        case _                           => Outcome(state)

    def map: GameMap =
      state match
        case s: GameplayModel.Initial    => s.map
        case s: GameplayModel.InProgress => s.map
        case s: GameplayModel.Paused     => s.pausedState.map
        case s: GameplayModel.GameOver   => s.finishedState.map

    def score: Int =
      state match
        case s: GameplayModel.Initial    => s.score
        case s: GameplayModel.InProgress => s.score
        case s: GameplayModel.Paused     => s.pausedState.score
        case s: GameplayModel.GameOver   => s.finishedState.score

    def spawnTetromino(
        ctx: GameContext,
        t: Option[Tetromino]
    ): Outcome[GameplayModel.InProgress] =
      val tetromino = t.getOrElse {
        Tetromino.spawn(
          side = ctx.dice.rollFromZero(7)
        )(spawnPoint)
      }

      Outcome[GameplayModel.InProgress](
        GameplayModel.InProgress(
          state.map,
          tetromino,
          ctx.gameTime.running,
          Seconds(1),
          state.score
        )
      ).addGlobalEvents(
        TetrominoPositionChanged(
          positions = tetromino.positions,
          from = ctx.gameTime.running
        )
      )

    def reset(ctx: GameContext, t: Option[Tetromino]): Outcome[GameplayModel] =
      GameplayModel
        .Initial(
          map = state.map.reset,
          score = state.score,
          fullLines = Batch.empty[Int]
        )
        .spawnTetromino(ctx, t)

  extension (state: GameplayModel.Initial)
    def removeFullLines: Outcome[GameplayModel] =
      Outcome(
        state.copy(
          map = state.map.removeFullLines(state.fullLines)
        )
      )

  extension (state: GameplayModel.GameOver)
    def onInput(ctx: GameContext, e: KeyboardEvent): Outcome[GameplayModel] =
      e match
        case KeyboardEvent.KeyDown(Key.KEY_R) => state.reset(ctx, None)
        case _                                => Outcome(state)

  extension (state: GameplayModel.Paused)
    def onInput(ctx: GameContext, e: KeyboardEvent): Outcome[GameplayModel] =
      e match
        case KeyboardEvent.KeyDown(Key.KEY_P) => state.continue
        case _                                => Outcome(state)

    def continue: Outcome[GameplayModel] = Outcome(state.pausedState)

  extension (state: GameplayModel.InProgress)
    // todo: move over to gameplay scene and abstract over input ?
    def onInput(ctx: GameContext, e: KeyboardEvent): Outcome[GameplayModel] =
      e match
        case KeyboardEvent.KeyDown(Key.LEFT_ARROW) =>
          state.moveTetrominoBy(Point(-1, 0), ctx)
        case KeyboardEvent.KeyDown(Key.RIGHT_ARROW) =>
          state.moveTetrominoBy(Point(1, 0), ctx)
        case KeyboardEvent.KeyDown(Key.DOWN_ARROW) =>
          state.moveTetrominoBy(Point(0, 1), ctx)
        case KeyboardEvent.KeyDown(Key.KEY_Q) =>
          state.rotateTetromino(ctx, RotationDirection.CounterClockwise)
        case KeyboardEvent.KeyDown(Key.KEY_W) =>
          state.rotateTetromino(ctx, RotationDirection.Clockwise)
        case KeyboardEvent.KeyDown(Key.SPACE) =>
          state.moveDown(ctx)

        // debug
        case KeyboardEvent.KeyDown(Key.KEY_I) =>
          state.spawnTetromino(ctx, Some(Tetromino.i(spawnPoint)))
        case KeyboardEvent.KeyDown(Key.KEY_J) =>
          state.spawnTetromino(ctx, Some(Tetromino.j(spawnPoint)))
        case KeyboardEvent.KeyDown(Key.KEY_L) =>
          state.spawnTetromino(ctx, Some(Tetromino.l(spawnPoint)))
        case KeyboardEvent.KeyDown(Key.KEY_O) =>
          state.spawnTetromino(ctx, Some(Tetromino.o(spawnPoint)))
        case KeyboardEvent.KeyDown(Key.KEY_S) =>
          state.spawnTetromino(ctx, Some(Tetromino.s(spawnPoint)))
        case KeyboardEvent.KeyDown(Key.KEY_T) =>
          state.spawnTetromino(ctx, Some(Tetromino.t(spawnPoint)))
        case KeyboardEvent.KeyDown(Key.KEY_Z) =>
          state.spawnTetromino(ctx, Some(Tetromino.z(spawnPoint)))
        case KeyboardEvent.KeyDown(Key.KEY_R) =>
          state.reset(ctx, None)
        case KeyboardEvent.KeyDown(Key.KEY_P) =>
          state.pause

        case _ => Outcome(state)

    def moveDown(ctx: GameContext): Outcome[GameplayModel] =
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
        Outcome(GameplayModel.GameOver(finishedState = state))
      else
        val nextMap = state.map.insertTetromino(movedTetromino)
        Outcome(
          GameplayModel.Initial(
            map = nextMap,
            score = state.score,
            fullLines = nextMap.fullLinesWith(movedTetromino)
          )
        ).addGlobalEvents(
          TetrominoPositionChanged(
            positions = state.tetromino.positions,
            from = ctx.gameTime.running
          )
        )

    def moveTetrominoBy(
        point: Point,
        ctx: GameContext
    ): Outcome[GameplayModel] =
      val movedTetromino = state.tetromino.moveBy(point)
      val intersections  = state.map.intersectsWith(movedTetromino.positions)

      val movesVertically = point.x == 0
      val noIntersections = intersections.isEmpty
      lazy val stackIntersections = intersections.collect {
        case e: MapElement.Floor  => e.point
        case e: MapElement.Debris => e.point
      }

      lazy val intersectedStack = movesVertically && !stackIntersections.isEmpty
      lazy val sticksOutOfTheMap =
        intersectedStack && movedTetromino.positions.exists(
          _.y <= state.map.topInternal
        )

      if sticksOutOfTheMap then
        Outcome(GameplayModel.GameOver(finishedState = state))
      else if intersectedStack then
        val nextMap = state.map.insertTetromino(state.tetromino)
        Outcome(
          GameplayModel.Initial(
            map = nextMap,
            score = state.score,
            fullLines = nextMap.fullLinesWith(state.tetromino)
          )
        )
      else if noIntersections then
        Outcome(state.copy(tetromino = movedTetromino))
          .addGlobalEvents(
            TetrominoPositionChanged(
              positions = state.tetromino.positions,
              from = ctx.gameTime.running
            )
          )
      else Outcome(state)

    def rotateTetromino(
        ctx: GameContext,
        direction: RotationDirection
    ): Outcome[GameplayModel] =
      Outcome(
        state.tetromino
          .rotate(direction)(state.map.intersects)
          .map(t => state.copy(tetromino = t))
          .getOrElse(state)
      )

    def autoTetrominoDescent(
        ctx: GameContext,
        input: Point
    ): Outcome[GameplayModel] =
      val running = ctx.gameTime.running

      if running > state.lastUpdatedFalling + state.fallDelay then
        state
          .copy(
            lastUpdatedFalling = running
          )
          .moveTetrominoBy(input + Point(0, 1), ctx)
      else if input != Point.zero then state.moveTetrominoBy(input, ctx)
      else Outcome(state)

    def pause: Outcome[GameplayModel] =
      Outcome(GameplayModel.Paused(pausedState = state))
