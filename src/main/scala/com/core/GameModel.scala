package com.core

import com.scenes.gameplay.model.GameplayModel
import indigo.*
import indigo.shared.Outcome
import indigoextras.geometry.BoundingBox

final case class GameModel(gameplay: GameplayModel):
  def onFrameTick(ctx: GameContext): Outcome[GameModel] =
    for gameplay <- gameplay.onFrameTick(ctx)
    yield copy(gameplay = gameplay)

  def onInput(ctx: GameContext, e: KeyboardEvent): Outcome[GameModel] =
    for gameplay <- gameplay.onInput(ctx, e)
    yield copy(gameplay = gameplay)

object GameModel:
  def initial(grid: BoundingBox) =
    GameModel(
      gameplay = GameplayModel.initial(grid)
    )
