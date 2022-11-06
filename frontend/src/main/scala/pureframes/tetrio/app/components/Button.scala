package pureframes.tetrio.app.components

import pureframes.css.*
import pureframes.tetrio.app.AppMsg
import tyrian.Html.*
import tyrian.*

object Button extends Styles:
  def apply[M](attributes: Attr[M]*)(str: String): Html[M] =
    apply(attributes*)(text(str))

  def apply[M](attributes: Attr[M]*)(children: Elem[M]*): Html[M] =
    button(
      (`class` := styles.className) +: attributes.toList
    )(children.toList)

  val styles = css"""
        border-radius: 8px;
        border: 1px solid transparent;
        padding: 0.6em 1.2em;
        font-size: 1em;
        font-weight: 500;
        font-family: inherit;
        cursor: pointer;
        transition: border-color 0.25s;

        &:hover  {
            border-color: #646cff;
        }

        &:focus,
        &:focus-visible {
            outline: 4px auto -webkit-focus-ring-color;
        }
    """
