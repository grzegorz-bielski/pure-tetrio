package pureframes.tetrio.game.scenes.gameplay

import pureframes.tetrio.game.scenes.*
import pureframes.tetrio.game.scenes.gameplay.model.Progress

enum GameplayEvent extends SceneEvent:
  case ProgressUpdated(progress: Progress, inProgress: Boolean)
