package pureframes.tetrio.app

import org.scalajs.dom.*
import pureframes.tetrio.app.components.*
import pureframes.tetrio.game.core.*
import pureframes.tetrio.game.scenes.gameplay.GameplayCommand
import pureframes.tetrio.game.scenes.gameplay.model.Progress

enum AppMsg:
  case StartGame
  case StopGame
  case Pause
  case Noop
  case GameNodeMounted(e: Element)
  case Resize(canvasSize: CanvasSize)
  case UpdateProgress(progress: Progress, inProgress: Boolean)
  case ControlsUpdate(m: Controls.Msg)
  case Input(cmd: GameplayCommand)
  case FollowLink(href: String, isExternal: Boolean)