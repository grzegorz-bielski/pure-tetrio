import sbt._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

object Dependencies {
  object Indigo {
    val version = "0.13.0"

    val deps: Deps = Def.setting {
      Seq(
        "io.indigoengine" %%% "indigo"            % version,
        "io.indigoengine" %%% "indigo-extras"     % version,
        "io.indigoengine" %%% "indigo-json-circe" % version
      )
    }
  }

  object Tyrian {
    val version = "0.5.1"

    val deps: Deps = Def.setting {
      Seq(
        "io.indigoengine" %%% "tyrian"               % version,
        "io.indigoengine" %%% "tyrian-io"            % version,
        "io.indigoengine" %%% "tyrian-indigo-bridge" % version
      )
    }
  }

  val munit: Deps = Def.setting(
    Seq("org.scalameta" %%% "munit" % "0.7.29" % Test)
  )

  val pprint: Deps = Def.setting(
    Seq("com.lihaoyi" %%% "pprint" % "0.7.0")
  )

  type Deps = Def.Initialize[Seq[ModuleID]]
}
