package pureframes.tetrio.app.components

import pureframes.tetrio.app.AppMsg
import pureframes.tetrio.game.Tetrio
import tyrian.Html.*
import tyrian.*

import scala.scalajs.js

object IndigoWrapper:
  val nodeId = Tetrio.gameNodeId

  def view[M]: Html[M] =
    div(clsx(
      "absolute w-full h-screen m-0 p-0",
      "bg-gradient-to-r from-pink-200",
      "select-none touch-manipulation",
    ), id := nodeId)()