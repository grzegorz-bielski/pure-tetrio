package pureframes.tetrio.app

import org.scalajs.dom.*
import pureframes.tetrio.app.components.*
import pureframes.tetrio.game.core.*
import pureframes.tetrio.game.scenes.gameplay.*
import pureframes.tetrio.game.scenes.gameplay.model.Progress

enum AppMsg:
  case StartGame
  case StopGame
  case Noop
  case GameNodeMounted(e: Element)
  case Resize(canvasSize: CanvasSize)
  case UpdateProgress(state: GameState, details: Option[Progress])
  case Input(cmd: GameplayCommand)
  case FollowLink(href: String, isExternal: Boolean)
object AppMsg:
  val Pause = Input(GameplayCommand.Pause)
  val Reset = Input(GameplayCommand.Reset)

  def ExternalLink(href: String) = FollowLink(href, isExternal = true)
  def InternalLink(href: String) = FollowLink(href, isExternal = false)