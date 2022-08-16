package pureframes.tetrio
package game

import indigo.*
import indigo.shared.Outcome
import indigoextras.geometry.BoundingBox
import pureframes.tetrio.game.core.*
import pureframes.tetrio.game.scenes.gameplay.model.GameplayModel

final case class GameModel(gameplay: GameplayModel)

object GameModel:
  def initial(setupData: SetupData) =
    GameModel(
      gameplay = GameplayModel.initial(setupData)
    )
