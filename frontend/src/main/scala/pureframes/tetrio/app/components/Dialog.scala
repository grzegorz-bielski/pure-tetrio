package pureframes.tetrio.app.components

import cats.effect.kernel.Async
import org.scalajs.dom
import pureframes.tetrio.app.AppMsg
import pureframes.tetrio.app.RouterView
import tyrian.Html.*
import tyrian.*

object Dialog:
    val viewId = "pause-menu"
    
    @SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
    private def element = Option: 
        dom.document.getElementById(viewId).asInstanceOf[dom.html.Dialog]

    def show[F[_]: Async] = Cmd.SideEffect:
        element.foreach(_.showModal())

    def hide[F[_]: Async] = Cmd.SideEffect:
        element.foreach(_.close())
        
    def view[M](attributes: Attr[M]*)(children: Elem[M]*) =
        dialog(
            (id := viewId) +: clsx(
                "rounded",
                "p-12", 
                "pt-0", 
                "bg-white/95",
                "backdrop:bg-indigo-500/20",
                "outline-0"
            ) +: attributes.toList
        )(children.toList)

    
