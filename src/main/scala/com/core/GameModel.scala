package com.core

import com.scenes.gameplay.model.GameplayModel
import indigo.*
import indigo.shared.Outcome
import indigoextras.geometry.BoundingBox

final case class GameModel(gameplay: GameplayModel)

object GameModel:
  def initial(grid: BoundingBox) =
    GameModel(
      gameplay = GameplayModel.initial(grid)
    )
