package pureframes.tetrio.game.core

import cats.syntax.all.*
import indigo.*
import indigo.shared.Outcome
import indigo.shared.datatypes.Vector2
import pureframes.tetrio.game.core.*

case class BootData(
    gridSize: BoundingBox,
    scale: Vector2,
    gridSquareSize: Int,
    magnificationLevel: Int,
    initialCanvasSize: CanvasSize,
    gameAssets: Assets,
    spawnPoint: Vector2,
    gameOverLine: Int
)
object BootData:
  val gridWidth          = 10
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
      scale = canvasSize.scale ,
      gridSquareSize = gridSquareSize,
      magnificationLevel = magnificationLevel,
      gameAssets = Assets(
        tetrominos = Assets.Tetrominos(gridSquareSize)
      ),
      initialCanvasSize = canvasSize,
      spawnPoint = Vector2(
        x = gridSize.x + math.floor(gridSize.width / 2),
        y = gridSize.y + 1
      ),
      gameOverLine = gridSize.top.toInt + 2
    )
