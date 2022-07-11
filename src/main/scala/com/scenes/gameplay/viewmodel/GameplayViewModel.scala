package com.scenes.gameplay.viewmodel

import com.core.*
import com.scenes.gameplay.model.*
import indigo.*
import indigo.shared.Outcome
import indigo.shared.collections.Batch
import indigo.shared.collections.NonEmptyBatch
import indigo.shared.datatypes.Point
import indigoextras.geometry.Vertex

enum GameplayViewModel:
  case Initial
  case InProgress(
      currentTetrominoPositions: NonEmptyBatch[Vertex],
      targetTetrominoPositions: NonEmptyBatch[Point]
  )

object GameplayViewModel:
  def initial = GameplayViewModel.Initial

  extension (viewModel: GameplayViewModel)
    def onFrameTick(
        ctx: GameContext,
        model: GameplayModel
    ): Outcome[GameplayViewModel] =
      Outcome(viewModel)

    def onInput(
        ctx: GameContext,
        e: KeyboardEvent,
        model: GameplayModel
    ): Outcome[GameplayViewModel] =
      Outcome(viewModel)
