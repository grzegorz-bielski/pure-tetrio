package pureframes.tetrio.game.scenes.gameplay.viewmodel

import indigo.IndigoLogger.*
import indigo.*
import indigo.shared.Outcome
import indigo.shared.collections.Batch
import indigo.shared.collections.NonEmptyBatch
import indigoextras.geometry.Polygon
import indigoextras.geometry.Vertex
import indigoextras.subsystems.Automata
import pureframes.tetrio.game.core.*
import pureframes.tetrio.game.scenes.gameplay.model.*

import GameplayViewModel.*
import GameplayModel.*

case class GameplayViewModel(
    state: State,
    canvasSize: CanvasSize
):
  def onCanvasResize(nextCanvasSize: CanvasSize): GameplayViewModel =
    copy(canvasSize = nextCanvasSize)

  lazy val gameMapScale: Vector2 = canvasSize.scale
  def gameMapCoords(using ctx: GameContext): Point =
    import ctx.startUpData.bootData.{gridSize, gridSquareSize}

    Point(
      x =
        (canvasSize.drawingBufferWidth / 2 - gridSize.width * gridSquareSize * gameMapScale.x / 2).toInt,
      y =
        (canvasSize.drawingBufferHeight / 2 - gridSize.height * gridSquareSize * gameMapScale.y / 2).toInt
    )

  def currentTetrominoPositions(using ctx: GameContext): Batch[Point] =
    state match
      case vm: GameplayViewModel.State.InProgress =>
        if vm.prevTetrominoPositions.isEmpty then vm.targetPoints
        else
          (vm.prevPoints zip vm.targetPoints).map(
            Signal
              .Lerp(_, _, Seconds(0.093))
              .at(ctx.gameTime.running - vm.from)
          )
      case _ => Batch.empty

  def onFrameTick(
      model: GameplayModel,
      ctx: GameContext
  ): Outcome[GameplayViewModel] =
    given GameContext = ctx

    (model.state, state) match
      case (m: GameplayState.InProgress, vm: State.InProgress) =>
        if vm.targetTetrominoPositions == m.tetromino.positions then
          Outcome(this)
        else
          Outcome(
            copy(
              state = State.InProgress(
                prevTetrominoPositions =
                  currentTetrominoPositions.map(_.toVector),
                targetTetrominoPositions = m.tetromino.positions,
                from = ctx.gameTime.running
              )
            )
          )
      case (m: GameplayState.InProgress, _: State.Empty) =>
        Outcome(
          copy(
            state = State.InProgress(
              prevTetrominoPositions = Batch.empty,
              targetTetrominoPositions = m.tetromino.positions,
              from = ctx.gameTime.running
            )
          )
        )
      case (m: GameplayState.Initial, _) =>
        Outcome(copy(state = State.Empty()))
      case _ => Outcome(this)

object GameplayViewModel:
  def toGridPoint(point: Vector2)(using ctx: GameContext): Point =
    (point * ctx.startUpData.bootData.gridSquareSize).toPoint

  def initial(canvasSize: CanvasSize): GameplayViewModel =
    GameplayViewModel(
      state = State.Empty(),
      canvasSize = canvasSize
    )

  enum State:
    case Empty()

    /** @param prevTetrominoPositions
      *   actual tetromino coords from the last frame, in the display size
      * @param targetTetrominoPositions
      *   target tetromino coords for the current frame, in game map size
      * @param from
      *   tetromino coords update time
      */
    case InProgress(
        prevTetrominoPositions: Batch[Vector2],
        targetTetrominoPositions: NonEmptyBatch[Vector2],
        from: Seconds
    )

  extension (state: State.InProgress)
    def prevPoints(using GameContext): Batch[Point] =
      state.prevTetrominoPositions.map(_.toPoint)

    def targetPoints(using GameContext): Batch[Point] =
      state.targetTetrominoPositions.map(toGridPoint).toBatch
