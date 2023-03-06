package pureframes.tetrio.app.components

import pureframes.css.*
import pureframes.tetrio.app.*
import pureframes.tetrio.app.components.*
import pureframes.tetrio.game.Tetrio.*
import tyrian.Html.*
import tyrian.*

object Stats extends Styled:
  def view[F[_]](using model: AppModel[F]): Html[AppMsg] =
    div(`class` := styles.className)(
      div(
        button(onClick(AppMsg.Pause))("Pause"),
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

  val styles = css"""
    position: absolute;
    top: 0;
    right: 0;
  """
