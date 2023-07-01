package pureframes.tetrio.app.components

import cats.effect.kernel.Async
import org.scalajs.dom
import pureframes.tetrio.app.AppMsg
import pureframes.tetrio.app.RouterView
import tyrian.Html.*
import tyrian.*

object PauseMenu:
    val viewId = "pause-menu"
    
    @SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
    private def element = Option: 
        dom.document.getElementById(viewId).asInstanceOf[dom.html.Dialog]

    def show[F[_]: Async] = Cmd.SideEffect:
        element.foreach(_.showModal())

    def hide[F[_]: Async] = Cmd.SideEffect:
        element.foreach(_.close())
        
    def view = dialog(
        id := viewId,
        clsx(
            "rounded",
            "p-12", 
            "pt-0", 
            "bg-white/95",
            "backdrop:bg-indigo-500/20",
        ),
    )(
        h2(
            clsx(
                "text-6xl/loose",
                "text-center",
                "font-extralight",
                "text-indigo-500",
                "pv-5",
            )
        )(
            "Paused"
        ),
        menu

    )

    def menu = ul(
        clsx("flex", "flex-col", "gap-2")
        ):
        List(
            MenuItem.view(
                "Resume",
                msg = Some: 
                    AppMsg.Pause
            ) ,
            MenuItem.view("Controls"),
            MenuItem.view(
                "Quit",
                msg = Some:
                    AppMsg.FollowLink(RouterView.Home.fullPath, isExternal = false)
            )
        )
    
