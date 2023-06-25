package pureframes.tetrio.app

import pureframes.tetrio.app.components.*
import pureframes.tetrio.game.Tetrio.*
import tyrian.Html.*
import tyrian.*
import tyrian.cmds.*

import scala.scalajs.js

object AppView:
  @js.native
  @JSImport("@styles/app-view.module.css")
  val root: String = js.native

  def view[F[_]](using model: AppModel[F]): Html[AppMsg] =
    div(`class` := root)(
      IndigoWrapper.view,
      Stats.view
    )

