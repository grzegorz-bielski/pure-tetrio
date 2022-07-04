package com.model

import com.*
import com.model.Tetromino.*
import indigo.*
import indigo.shared.Outcome
import indigo.shared.datatypes.Point
import indigoextras.geometry.BoundingBox
import indigoextras.geometry.LineSegment
import indigoextras.geometry.LineSegment.apply
import indigoextras.geometry.Vertex

final case class GameModel(state: GameState):
  def onFrameTick(ctx: GameContext): Outcome[GameModel] =
    Outcome(
      copy(
        state = state.onFrameTick(ctx)
      )
    )

  def onInput(ctx: GameContext, e: KeyboardEvent): Outcome[GameModel] =
    Outcome(
      copy(
        state = state.onInput(ctx, e)
      )
    )
end GameModel

object GameModel:
  val spawnPoint = Point(9, 1)
  def initial(grid: BoundingBox) =
    GameModel(
      state = GameState.Initial(GameMap.walled(grid), 0, Batch.empty[Int])
    )

enum GameState:
  case Initial(
      map: GameMap,
      score: Int,
      fullLines: Batch[Int]
  )
  case InProgress(
      map: GameMap,
      tetromino: Tetromino,
      lastUpdated: Seconds,
      fallDelay: Seconds,
      score: Int
  )
  case Paused(
      pausedState: GameState
  )

  case GameOver(
      finishedState: GameState
  )

extension (state: GameState)
  def map: GameMap =
    state match
      case s: GameState.Initial    => s.map
      case s: GameState.InProgress => s.map
      case s: GameState.Paused     => s.pausedState.map
      case s: GameState.GameOver   => s.finishedState.map

  def score: Int =
    state match
      case s: GameState.Initial    => s.score
      case s: GameState.InProgress => s.score
      case s: GameState.Paused     => s.pausedState.score
      case s: GameState.GameOver   => s.finishedState.score

  def onFrameTick(ctx: GameContext): GameState =
    state match
      case s: GameState.Initial =>
        s.removeFullLines // add score + anmations
          .spawnTetromino(ctx, None)
      case s: GameState.InProgress =>
        s.autoTetrominoDescent(ctx, input = Point.zero)
      case s => s

  def onInput(ctx: GameContext, e: KeyboardEvent): GameState =
    state match
      case s: GameState.InProgress => s.onInput(ctx, e)
      case s: GameState.Paused     => s.onInput(ctx, e)
      case s: GameState.GameOver   => s.onInput(ctx, e)
      case _                       => state

  def spawnTetromino(
      ctx: GameContext,
      t: Option[Tetromino]
  ): GameState.InProgress =
    val tetromino = t.getOrElse {
      Tetromino.spawn(
        side = ctx.dice.rollFromZero(7)
      )(GameModel.spawnPoint)
    }

    GameState.InProgress(
      state.map,
      tetromino,
      ctx.gameTime.running,
      Seconds(1),
      state.score
    )

  def reset(ctx: GameContext, t: Option[Tetromino]): GameState =
    GameState
      .Initial(
        map = state.map.reset,
        score = state.score,
        fullLines = Batch.empty[Int]
      )
      .spawnTetromino(ctx, t)

extension (state: GameState.Initial)
  def removeFullLines: GameState = state.copy(
    map = state.map.removeFullLines(state.fullLines)
  )

extension (state: GameState.GameOver)
  def onInput(ctx: GameContext, e: KeyboardEvent): GameState =
    e match
      case KeyboardEvent.KeyDown(Key.KEY_R) => state.reset(ctx, None)
      case _                                => state

extension (state: GameState.Paused)
  def onInput(ctx: GameContext, e: KeyboardEvent): GameState =
    e match
      case KeyboardEvent.KeyDown(Key.KEY_P) => state.continue
      case _                                => state

  def continue: GameState = state.pausedState

extension (state: GameState.InProgress)
  // todo: move it to gameplay scene?
  def onInput(ctx: GameContext, e: KeyboardEvent): GameState =
    // todo: smooth movement on long press
    e match
      case KeyboardEvent.KeyDown(Key.LEFT_ARROW) =>
        state.moveTetrominoBy(Point(-1, 0))
      case KeyboardEvent.KeyDown(Key.RIGHT_ARROW) =>
        state.moveTetrominoBy(Point(1, 0))
      case KeyboardEvent.KeyDown(Key.DOWN_ARROW) =>
        state.moveTetrominoBy(Point(0, 1))
      case KeyboardEvent.KeyDown(Key.KEY_Q) =>
        state.rotateTetromino(ctx, RotationDirection.CounterClockwise)
      case KeyboardEvent.KeyDown(Key.KEY_W) =>
        state.rotateTetromino(ctx, RotationDirection.Clockwise)
      case KeyboardEvent.KeyDown(Key.SPACE) =>
        state.moveDown

      // debug
      case KeyboardEvent.KeyDown(Key.KEY_I) =>
        state.spawnTetromino(ctx, Some(Tetromino.i(GameModel.spawnPoint)))
      case KeyboardEvent.KeyDown(Key.KEY_J) =>
        state.spawnTetromino(ctx, Some(Tetromino.j(GameModel.spawnPoint)))
      case KeyboardEvent.KeyDown(Key.KEY_L) =>
        state.spawnTetromino(ctx, Some(Tetromino.l(GameModel.spawnPoint)))
      case KeyboardEvent.KeyDown(Key.KEY_O) =>
        state.spawnTetromino(ctx, Some(Tetromino.o(GameModel.spawnPoint)))
      case KeyboardEvent.KeyDown(Key.KEY_S) =>
        state.spawnTetromino(ctx, Some(Tetromino.s(GameModel.spawnPoint)))
      case KeyboardEvent.KeyDown(Key.KEY_T) =>
        state.spawnTetromino(ctx, Some(Tetromino.t(GameModel.spawnPoint)))
      case KeyboardEvent.KeyDown(Key.KEY_Z) =>
        state.spawnTetromino(ctx, Some(Tetromino.z(GameModel.spawnPoint)))
      case KeyboardEvent.KeyDown(Key.KEY_R) =>
        state.reset(ctx, None)
      case KeyboardEvent.KeyDown(Key.KEY_P) =>
        state.pause

      case _ => state

  def moveDown: GameState =
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

    if sticksOutOfTheMap then GameState.GameOver(finishedState = state)
    else
      val nextMap = state.map.insertTetromino(movedTetromino)
      GameState.Initial(
        map = nextMap,
        score = state.score,
        fullLines = nextMap.fullLinesWith(movedTetromino)
      )

  def moveTetrominoBy(point: Point): GameState =
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
      intersectedStack && movedTetromino.positions.exists(_.y <= state.map.topInternal)

    if sticksOutOfTheMap then GameState.GameOver(finishedState = state)
    else if intersectedStack then
      val nextMap = state.map.insertTetromino(state.tetromino)
      GameState.Initial(
        map = nextMap,
        score = state.score,
        fullLines = nextMap.fullLinesWith(state.tetromino)
      )
    else if noIntersections then state.copy(tetromino = movedTetromino)
    else state

  def rotateTetromino(
      ctx: GameContext,
      direction: RotationDirection
  ): GameState =
    state.tetromino
      .rotate(direction)(state.map.intersects)
      .map(t => state.copy(tetromino = t))
      .getOrElse(state)

  def autoTetrominoDescent(ctx: GameContext, input: Point): GameState =
    if ctx.gameTime.running > state.lastUpdated + state.fallDelay then
      state
        .copy(
          lastUpdated = ctx.gameTime.running
        )
        .moveTetrominoBy(input + Point(0, 1))
    else if input != Point.zero then state.moveTetrominoBy(input)
    else state

  def pause: GameState =
    GameState.Paused(pausedState = state)
