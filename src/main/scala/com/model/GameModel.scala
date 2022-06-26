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
  def initial(grid: BoundingBox) =
    GameModel(
      state = GameState.Initial(GameMap.walled(grid))
    )

private trait MapState:
  def map: GameMap

enum GameState extends MapState:
  case Initial(
      map: GameMap
  )
  case InProgress(
      map: GameMap,
      tetromino: Tetromino,
      lastUpdated: Seconds,
      fallDelay: Seconds
  )

extension (state: GameState)
  def onFrameTick(ctx: GameContext): GameState =
    state match
      case s: GameState.Initial =>
        s.spawnTetromino(ctx)
      case s: GameState.InProgress =>
        s.autoTetrominoDescent(ctx, input = Point.zero)

  def onInput(ctx: GameContext, e: KeyboardEvent): GameState =
    state match
      case s: GameState.InProgress => s.onInput(ctx, e)
      case _                       => state

extension (state: GameState.Initial)
  def spawnTetromino(ctx: GameContext): GameState.InProgress =
    val tetromino = Tetromino.spawn(
      side = ctx.dice.rollFromZero(6)
    )(Point(9, 0))

    GameState.InProgress(state.map, tetromino, ctx.gameTime.running, Seconds(1))

extension (state: GameState.InProgress)
  def onInput(ctx: GameContext, e: KeyboardEvent) =
    // todo: smooth movement on long press
    // todo: fast drop
    e match
      case KeyboardEvent.KeyDown(Key.LEFT_ARROW) =>
        state.moveTetrominoBy(Point(-1, 0))
      case KeyboardEvent.KeyDown(Key.RIGHT_ARROW) =>
        state.moveTetrominoBy(Point(1, 0))
      case KeyboardEvent.KeyDown(Key.DOWN_ARROW) =>
        state.moveTetrominoBy(Point(0, 1))
      case KeyboardEvent.KeyDown(Key.KEY_Z) =>
        state.rotateTetromino(ctx, RotationDirection.CounterClockwise)
      case KeyboardEvent.KeyDown(Key.KEY_X) =>
        state.rotateTetromino(ctx, RotationDirection.Clockwise)
      case KeyboardEvent.KeyDown(Key.SPACE) =>
        state.moveDown
      case KeyboardEvent.KeyDown(Key.KEY_R) =>
        state.reset
      case _ => state

  def moveDown: GameState =
    val start = state.tetromino.lowestPoint
    val end   = Point(start.x, state.map.quadTree.bounds.bottom.toInt)
    val intersection = (start.y to end.y).find { y =>
      state.map.intersects(
        state.tetromino.moveBy(Point(0, y)).positions
      )
    }

    val movement =
      intersection.map(y => Point(0, y - 1)) getOrElse Point(0, end.y - start.y)
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
    lazy val intersectedStack =
      intersections.exists {
        case _: MapElement.Floor | _: MapElement.Debris => true
        case _                                          => false
      }

    // println("state.tetromino.positions" -> state.tetromino.positions)

    if intersections.isEmpty then
      state.copy(
        tetromino = movedTetromino
      )
    else if intersectedStack then
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
    else state.moveTetrominoBy(input)

  def reset = GameState.Initial(
    map = state.map.reset
  )
