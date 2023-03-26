package pureframes.tetrio.app.components

import tyrian.Html.*
import tyrian.*

import scala.scalajs.js
import scala.scalajs.js.annotation.*

object Controls:
  opaque type Model = Boolean

  def init: Model = false

  enum Msg:
    case Toggle
    case Close
    case Open

  def view(model: Model): Html[Msg] =
    div()(
      modal(model),
      button(onClick(Msg.Toggle))(
        if model then "Hide controls" else "Show controls"
      )
    )

  def update(msg: Msg, model: Model): Model =
    msg match
      case Msg.Toggle => !model
      case Msg.Close  => false
      case Msg.Open   => true

  private def modal(open: Boolean) =
    Tag[Msg](
      "fw-modal",
      List(
        Attribute("is-open", open.toString),
        Attribute("slider", "true"),
        Event("fwClose", _ => Msg.Close),
        Event("fwOpen", _ => Msg.Open),
        Event("fwSubmit", _ => Msg.Close)
      ),
      // TODO: use dynamic game control mappings and infer it from there
      List(
        h1("Controls"),
        h2("Keyboard"),
        ul(
          li("Space       - Hard Drop"),
          li("Left Arrow  - Move Left"),
          li("Right Arrow - Move Right"),
          li("Down Arrow  - Move Down"),
          li("Up Arrow    - Rotate Clockwise"),
          li("Q           - Rotate Counter Clockwise"),
          li("W           - Rotate Clockwise"),
          li("P           - Pause"),
          li("H           - Hold / Swap"),
        ),
        h2("Keyboard (Debug)"),
        ul(
          li("I - Spawn I Tetromino"),
          li("J - Spawn J Tetromino"),
          li("L - Spawn L Tetromino"),
          li("O - Spawn O Tetromino"),
          li("S - Spawn S Tetromino"),
          li("T - Spawn T Tetromino"),
          li("Z - Spawn Z Tetromino"),
          li("R - Reset")
        ),
        h2("Touch / Mouse"),
        ul(
          li("Swipe Down - Hard Drop"),
          li("Pan Left   - Move Left"),
          li("Pan Right  - Move Right"),
          li("Pan Down   - Move Down"),
          li("Swipe Up   - Hold / Swap"),
          li("Tap        - Rotate Clockwise"),
        )
      )
    )
