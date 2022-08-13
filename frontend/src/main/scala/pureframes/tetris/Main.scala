package pureframes.tetris

import cats.effect.IO
import org.scalajs.dom.document
import pureframes.tetris.game.ExternalCommand
import pureframes.tetris.game.*
import tyrian.Html.*
import tyrian.*

import scala.scalajs.js.annotation.*

enum Msg:
    case StartGame
    case Pause
    case UpdateProgress(inProgress: Boolean)

@JSExportTopLevel("TyrianApp")
object Main extends TyrianApp[Msg, Model]:
    val gameDivId = "game-container"

    def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
        (Model.init, Cmd.Emit(Msg.StartGame))

    def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
        case Msg.StartGame =>
            (
                model,
                Cmd.SideEffect {
                    Tetris(model.bridge.subSystem(IndigoGameId(gameDivId)))
                        .launch(
                            gameDivId,
                            "width" -> "550",
                            "height" -> "400"
                        )
                }
            )
        case Msg.Pause => 
            (
                model,
                model.bridge.publish(IndigoGameId(gameDivId), ExternalCommand.Pause)
            )
        case Msg.UpdateProgress(inProgress) => 
            (
                model.copy(
                    gameInProgress = inProgress
                ),
                Cmd.None
            )


    def view(model: Model): Html[Msg] =
        div(`class` := "main")(
            div(`class` := "game", id := gameDivId)(),
            div(`class`:= "counter")(),
            div(`class`:= "btn")(
                button(onClick(Msg.Pause))("Pause")
            ),
            div()(s"Is in progress ${model.gameInProgress}")
        )

    def subscriptions(model: Model): Sub[IO, Msg] = 
        model.bridge.subscribe { 
            case m: ExternalCommand.UpdateProgress => Some(Msg.UpdateProgress(m.inProgress))
            case _ => None
        }

    def main(args: Array[String]): Unit = 
        Tyrian.start(document.getElementById("main"), init(Map()), update, view, subscriptions, 1024)

case class Model(bridge: TyrianIndigoBridge[IO, ExternalCommand], gameInProgress: Boolean)
object Model:
  val init: Model = Model(TyrianIndigoBridge(), false)