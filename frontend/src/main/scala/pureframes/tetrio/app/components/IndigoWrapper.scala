package pureframes.tetrio.app.components

import pureframes.css.*
import pureframes.tetrio.app.AppMsg
import pureframes.tetrio.game.Tetrio
import tyrian.Html.*
import tyrian.*

object IndigoWrapper extends Styles:
  def view[M]: Html[M] =
    div(`class` := styles.className, id := Tetrio.gameNodeId)()

  val styles = css"""
    position: absolute;
    width: 100%;
    height: 100vh;
    margin: 0;
    padding: 0;

    #game-container-indigo {
        width: 100%;
        height: 100vh;
        margin: 0;
    }
  """