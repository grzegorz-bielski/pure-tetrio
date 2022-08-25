package pureframes.tetrio
package game.scenes.gameplay.viewmodel

import indigo.IndigoLogger.*
import indigo.*
import indigo.shared.Outcome
import indigo.shared.collections.Batch
import indigo.shared.collections.NonEmptyBatch
import indigoextras.subsystems.Automata
import pureframes.tetrio.game.core.*
import pureframes.tetrio.game.scenes.gameplay.model.*

import GameplayViewModel.*
import GameplayModel.*

case class GameplayViewModel(state: State, viewport: GameViewport):
  def onViewportResize(nextViewport: GameViewport): GameplayViewModel =
    copy(viewport = nextViewport)

  def gameMapCoords(ctx: GameContext): Point =
    import ctx.startUpData.bootData.{gridSize, gridSquareSize, scale}

    Point(
      x =
        (viewport.width / 2 - gridSize.width * gridSquareSize * scale.x / 2).toInt,
      y =
        (viewport.height / 2 - gridSize.height * gridSquareSize * scale.y / 2).toInt
    )

  def onFrameTick(
      ctx: GameContext,
      model: GameplayModel
  ): Outcome[GameplayViewModel] =
    (model.state, state) match
      case (m: GameplayState.InProgress, vm: State.InProgress) =>
        if vm.targetTetrominoPositions == m.tetromino.positions then
          Outcome(this)
        else
          Outcome(
            copy(
              state = State.InProgress(
                prevTetrominoPositions = Some(vm.targetTetrominoPositions),
                targetTetrominoPositions = m.tetromino.positions,
                from = ctx.gameTime.running
              )
            )
          )
      case (m: GameplayState.InProgress, _: State.Empty) =>
        Outcome(
          copy(
            state = State.InProgress(
              prevTetrominoPositions = None,
              targetTetrominoPositions = m.tetromino.positions,
              from = ctx.gameTime.running
            )
          )
        )
      case (m: GameplayState.Initial, _) =>
        Outcome(copy(state = State.Empty()))
      case _ => Outcome(this)

object GameplayViewModel:
  def initial(viewport: GameViewport): GameplayViewModel =
    GameplayViewModel(
      state = State.Empty(),
      viewport = viewport
    )

  enum State:
    case Empty()
    case InProgress(
        prevTetrominoPositions: Option[NonEmptyBatch[Vector2]],
        targetTetrominoPositions: NonEmptyBatch[Vector2],
        from: Seconds
    )
