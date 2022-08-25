package pureframes.tetrio
package game.core

import cats.syntax.all.*
import indigo.GameViewport
import indigo.shared.Outcome
import indigo.shared.datatypes.Vector2
import indigoextras.geometry.BoundingBox
import pureframes.tetrio.game.core.*

case class BootData(
    gridSize: BoundingBox,
    scale: Vector2,
    gridSquareSize: Int,
    magnificationLevel: Int,
    initialViewport: GameViewport,
    gameAssets: Assets,
    spawnPoint: Vector2
)
object BootData:
  val gridWidth          = 11
  val gridWidthExternal  = gridWidth + 1
  val gridHeight         = 22
  val gridHeightExternal = gridHeight + 3
  val gridSquareSize     = 32 // game asset actual size in px

  val defaultCanvasSize =
    CanvasSize(500, 600, 1)

  private val magnificationLevel = 1

  def fromFlags(flags: Map[String, String]): Outcome[BootData] =
    Outcome {
      val initialCanvasSize =
        (
          flags.get("width").flatMap(_.toIntOption),
          flags.get("height").flatMap(_.toIntOption)
        )
          .mapN(CanvasSize.unsafeFromClientSizes)
          .getOrElse(defaultCanvasSize)

      fromCanvasSize(initialCanvasSize)
    }

  def default: BootData =
    fromCanvasSize(defaultCanvasSize)

  def fromCanvasSize(canvasSize: CanvasSize): BootData =
    val gridSize = BoundingBox(
      x = 0,
      y = 2,
      width = gridWidthExternal,
      height = gridHeightExternal
    )

    BootData(
      gridSize = gridSize,
      // TODO: dpr could be changed during gameplay -> moving game to different screen
      scale = Vector2(canvasSize.dpr),
      gridSquareSize = gridSquareSize,
      magnificationLevel = magnificationLevel,
      gameAssets = Assets(
        tetrominos = Assets.Tetrominos(gridSquareSize)
      ),
      initialViewport = canvasSize.toViewport,
      spawnPoint = Vector2(
        x = gridSize.x + math.floor(gridSize.width / 2),
        y = gridSize.y + 1
      )
    )
