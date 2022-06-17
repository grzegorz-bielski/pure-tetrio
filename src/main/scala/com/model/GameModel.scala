package com.model

import com.*
import com.model.Tetramino.*
import indigo.*
import indigo.shared.Outcome
import indigo.shared.datatypes.Point
import indigoextras.geometry.BoundingBox

case class GameModel(
    map: GameMap,
    state: GameState
):
  def update(ctx: GameContext): Outcome[GameModel] =
    Outcome(
      copy(
        state = state.update(ctx)
      )
    )

end GameModel

object GameModel:
  def initial(grid: BoundingBox) =
    GameModel(
      map = GameMap.walled(grid),
      state = GameState.Initial
    )

enum GameState:
  case Initial
  case InProgress(tetramino: Tetramino, lastUpdated: Seconds, fallDelay: Seconds)

extension (state: GameState)
  def update(ctx: GameContext): GameState =
    state match
      case GameState.Initial       => start(ctx)
      case s: GameState.InProgress => 
        // println(progress(ctx, s))
        progress(ctx, s)

  private def progress(ctx: GameContext, s: GameState.InProgress): GameState =
    // Signal
    //   .Pulse(Seconds(1))
    //   // .clampTime()
    //   .map {
    //     case true if ctx.gameTime.running <  =>
    //       println("true")
    //       s.copy(
    //         tetramino = s.tetramino.moveBy(Point(0, 1))
    //       )
    //     case false => 
    //       println("false")
    //       s
    //   }
    //   // .affectTime(0.2)
    //   .at(ctx.gameTime.running)
    if ctx.gameTime.running > s.lastUpdated + s.fallDelay
    then s.copy(
      lastUpdated = ctx.gameTime.running,
      tetramino = s.tetramino.moveBy(Point(0, 1))
    )
    else s

  private def start(ctx: GameContext): GameState =
    val tetramino = Tetramino.spawn(
      center = Point(9, 1),
      side = ctx.dice.rollFromZero(6)
    )

    GameState.InProgress(tetramino, ctx.gameTime.running, Seconds(1))
