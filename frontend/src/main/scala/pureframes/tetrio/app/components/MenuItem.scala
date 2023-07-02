package pureframes.tetrio.app.components

import tyrian.Html.*
import tyrian.*

object MenuItem:
  def view[M](
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

    li(clsx("contents")):
      button(classNames +: msg.toList.map(onClick))(txt)