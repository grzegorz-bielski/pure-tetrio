package pureframes.tetris.game

import pureframes.tetris.game.scenes.gameplay.model.Progress

enum ExternalCommand:
    case Pause
    case UpdateProgress(progress: Progress, inProgress: Boolean)