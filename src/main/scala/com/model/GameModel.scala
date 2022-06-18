package com.model

import com.*
import com.model.Tetramino.*
import indigo.*
import indigo.shared.Outcome
import indigo.shared.datatypes.Point
import indigoextras.geometry.BoundingBox
import indigoextras.geometry.Vertex

case class GameModel(
    state: GameState
):
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
      tetramino: Tetramino,
      lastUpdated: Seconds,
      fallDelay: Seconds
  )
extension (state: GameState)
  def onFrameTick(ctx: GameContext): GameState =
    state match
      case s: GameState.Initial =>
        s.spawnTetramino(ctx)
      case s: GameState.InProgress =>
        s.autoTetraminoDescent(ctx, input = Point.zero)

  def onInput(ctx: GameContext, e: KeyboardEvent): GameState =
    // todo: fast, smooth movement on long press
    (state, e) match
      case (s: GameState.InProgress, KeyboardEvent.KeyDown(Key.LEFT_ARROW)) =>
        s.moveTetraminoBy(Point(-1, 0))
      case (s: GameState.InProgress, KeyboardEvent.KeyDown(Key.RIGHT_ARROW)) =>
        s.moveTetraminoBy(Point(1, 0))
      case (s: GameState.InProgress, KeyboardEvent.KeyDown(Key.DOWN_ARROW)) =>
        s.moveTetraminoBy(Point(0, 1))
      case _ => state

extension (state: GameState.Initial)
  def spawnTetramino(ctx: GameContext): GameState.InProgress =
    val tetramino = Tetramino.spawn(
      center = Point(9, 1),
      side = ctx.dice.rollFromZero(6)
    )

    GameState.InProgress(state.map, tetramino, ctx.gameTime.running, Seconds(1))

extension (state: GameState.InProgress)
  def moveTetraminoBy(point: Point): GameState =
    val movedTetramino = state.tetramino.moveBy(point)

    val intersections = state.map.intersects(movedTetramino)
    lazy val intersectStack =
      intersections.exists {
        case _: MapElement.Floor  => true
        case _: MapElement.Debris => true
        case _                    => false
      }

    if intersections.isEmpty then
      state.copy(
        tetramino = movedTetramino
      )
    else if intersectStack then
      GameState.Initial(
        map = state.map.insertDebris(
          state.tetramino.positions.map(Vertex.fromPoint(_))
        )
      )
    else state

  def autoTetraminoDescent(ctx: GameContext, input: Point): GameState =
    if ctx.gameTime.running > state.lastUpdated + state.fallDelay then
      state
        .copy(
          lastUpdated = ctx.gameTime.running
        )
        .moveTetraminoBy(input + Point(0, 1))
    else state.moveTetraminoBy(input)
