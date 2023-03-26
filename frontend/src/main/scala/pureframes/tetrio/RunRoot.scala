package pureframes.tetrio

import pureframes.css.JSRender
import pureframes.tetrio.app.*
import pureframes.tetrio.app.components.*

import scala.scalajs.js

@main
def run =
  JSRender.toFiles(
    directory = "frontend/styles",
    AppView,
    Button,
    IndigoWrapper,
    Stats
  )
