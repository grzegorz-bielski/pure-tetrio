package com.init

import indigo.GameViewport
import indigoextras.geometry.BoundingBox

case class BootData(
    gridSize: BoundingBox,
    gridSquareSize: Int,
    magnificationLevel: Int,
    viewport: GameViewport
)
object BootData:
  val default: BootData = {
    val gridSquareSize = 12
    val gridSize = BoundingBox(
      x = 0,
      y = 0,
      width = 30,
      height = 20
    )
    val magnificationLevel = 2

    BootData(
      gridSize,
      gridSquareSize,
      magnificationLevel,
      GameViewport(
        gridSquareSize * gridSize.width.toInt * magnificationLevel,
        gridSquareSize * gridSize.height.toInt * magnificationLevel
      )
    )
  }
