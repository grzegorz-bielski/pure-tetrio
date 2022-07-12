package com.scenes.gameplay.viewmodel

import com.core.*
import com.scenes.gameplay.model.*
import indigo.IndigoLogger.*
import indigo.*
import indigo.shared.Outcome
import indigo.shared.collections.Batch
import indigo.shared.collections.NonEmptyBatch
import indigo.shared.datatypes.Point
import indigoextras.geometry.Vertex
import indigoextras.subsystems.Automata

enum GameplayViewModel:
  case Empty()
  case InProgress(
      prevTetrominoPositions: Option[NonEmptyBatch[Point]],
      targetTetrominoPositions: NonEmptyBatch[Point],
      from: Seconds
  )

object GameplayViewModel:
  def initial: GameplayViewModel.Empty = GameplayViewModel.Empty()

  extension (viewModel: GameplayViewModel)
    def tetrominoPositions(ctx: GameContext): Batch[Point] =
      lazy val ctxGrindPoint = toGridPoint(ctx)
      lazy val targetPositions = (vm:  GameplayViewModel.InProgress) => vm.targetTetrominoPositions.map(ctxGrindPoint)

      viewModel match
        case vm @ GameplayViewModel.InProgress(Some(prevTetrominoPositions), _, _) =>
           (prevTetrominoPositions.map(ctxGrindPoint) zip targetPositions(vm))
                .map(
                  Signal
                    .Lerp(_, _, Seconds(0.093))
                    .at(ctx.gameTime.running - vm.from)
                ).toBatch
        case vm: GameplayViewModel.InProgress =>  targetPositions(vm).toBatch
        case _ => Batch.empty

    def onFrameTick(
        ctx: GameContext,
        model: GameplayModel
    ): Outcome[GameplayViewModel] =
      (model, viewModel) match
        case (m: GameplayModel.InProgress, vm: GameplayViewModel.InProgress) =>
          if vm.targetTetrominoPositions == m.tetromino.positions then
            Outcome(vm)
          else
            Outcome(
              GameplayViewModel.InProgress(
                prevTetrominoPositions = Some(vm.targetTetrominoPositions),
                targetTetrominoPositions = m.tetromino.positions,
                from = ctx.gameTime.running
              )
            )

        case (m: GameplayModel.InProgress, _: GameplayViewModel.Empty) =>
          Outcome(
            GameplayViewModel.InProgress(
              prevTetrominoPositions = None,
              targetTetrominoPositions = m.tetromino.positions,
              from = ctx.gameTime.running
            )
          )

        case _ => Outcome(viewModel)

    def onTetrominoPositionsChanged(
        ctx: GameContext,
        e: GameplayModel.TetrominoPositionChanged,
        model: GameplayModel
    ): Outcome[GameplayViewModel] =
      model match
        case _ => Outcome(viewModel)

def toGridPoint(ctx: GameContext)(point: Point) =
    point * ctx.startUpData.bootData.gridSquareSize