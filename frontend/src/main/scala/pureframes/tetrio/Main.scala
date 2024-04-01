package pureframes.tetrio

import cats.effect.IO
import cats.effect.kernel.Sync
import cats.syntax.all.*
import indigo.shared.collections.NonEmptyBatch
import org.scalajs.dom
import org.scalajs.dom.*
import org.scalajs.dom.document
import pureframes.tetrio.app.*
import pureframes.tetrio.game.Tetrio.*
import pureframes.tetrio.game.*
import pureframes.tetrio.game.core.*
import tyrian.Html.*
import tyrian.*
import tyrian.cmds.*

import scala.scalajs.js
import scala.scalajs.js.annotation.*

@JSExportTopLevel("TyrianApp")
object Main extends tyrian.TyrianIOApp[AppMsg, AppModel[IO]]:
  def router: tyrian.Location => AppMsg = Routing.basic(
    AppMsg.InternalLink(_),
    AppMsg.ExternalLink(_)
  )

  def init(flags: Map[String, String]): (AppModel[IO], Cmd[IO, AppMsg]) =
    (AppModel.init[IO], Cmd.None)

  def update(model: AppModel[IO]): AppMsg => (AppModel[IO], Cmd[IO, AppMsg]) =
    model.update

  def view(model: AppModel[IO]): Html[AppMsg] =
    AppView.view(using model)

  def subscriptions(model: AppModel[IO]): Sub[IO, AppMsg] =
    AppSubs.all(model)
