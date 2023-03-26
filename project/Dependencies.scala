import sbt._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

object Dependencies {
  object Indigo {
    val version = "0.14.1-SNAPSHOT"

    val deps: Deps = Def.setting {
      Seq(
        "io.indigoengine" %%% "indigo"            % version,
        "io.indigoengine" %%% "indigo-extras"     % version,
        "io.indigoengine" %%% "indigo-json-circe" % version
      )
    }
  }

  object Tyrian {
    val version = "0.6.0"

    val deps: Deps = Def.setting {
      Seq(
        "io.indigoengine" %%% "tyrian"               % version,
        "io.indigoengine" %%% "tyrian-io"            % version,
        "io.indigoengine" %%% "tyrian-indigo-bridge" % version
      )
    }
  }

  object Pureframes {
    val deps: Deps = Def.setting {
      Seq(
        "eu.pureframes" %%% "css-core" % "0.0.1-SNAPSHOT"
      )
    }
  }

  val munit: Deps = Def.setting(
    Seq("org.scalameta" %%% "munit" % "0.7.29" % Test)
  )

  val pprint: Deps = Def.setting(
    Seq("com.lihaoyi" %%% "pprint" % "0.7.0")
  )

  val dom: Deps = Def.setting(
    Seq("org.scala-js" %%% "scalajs-dom" % "2.2.0")
  )

  val organizeImports: Deps = Def.setting(
    Seq("com.github.liancheng" %% "organize-imports" % "0.5.0")
  )

  type Deps = Def.Initialize[Seq[ModuleID]]
}
