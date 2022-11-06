package pureframes.tetrio

import pureframes.css.JSRender
import pureframes.tetrio.app.*
import pureframes.tetrio.app.components.*

import scala.scalajs.js

@main
def run =
  if js.typeOf(js.Dynamic.global.window) == "undefined" then
    js.dynamicImport {
      println("Generating stylesheets")
      JSRender.toFiles(
        "frontend/styles",
        AppView,
        ScreenControls,
        Button,
        IndigoWrapper,
        Stats,
      )
    }
  else 
    println("Running in browser - aborting stylesheet gen")
