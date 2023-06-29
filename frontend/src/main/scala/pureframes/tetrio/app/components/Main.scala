package pureframes.tetrio.app.components

import pureframes.tetrio.app.RouterView
import tyrian.Html.*
import tyrian.*

object Main:
  def view[M]: Html[M] = div(
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
        menuItem(
          menuButton(
            "Play Now!",
            path = RouterView.Game.path,
            emphasized = true
          )
        ),
        menuItem(menuButton("Stats")),
        menuItem(menuButton("Settings")),
        menuItem(menuButton("About"))
      )
    )

    // h2("A Tetris clone written in Scala 3"),
    // h3("By @pureframes"),
    // h4("Powered by Scala.js, Tyrian, and Indigo"),
    // h5("Source code available at")
  )

  private def menuItem[M](item: Html[M]): Html[M] = li(
    clsx("contents")
  )(item)

  private def menuButton[M](
      txt: String,
      path: String = "",
      emphasized: Boolean = false
  ): Html[M] =
    a(
      href := path,
      cls := List(
        "grow",
        "flex",
        "items-center",
        "justify-center",
        "font-bold",
        "py-2",
        "px-4",
        "h-12",
        "rounded",
        "focus:outline-none",
        "focus-visible:ring",
        "text-center",
        "cursor-pointer",
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
    )(txt)
