package pureframes.tetrio.app.components

import pureframes.tetrio.app.AppMsg
import pureframes.tetrio.app.RouterView
import tyrian.Html.*
import tyrian.*

object Home:
  def view: Html[AppMsg] = 
    div(
      clsx("bg-gradient-to-b from-pink-200")
    ):
      div(
        clsx(
          "container", "mx-auto", 
          "py-14",
          "px-8",
          "m-0",
          "flex",
          "flex-col",
          "justify-between",
          "h-screen",
        )
      )(
        title("Tetrio"),
        menu
      )

  def menu = ul(
      clsx("flex", "flex-col", "gap-2")
    ):
      List(
        MenuItem.view(
          "Play Now!",
          emphasized = true,
          msg = Some:
            AppMsg.FollowLink(RouterView.Game.fullPath, isExternal = false)
        ) ,
        MenuItem.view("Stats"),
        MenuItem.view("Settings"),
        MenuItem.view("About")
      )
  
  def title(txt: String) = h1(
      clsx(
        "text-9xl/loose",
        "italic",
        "font-extralight",
        "tracking-wider",
        "text-center",
        "text-indigo-500"
      )
    )(txt)
