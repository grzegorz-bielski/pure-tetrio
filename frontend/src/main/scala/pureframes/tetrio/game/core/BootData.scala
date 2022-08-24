package pureframes.tetrio
package game.core

import indigo.GameViewport
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

  private val magnificationLevel = 1
  // TODO: should be based on dpr
  private val scale              = Vector2(2) 

  def fromFlags(flags: Map[String, String]): BootData = 
    val width  = flags.get("width").flatMap(_.toIntOption)
    val height = flags.get("height").flatMap(_.toIntOption)

    val initialViewPort = (
      for
        w <- width
        h <- height
      yield CanvasSize.unsafeFromClientSizes(w, h).toViewport
    ).getOrElse(GameViewport(500, 600))

    fromInitalViewport(initialViewPort)

  def default: BootData = 
    fromInitalViewport(GameViewport(500, 600))

  def fromInitalViewport(initialViewport: GameViewport): BootData =
    val gridSize = BoundingBox(
      x = 0,
      y = 2,
      width = gridWidthExternal,
      height = gridHeightExternal
      // width = (gridWidthExternal * gridSquareSize * scale.x).toInt,
      // height = (gridHeightExternal * gridSquareSize * scale.y).toInt
    )

    BootData(
      gridSize = gridSize,
      scale = scale,
      gridSquareSize = gridSquareSize,
      magnificationLevel = magnificationLevel,
      gameAssets = Assets(
        tetrominos = Assets.Tetrominos(gridSquareSize)
      ),
      initialViewport = initialViewport,
      spawnPoint = Vector2(
        x = gridSize.x + math.floor(gridSize.width / 2),
        y = gridSize.y + 1
      )
    )
