package pureframes.tetrio.app.components

import pureframes.css.*
import pureframes.tetrio.app.AppMsg
import tyrian.Html.*
import tyrian.*

object ScreenControls extends Styles:
  def view: Html[AppMsg] =
    div(
      `class` := styles.className
    )(
      Button(onClick(AppMsg.Pause))("Left"),
      Button(onClick(AppMsg.Pause))("Down"),
      Button(onClick(AppMsg.Pause))("Right")
    )

  val styles = css"""
        position: absolute;
        bottom: 0;
        right: 0;

        button {
            color: tomato;
        }
    """
