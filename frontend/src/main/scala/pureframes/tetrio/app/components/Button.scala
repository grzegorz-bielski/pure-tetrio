package pureframes.tetrio.app.components

import pureframes.tetrio.app.AppMsg
import tyrian.Html.*
import tyrian.*

import scala.scalajs.js

object Button:
  @JSImport("@styles/components/button.module.css")
  @js.native
  def root: String = js.native

  def apply[M](attributes: Attr[M]*)(str: String): Html[M] =
    apply(attributes*)(text(str))

  def apply[M](attributes: Attr[M]*)(children: Elem[M]*): Html[M] =
    button(
      (cls := root) +: attributes.toList
    )(children.toList)
