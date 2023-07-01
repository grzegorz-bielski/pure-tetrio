package pureframes.tetrio.app.components

import pureframes.tetrio.app.AppMsg
import pureframes.tetrio.app.RouterView
import pureframes.tetrio.game.scenes.gameplay.GameplayCommand
import tyrian.Html.*
import tyrian.*

object GameOverMenu:
    def view: Seq[Html[AppMsg]] = 
        Seq(
            title, 
            menu
        )

    def title = 
        h2(
            clsx(
                "text-4xl/loose",
                "md:text-6xl/loose",
                "text-center",
                "font-extralight",
                "text-rose-500",
                "pv-5",
            )
        )("Game Over")

    def menu = ul(
            clsx("flex", "flex-col", "gap-2")
        ):
           List(
                MenuItem.view(
                    "Retry?",
                    msg = Some:
                        AppMsg.Reset
                ),
                MenuItem.view(
                    "Quit",
                    msg = Some:
                        AppMsg.InternalLink(RouterView.Home.fullPath)
                )
           )
