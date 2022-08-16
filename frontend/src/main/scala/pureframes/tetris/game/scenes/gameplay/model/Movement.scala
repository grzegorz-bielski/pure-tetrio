package pureframes.tetris.game.scenes.gameplay.model

import indigo.*
import pureframes.tetris.game.core.*

import GameplayModel.*

final case class Movement private (
    movedTetromino: Tetromino,
    intersections: Batch[MapElement],
    point: Vector2
):
  lazy val intersects = !intersections.isEmpty

  lazy val minimalMovement =
    point == Vector2.zero || point.abs.max(1) == Vector2(1, 1)
  lazy val horizontalMovement = point.x != 0
  lazy val verticalMovement   = point.y != 0

  lazy val stackIntersections = intersections.collect {
    case e: MapElement.Floor  => e.point
    case e: MapElement.Debris => e.point
  }

  lazy val intersectedStack =
    !horizontalMovement && !stackIntersections.isEmpty

  def sticksOutOfTheMap(topInternal: Int) =
    intersectedStack && movedTetromino.positions.exists(_.y <= topInternal)

object Movement:
  def closestMovement(
      point: Vector2,
      state: GameplayState.InProgress
  ): Movement =
    val range = Vector2.zero --> point

    @scala.annotation.tailrec
    def go(i: Int, prev: Option[Movement]): Movement =
      val intersection =
        val point          = range(i)
        val movedTetromino = state.tetromino.moveBy(point)
        val intersections  = state.map.intersectsWith(movedTetromino.positions)
        Movement(movedTetromino, intersections, point)

      prev match
        case _ if intersection.intersects && intersection.minimalMovement =>
          intersection
        case Some(prev) if intersection.intersects => prev
        case _ if i == range.length - 1            => intersection
        case _ => go(i + 1, Some(intersection))

    go(0, None)
