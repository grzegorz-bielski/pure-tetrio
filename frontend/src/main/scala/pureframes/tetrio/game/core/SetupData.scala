package pureframes.tetrio.game.core

import indigo.*

case class SetupData(bootData: BootData, spawnPoint: Vector2)
object SetupData:
  def initial(bootData: BootData): SetupData =
    SetupData(
      bootData,
      bootData.spawnPoint
    )
