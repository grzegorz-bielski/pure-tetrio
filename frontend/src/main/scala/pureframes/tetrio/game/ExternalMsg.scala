package pureframes.tetrio.game

import pureframes.tetrio.game.core.*
import pureframes.tetrio.game.scenes.gameplay.*
import pureframes.tetrio.game.scenes.gameplay.model.Progress

sealed trait ExternalMsg

enum ExternalCommand extends ExternalMsg:
    case Pause
    case CanvasResize(canvasSize: CanvasSize)
    case Input(cmd: GameplayCommand)

enum ExternalEvent extends ExternalMsg:
    case ProgressUpdated(state: GameState, progress: Option[Progress])
