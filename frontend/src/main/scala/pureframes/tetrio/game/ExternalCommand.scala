package pureframes.tetrio.game

import pureframes.tetrio.game.core.*
import pureframes.tetrio.game.scenes.gameplay.model.Progress

enum ExternalCommand:
    case Pause
    case CanvasResize(canvasSize: CanvasSize)
    case UpdateProgress(progress: Progress, inProgress: Boolean)