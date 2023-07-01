package pureframes.tetrio.app.components

import pureframes.tetrio.app.*
import pureframes.tetrio.app.components.*
import pureframes.tetrio.game.Tetrio.*
import tyrian.Html.*
import tyrian.*

import scala.scalajs.js

object Stats:
  def view[F[_]](using model: AppModel[F]): Html[AppMsg] =
    div(clsx(
      "absolute right-0 bottom-0",
      "text-black",
      "select-none"
    ))(
      div(
        Button(onClick(AppMsg.Pause))("Pause")
      ),
      model.gameProgress
        .map { progress =>
          ul()(
            li()(s"level: ${progress.level}"),
            li()(s"lines: ${progress.lines}"),
            li()(s"score: ${progress.score}")
          )
        }
        .getOrElse(div())
    )
