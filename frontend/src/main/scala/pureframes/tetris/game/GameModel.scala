package pureframes.tetris
package game

import indigo.*
import indigo.shared.Outcome
import indigoextras.geometry.BoundingBox
import pureframes.tetris.game.core.*
import pureframes.tetris.game.scenes.gameplay.model.GameplayModel

final case class GameModel(gameplay: GameplayModel)

object GameModel:
  def initial(setupData: SetupData) =
    GameModel(
      gameplay = GameplayModel.initial(setupData)
    )
