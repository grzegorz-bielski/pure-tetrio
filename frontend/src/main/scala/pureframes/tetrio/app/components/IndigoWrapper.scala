package pureframes.tetrio.app.components

import pureframes.css.*
import pureframes.tetrio.app.AppMsg
import pureframes.tetrio.game.Tetrio
import tyrian.Html.*
import tyrian.*

object IndigoWrapper extends Styled:
  val nodeId = Tetrio.gameNodeId

  def view[M]: Html[M] =
    div(`class` := styles.className, id := nodeId)()

  val styles = css"""
    position: absolute;
    width: 100%;
    height: 100vh;
    margin: 0;
    padding: 0;

    > canvas {
      width: 100%;
      height: 100vh;
      margin: 0;
    }
  """