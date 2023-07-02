package pureframes.tetrio.game.scenes.gameplay

import pureframes.tetrio.game.scenes.*
import pureframes.tetrio.game.scenes.gameplay.model.Progress

enum GameState:
  case UnStarted, InProgress, Paused, Lost

enum GameplayEvent extends SceneEvent:
  case ProgressUpdated(state: GameState, progress: Option[Progress])
