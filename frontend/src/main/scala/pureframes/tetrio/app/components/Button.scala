package pureframes.tetrio.app.components

import pureframes.tetrio.app.AppMsg
import tyrian.Html.*
import tyrian.*

import scala.scalajs.js

object Button:
  def apply[M](attributes: Attr[M]*)(str: String): Html[M] =
    apply(attributes*)(text(str))

  def apply[M](attributes: Attr[M]*)(children: Elem[M]*): Html[M] =
    button(
      (cls := "bg-indigo-500 border font-medium cursor-pointer transition-[border-color] duration-[0.25s] px-[1.2em] py-[0.6em] rounded-lg border-solid border-transparent hover:border-[#646cff]") +: attributes.toList
    )(children.toList)
