package pureframes.tetrio.app.components

import pureframes.tetrio.app.AppMsg
import pureframes.tetrio.game.Tetrio
import tyrian.Html.*
import tyrian.*

import scala.scalajs.js

object IndigoWrapper:

  @js.native
  @JSImport("@styles/components/indigo-wrapper.module.css")
  def root: String = js.native

  val nodeId = Tetrio.gameNodeId

  def view[M]: Html[M] =
    div(`class` := "absolute w-full h-screen m-0 p-0", id := nodeId)()