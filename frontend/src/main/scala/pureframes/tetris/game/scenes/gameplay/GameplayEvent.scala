package pureframes.tetris.game.scenes.gameplay

import pureframes.tetris.game.scenes.*

enum GameplayEvent extends SceneEvent:
    case ProgressUpdated(inProgress: Boolean)