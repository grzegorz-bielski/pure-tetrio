package pureframes.tetrio.app.components

import pureframes.tetrio.app.*
import pureframes.tetrio.app.components.*
import pureframes.tetrio.game.Tetrio.*
import pureframes.tetrio.game.scenes.gameplay.*
import pureframes.tetrio.game.scenes.gameplay.model.Progress
import tyrian.Html.*
import tyrian.*

import scala.scalajs.js

object Overlay:
  def view[F[_]](using model: AppModel[F]): Html[AppMsg] =
    (model.gameState, model.gameProgress) match
      case (GameState.InProgress, Some(progress: Progress)) =>
        div()(
          div(
            clsx(
              "select-none",
              "absolute bottom-0 right-0"
            )
          )(
            button(
              onClick(AppMsg.Pause),
              // prevent focus so gameplay is not interrupted by tabbing around,
              // there are external keybindings for pause
              tabIndex := -1,
              clsx(
                "outline-0",
                "font-bold",
                "rotate-90",
                "text-5xl",
                "text-indigo-500",
                "hover:text-indigo-700",
                "p-2"
              )
            )("=")
          ),
          div(
            clsx(
              "select-none",
              "absolute top-0 left-0",
              "p-2",
              "grid grid-cols-2 gap-y-1 gap-x-2"
            )
          )(
            stat("Score"),
            stat(progress.score.toString),
            stat("Level"),
            stat(progress.level.toString),
            stat("Lines"),
            stat(progress.lines.toString)
          )
        )

      case _ => div()

  def stat(txt: String) =
    span(
      clsx(
        "text-white", 
        "text-lg md:text-xl",
      )
    )(txt)
