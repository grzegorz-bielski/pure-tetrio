package pureframes.tetrio.game.core

import indigo.*

case class SetupData(bootData: BootData, spawnPoint: Vector2)
object SetupData:
  def initial(bootData: BootData): SetupData =
    SetupData(
      bootData,
      spawnPoint = Vector2(
        x = math.floor(bootData.gridSize.width / 2), 
        y = bootData.gridSize.y + 1
      )
    )
