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
    gameAssets: Assets,
    spawnPoint: Vector2
)
object BootData:
  val gridWidth          = 11
  val gridWidthExternal  = gridWidth + 1
  val gridHeight         = 22
  val gridHeightExternal = gridHeight + 3
  val gridSquareSize     = 32 // game asset actual size in px

  private val magnificationLevel = 1
  private val scale              = Vector2(1)

  def fromScreenSize(width: Int, height: Int): BootData =
    fromBoundingBox(
      BoundingBox(
        x = (width / 2 / gridSquareSize) - gridWidth / 2 ,
        y = (height / 2 / gridSquareSize) - gridHeight / 2 ,
        width = width,
        height = height
      )
    )

  def default: BootData =
    // minimal working sizes
    fromBoundingBox(
      BoundingBox(
        x = 0,
        y = 2,
        width = (gridWidthExternal * gridSquareSize * scale.x).toInt,
        height = (gridHeightExternal * gridSquareSize * scale.y).toInt
      )
    )

  def fromBoundingBox(boundingBox: BoundingBox): BootData =
    val gridSize = BoundingBox(
      x = boundingBox.x,
      y = boundingBox.y,
      width = gridWidth,
      height = gridHeight
    )

    BootData(
      gridSize = gridSize,
      scale = scale,
      gridSquareSize = gridSquareSize,
      magnificationLevel = magnificationLevel,
      gameAssets = Assets(
        tetrominos = Assets.Tetrominos(gridSquareSize)
      ),
      viewport =
        GameViewport(boundingBox.width.toInt, boundingBox.height.toInt),
      spawnPoint = Vector2(
        x = gridSize.x + math.floor(gridSize.width / 2),
        y = gridSize.y + 1
      )
    )
