package com.core

import com.scenes.gameplay.viewmodel.GameplayViewModel
import indigo.*
import indigo.shared.Outcome

final case class GameViewModel(gameplay: GameplayViewModel)

object GameViewModel:
  def initial = GameViewModel(
    gameplay = GameplayViewModel.initial
  )
