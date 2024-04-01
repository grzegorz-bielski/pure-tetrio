import sbt._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

object Dependencies {
  object Indigo {
    val version = "0.16.0"

    val deps: Deps = Def.setting {
      Seq(
        "io.indigoengine" %%% "indigo"               % version,
        "io.indigoengine" %%% "indigo-extras"        % version,
        "io.indigoengine" %%% "indigo-json-circe"    % version,
        "io.indigoengine" %%% "tyrian-indigo-bridge" % version
      )
    }
  }

  object Tyrian {
    val version = "0.9.0"

    val deps: Deps = Def.setting {
      Seq(
        "io.indigoengine" %%% "tyrian"               % version,
        "io.indigoengine" %%% "tyrian-io"            % version,
      )
    }
  }

  val munit: Deps = Def.setting(
    Seq("org.scalameta" %%% "munit" % "0.7.29" % Test)
  )

  val pprint: Deps = Def.setting(
    Seq("com.lihaoyi" %%% "pprint" % "0.8.1")
  )

  val dom: Deps = Def.setting(
    Seq("org.scala-js" %%% "scalajs-dom" % "2.8.0")
  )

  type Deps = Def.Initialize[Seq[ModuleID]]
}
