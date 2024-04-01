package pureframes.tetrio.app

import pureframes.tetrio.app.components.*
import pureframes.tetrio.game.scenes.gameplay.*
import tyrian.Html.*
import tyrian.*
import tyrian.cmds.*

import scala.scalajs.js

object AppView:
  def view[F[_]](using model: AppModel[F]): Html[AppMsg] =
    main(
      clsx(
        "relative", 
        "w-full", 
        "h-screen",
      )
    ):
      model.view match
        case RouterView.Home => List(Home.view)
        case RouterView.Game => List(
            IndigoWrapper.view, 
            Overlay.view, 
            modal(model.gameState)
          ) 

  def modal(state: GameState) = 
    state match
      case GameState.Paused => 
        //  AppMsg.Pause could be sent twice when clicking the button & onClose
        //   so we are relying on Key.ESCAPE in GameplayInput
        Dialog.view()(PauseMenu.view*)
      case GameState.Lost   => 
        Dialog.view(Event("close", _ => AppMsg.Reset))(GameOverMenu.view*)
      case _ => Dialog.view()()
