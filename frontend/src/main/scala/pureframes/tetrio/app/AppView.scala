package pureframes.tetrio.app

import pureframes.tetrio.app.components.*
import pureframes.tetrio.game.Tetrio.*
import tyrian.Html.*
import tyrian.*
import tyrian.cmds.*

import scala.scalajs.js

object AppView:
  def view[F[_]](using model: AppModel[F]): Html[AppMsg] =
    div(`class` := "relative w-full h-screen overflow-hidden mx-auto m-0 p-0")(
      IndigoWrapper.view,
      Stats.view
    )

