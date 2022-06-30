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

case class GameModel(state: GameState):
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
  val spawnPoint = Point(9, 0)
  def initial(grid: BoundingBox) =
    GameModel(
      state = GameState.Initial(GameMap.walled(grid))
    )

enum GameState:
  case Initial(
      map: GameMap
  )
  case InProgress(
      map: GameMap,
      tetromino: Tetromino,
      lastUpdated: Seconds,
      fallDelay: Seconds
  )

  case Paused(
      pausedState: GameState
  )

extension (state: GameState)
  def map: GameMap =
    state match
      case s: GameState.Initial    => s.map
      case s: GameState.InProgress => s.map
      case s: GameState.Paused     => s.pausedState.map

  def onFrameTick(ctx: GameContext): GameState =
    state match
      case s: GameState.Initial =>
        s.spawnTetromino(ctx, None)
      case s: GameState.InProgress =>
        s.autoTetrominoDescent(ctx, input = Point.zero)
      case s: GameState.Paused => s

  def onInput(ctx: GameContext, e: KeyboardEvent): GameState =
    state match
      case s: GameState.InProgress => s.onInput(ctx, e)
      case s: GameState.Paused     => s.onInput(ctx, e)
      case _                       => state

  def spawnTetromino(
      ctx: GameContext,
      t: Option[Tetromino]
  ): GameState.InProgress =
    val tetromino = t.getOrElse {
      Tetromino.spawn(
        side = ctx.dice.rollFromZero(6)
      )(GameModel.spawnPoint)
    }

    GameState.InProgress(state.map, tetromino, ctx.gameTime.running, Seconds(1))

extension (state: GameState.Paused)
  def onInput(ctx: GameContext, e: KeyboardEvent): GameState =
    e match
      case KeyboardEvent.KeyDown(Key.KEY_P) => state.continue
      case _                                => state

  def continue: GameState = state.pausedState

extension (state: GameState.InProgress)
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
    val lineBeforeFloor = state.map.bottom - 1
    val linesToBottom   = lineBeforeFloor - state.tetromino.lowestPoint.y

    val intersection = (0 to linesToBottom).find { y =>
      state.map.intersects(
        state.tetromino.moveBy(Point(0, y)).positions
      )
    }
    val movement = Point(0, intersection.map(_ - 1) getOrElse linesToBottom)
    val debrisPositons =
      state.tetromino.positions.map(_.moveBy(movement).toVertex).toBatch

    GameState.Initial(
      map = state.map.insertDebris(debrisPositons, state.tetromino.color)
    )

  def moveTetrominoBy(
      point: Point
  ): GameState =
    val movedTetromino = state.tetromino.moveBy(point)
    val intersections  = state.map.intersectsWith(movedTetromino.positions)

    lazy val movesVertically = point.x == 0
    lazy val intersectedStack =
      intersections.exists {
        case _: MapElement.Floor | _: MapElement.Debris => true
        case _                                          => false
      }

    if intersections.isEmpty then
      state.copy(tetromino = movedTetromino)
    else if movesVertically && intersectedStack then
      GameState.Initial(
        map = state.map.insertDebris(
          state.tetromino.positions.map(_.toVertex).toBatch,
          state.tetromino.color
        )
      )
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

  def reset(ctx: GameContext, t: Option[Tetromino]): GameState =
    GameState.Initial(map = state.map.reset).spawnTetromino(ctx, t)
