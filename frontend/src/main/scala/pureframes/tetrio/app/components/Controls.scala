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
        h2("Standard"),
        ul(
          li("Key.SPACE       => HardDrop"),
          li("Key.LEFT_ARROW  => Move(Vector2(-1, 0))"),
          li("Key.RIGHT_ARROW => Move(Vector2(1, 0))"),
          li("Key.DOWN_ARROW  => Move(Vector2(0, 1))"),
          li("Key.KEY_Q       => Rotate(CounterClockwise)"),
          li("Key.KEY_W       => Rotate(Clockwise)"),
          li("Key.KEY_P       => Pause")
        ),
        h2("Debug"),
        ul(
          li("Key.KEY_I => SpawnTetromino(Tetromino.i(spawnPoint))"),
          li("Key.KEY_J => SpawnTetromino(Tetromino.j(spawnPoint))"),
          li("Key.KEY_L => SpawnTetromino(Tetromino.l(spawnPoint))"),
          li("Key.KEY_O => SpawnTetromino(Tetromino.o(spawnPoint))"),
          li("Key.KEY_S => SpawnTetromino(Tetromino.s(spawnPoint))"),
          li("Key.KEY_T => SpawnTetromino(Tetromino.t(spawnPoint))"),
          li("Key.KEY_Z => SpawnTetromino(Tetromino.z(spawnPoint))"),
          li("Key.KEY_R => Reset")
        )
      )
    )
