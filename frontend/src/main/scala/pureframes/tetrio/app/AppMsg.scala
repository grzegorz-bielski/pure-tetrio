package pureframes.tetrio.app

import org.scalajs.dom.*
import pureframes.tetrio.game.core.*
import pureframes.tetrio.game.scenes.gameplay.model.Progress

enum AppMsg:
  case StartGame
  case Pause
  case Noop
  case GameNodeMounted(e: Element)
  case Resize(canvasSize: CanvasSize)
  case UpdateProgress(progress: Progress, inProgress: Boolean)
  case ControlsUpdate(m: Controls.Msg)