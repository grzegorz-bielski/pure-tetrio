package pureframes.tetrio.app

import pureframes.tetrio.app.components.*
import tyrian.Html.*
import tyrian.*
import tyrian.cmds.*

import scala.scalajs.js

object AppView:
  def view[F[_]](using model: AppModel[F]): Html[AppMsg] =
    main(clsx("relative w-full h-screen overflow-hidden mx-auto m-0 p-0")):
      model.view match
        case RouterView.Home => List(Home.view)
        case RouterView.Game => List(IndigoWrapper.view, Stats.view)

