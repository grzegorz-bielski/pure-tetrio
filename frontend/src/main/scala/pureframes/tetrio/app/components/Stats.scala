package pureframes.tetrio.app.components

import pureframes.tetrio.app.*
import pureframes.tetrio.app.components.*
import pureframes.tetrio.game.Tetrio.*
import tyrian.Html.*
import tyrian.*

import scala.scalajs.js

object Stats:
  def view[F[_]](using model: AppModel[F]): Html[AppMsg] =
    div(`class` := "absolute right-0 top-0 text-white")(
      div(
        Button(onClick(AppMsg.Pause))("Pause"),
        Controls.view(model.controls).map(AppMsg.ControlsUpdate(_))
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
