package pureframes.tetrio
package game

import indigo.*
import indigo.shared.Outcome
import pureframes.tetrio.game.core.*
import pureframes.tetrio.game.scenes.gameplay.viewmodel.GameplayViewModel

final case class GameViewModel(gameplay: GameplayViewModel)

object GameViewModel:
  def initial(startupData: SetupData) = GameViewModel(
    gameplay = GameplayViewModel.initial(startupData.bootData.initialCanvasSize)
  )
