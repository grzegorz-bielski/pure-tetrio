package pureframes.tetrio
package game.core

import indigo.GameViewport
import indigo.shared.datatypes.Vector2
import indigoextras.geometry.BoundingBox

case class BootData(
    gridSize: BoundingBox,
    scale: Vector2,
    gridSquareSize: Int,
    magnificationLevel: Int,
    viewport: GameViewport,
    gameAssets: Assets
)
object BootData:
  val default: BootData = {
    val magnificationLevel = 1
    val gridSquareSize     = 32 // game asset actual size
    val gridSize = BoundingBox(
      x = 0,
      y = 2,
      width = 11,
      height = 22
    )
    val scale  = Vector2(0.75)
    val width  = (12 * gridSquareSize * scale.x).toInt
    val height = (25 * gridSquareSize * scale.y).toInt

    BootData(
      gridSize = gridSize,
      scale = scale,
      gridSquareSize = gridSquareSize,
      magnificationLevel = magnificationLevel,
      gameAssets = Assets(
        tetrominos = Assets.Tetrominos(gridSquareSize)
      ),
      viewport = GameViewport(width, height)
    )
  }
