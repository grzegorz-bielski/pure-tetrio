package com.init

import indigo.GameViewport
import indigoextras.geometry.BoundingBox

case class BootData(
    gridSize: BoundingBox,
    // gridSquareSize: Int,
    magnificationLevel: Int,
    viewport: GameViewport
)
object BootData:
  val default: BootData = {
    // val gridSquareSize = 12
    val gridSize = BoundingBox(
      x = 4,
      y = 2,
      width = 11,
      height = 21
    )
    val magnificationLevel = 20

    BootData(
      gridSize,
      // gridSquareSize,
      magnificationLevel,
      GameViewport(
        width = 20 * magnificationLevel,
        height = 25 * magnificationLevel
        // width = 400,
        // height = 500
        // gridSquareSize * gridSize.width.toInt * magnificationLevel,
        // gridSquareSize * gridSize.height.toInt * magnificationLevel
        // gridSize.width.toInt * magnificationLevel,
        // gridSize.height.toInt * magnificationLevel
        
      )
    )
  }
