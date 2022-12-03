package pureframes.tetrio.app

import pureframes.css.*
import pureframes.tetrio.app.components.*
import pureframes.tetrio.game.Tetrio.*
import tyrian.Html.*
import tyrian.*
import tyrian.cmds.*

object AppView extends Styled:
  def view[F[_]](using model: AppModel[F]): Html[AppMsg] =
    div(`class` := styles.className)(
      IndigoWrapper.view,
      Stats.view,
      ScreenControls.view
    )

  val styles = css"""
    position: relative;
    width: 100%;
    height: 100vh;
    margin: 0;
    padding: 0;
    margin: 0 auto;
    overflow: hidden;
  """

