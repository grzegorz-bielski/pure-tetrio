package com.core

import com.scenes.gameplay.viewmodel.GameplayViewModel
import indigo.*
import indigo.shared.Outcome

case class GameViewModel(
    gameplay: GameplayViewModel
):
  def onFrameTick(ctx: GameContext, model: GameModel): Outcome[GameViewModel] =
    for gameplay <- gameplay.onFrameTick(ctx, model.gameplay)
    yield copy(gameplay = gameplay)

  def onInput(
      ctx: GameContext,
      e: KeyboardEvent,
      model: GameModel
  ): Outcome[GameViewModel] =
    for gameplay <- gameplay.onInput(ctx, e, model.gameplay)
    yield copy(gameplay = gameplay)

object GameViewModel:
  def initial = GameViewModel(
    gameplay = GameplayViewModel.Initial
  )
