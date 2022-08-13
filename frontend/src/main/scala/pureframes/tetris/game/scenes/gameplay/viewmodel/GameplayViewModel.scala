package pureframes.tetris
package game.scenes.gameplay.viewmodel

import indigo.IndigoLogger.*
import indigo.*
import indigo.shared.Outcome
import indigo.shared.collections.Batch
import indigo.shared.collections.NonEmptyBatch
import indigoextras.subsystems.Automata
import pureframes.tetris.game.core.*
import pureframes.tetris.game.scenes.gameplay.model.*

import GameplayModel.GameplayState

enum GameplayViewModel:
  case Empty()
  case InProgress(
      prevTetrominoPositions: Option[NonEmptyBatch[Vector2]],
      targetTetrominoPositions: NonEmptyBatch[Vector2],
      from: Seconds
  )

object GameplayViewModel:
  def initial: GameplayViewModel.Empty = GameplayViewModel.Empty()

  // TODO: move to view ?
  extension (viewModel: GameplayViewModel)
    def tetrominoPositions(ctx: GameContext): Batch[Point] =
      lazy val ctxGrindPoint = toGridPoint(ctx).andThen(_.toPoint)
      lazy val targetPositions = (vm: GameplayViewModel.InProgress) =>
        vm.targetTetrominoPositions.map(ctxGrindPoint)

      viewModel match
        // format: off
        case vm @ GameplayViewModel.InProgress(Some(prevTetrominoPositions), _, _) =>
           (prevTetrominoPositions.map(ctxGrindPoint) zip targetPositions(vm))
                .map(
                  Signal
                    .Lerp(_, _, Seconds(0.093))
                    .at(ctx.gameTime.running - vm.from)
                ).toBatch
        // format: on
        case vm: GameplayViewModel.InProgress => targetPositions(vm).toBatch
        case _                                => Batch.empty

    def onFrameTick(
        ctx: GameContext,
        model: GameplayModel
    ): Outcome[GameplayViewModel] =
      (model.state, viewModel) match
        case (m: GameplayState.InProgress, vm: GameplayViewModel.InProgress) =>
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

        case (m: GameplayState.InProgress, _: GameplayViewModel.Empty) =>
          Outcome(
            GameplayViewModel.InProgress(
              prevTetrominoPositions = None,
              targetTetrominoPositions = m.tetromino.positions,
              from = ctx.gameTime.running
            )
          )

        case (m: GameplayState.Initial, _) => 
          Outcome(GameplayViewModel.Empty())
        case _ => Outcome(viewModel)

def toGridPoint(ctx: GameContext)(point: Vector2) =
  point * ctx.startUpData.bootData.gridSquareSize
