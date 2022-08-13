package pureframes.tetris
package game

import indigo.*
import indigo.shared.Outcome
import pureframes.tetris.game.scenes.gameplay.viewmodel.GameplayViewModel

final case class GameViewModel(gameplay: GameplayViewModel)

object GameViewModel:
  def initial = GameViewModel(
    gameplay = GameplayViewModel.initial
  )
