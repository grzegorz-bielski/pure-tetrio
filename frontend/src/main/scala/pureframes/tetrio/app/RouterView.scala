package pureframes.tetrio.app

enum RouterView(val path: String):
    case Home extends RouterView("/")
    case Game extends RouterView("/game")

object RouterView:
     def unapply(arg: String): Option[RouterView] = 
        RouterView.values.find(_.path == arg).orElse(Option.when(
            // TODO: hack
            arg.startsWith("http") && arg.endsWith("/pure-tetrio/")
        )(RouterView.Home))
