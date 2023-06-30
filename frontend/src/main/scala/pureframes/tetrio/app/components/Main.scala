package pureframes.tetrio.app.components

import pureframes.tetrio.app.AppMsg
import pureframes.tetrio.app.RouterView
import tyrian.Html.*
import tyrian.*

object Main:
  def view: Html[AppMsg] = div(
    clsx(
      "container",
      "mx-auto",
      "py-14",
      "px-8",
      "m-0",
      "flex",
      "flex-col",
      "justify-between",
      "h-screen"
    )
  )(
    h1(
      clsx(
        "text-8xl",
        "font-extralight",
        "tracking-wider",
        "text-center",
        "text-indigo-500"
      )
    )("Tetrio"),
    ul(
      clsx("flex", "flex-col", "gap-2")
    )(
      List(
        menuItem:
          btn(
            "Play Now!",
            emphasized = true,
            msg = Some:
              AppMsg.FollowLink(RouterView.Game.fullPath, isExternal = false)
          )
        ,
        menuItem(btn("Stats")),
        menuItem(btn("Settings")),
        menuItem(btn("About"))
      )
    )
  )

  private def menuItem[M](item: Html[M]): Html[M] = li(
    clsx("contents")
  )(item)

  private def btn[M](
      txt: String,
      emphasized: Boolean = false,
      msg: Option[M] = None
  ): Html[M] =
    val classNames =
      cls := List(
        "grow",
        "font-bold",
        "py-2",
        "px-4",
        "h-12",
        "rounded",
        "focus:outline-none",
        "focus-visible:ring",
        "text-center",
        "cursor-pointer"
      )
        .concat(
          if emphasized then
            List(
              "bg-indigo-500",
              "hover:bg-indigo-700",
              "text-white"
            )
          else
            List(
              "border-2",
              "border-indigo-500",
              "text-indigo-500",
              "hover:bg-indigo-700",
              "hover:text-white"
            )
        )
        .mkString(" ")

    button(classNames +: msg.toList.map(onClick))(txt)
