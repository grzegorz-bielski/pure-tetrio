package pureframes.tetrio.app

import org.scalajs.dom.*

import scala.scalajs.js
import scala.util.Try

@SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
def baseUrl = js.`import`.meta.env.BASE_URL.asInstanceOf[String]
def origin = window.location.origin

enum RouterView(val path: String):
  def basePath: String = baseUrl + path
  def fullPath: String = origin + basePath

  case Home extends RouterView("")
  case Game extends RouterView("game")

object RouterView:
  def unapply(arg: String): Option[RouterView] =
    Try(URL(arg)).toOption.flatMap: 
      _.pathname match 
        case str if str == Home.basePath => Some(RouterView.Home)
        case str if str == Game.basePath => Some(RouterView.Game)
        case _ => None