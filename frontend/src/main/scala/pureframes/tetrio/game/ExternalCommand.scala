package pureframes.tetrio.game

import pureframes.tetrio.game.scenes.gameplay.model.Progress

enum ExternalCommand:
    case Pause
    case UpdateProgress(progress: Progress, inProgress: Boolean)