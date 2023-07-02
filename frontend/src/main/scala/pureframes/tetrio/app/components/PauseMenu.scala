package pureframes.tetrio.app.components

import pureframes.tetrio.app.AppMsg
import pureframes.tetrio.app.RouterView
import tyrian.Html.*
import tyrian.*

object PauseMenu:
    def view: Seq[Html[AppMsg]] = 
        Seq(title, menu)

    def title = 
        h2(
            clsx(
                "text-6xl/loose",
                "text-center",
                "font-extralight",
                "text-indigo-500",
                "pv-5",
            )
        )("Paused")

    def menu = ul(
            clsx(
                "flex", 
                "flex-col", 
                "gap-2"
            )
        ):
            List(
                MenuItem.view(
                    "Resume",
                    msg = Some: 
                        AppMsg.Pause,
                    emphasized = true
                ) ,
                MenuItem.view(
                    "Quit",
                    msg = Some:
                        AppMsg.InternalLink(RouterView.Home.fullPath)
                )
            )
    