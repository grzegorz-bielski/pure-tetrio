package pureframes.tetrio.app

import pureframes.tetrio.game.Tetrio.*
import tyrian.Html.*
import tyrian.*
import tyrian.cmds.*

object AppView:
  def view[F[_]](model: AppModel[F]): Html[AppMsg] =
    div(`class` := "main")(
      div(`class` := "game", id := gameNodeId)(),
      div(`class` := "ui")(
        div(`class` := "btns")(
          button(onClick(AppMsg.Pause))("Pause"),
          Controls.view(model.controls).map(AppMsg.ControlsUpdate(_))
        ),
        div()(s"Is in progress ${model.gameInProgress}"),
        model.gameProgress
          .map { progress =>
            ul()(
              li()(s"level: ${progress.level}"),
              li()(s"lines: ${progress.lines}"),
              li()(s"score: ${progress.score}")
            )
          }
          .getOrElse(div()) // TODO: how to present empty elements?
      )
    )

