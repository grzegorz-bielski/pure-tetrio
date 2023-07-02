package pureframes.tetrio.app.components

import tyrian.Html.*
import tyrian.*

export scala.scalajs.js.annotation.JSImport

/** Helper function to create a CSS's `class` attribute from a list of classes
  */
inline def clsx(inline classNames: String*): Attribute = cls := classNames.mkString(" ")
