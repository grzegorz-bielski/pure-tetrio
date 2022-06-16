package com.model

import indigoextras.geometry.BoundingBox

case class GameModel(
    map: GameMap
)

object GameModel:
  def initial(grid: BoundingBox) = 
    GameModel(
        map = GameMap.walled(grid)
    )