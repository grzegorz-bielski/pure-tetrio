package pureframes.tetris.game.scenes.gameplay

import pureframes.tetris.game.scenes.*
import pureframes.tetris.game.scenes.gameplay.model.Progress

enum GameplayEvent extends SceneEvent:
  case ProgressUpdated(progress: Progress, inProgress: Boolean)
