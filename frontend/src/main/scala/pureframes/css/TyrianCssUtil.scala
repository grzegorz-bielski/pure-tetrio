package pureframes.css

import tyrian.Html.*
import tyrian.*

def clsx(classNames: (String | Css | Option[Css | String])*): Attribute =
  cls := classNames
    .collect {
      case a: String       => a
      case a: Css          => a.className
      case Some(a: String) => a
      case Some(a: Css)    => a.className
    }
    .mkString(" ")
