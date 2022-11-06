import org.scalajs.linker.interface.ModuleSplitStyle

Global / onChangedBuildSource := ReloadOnSourceChanges
ThisBuild / scalafixDependencies += Dependencies.organizeImports.value.head
ThisBuild / organization      := "pureframes"
ThisBuild / scalaVersion      := IO.read(file("./scalaVersion.txt"))
ThisBuild / version           := "0.0.1"
ThisBuild / scalafixOnCompile := true
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

lazy val puretetrio = project
  .in(file("."))
  .aggregate(frontend)

lazy val frontend = project
  .enablePlugins(ScalaJSPlugin)
  .settings(
    libraryDependencies ++= Seq.concat(
      Dependencies.Indigo.deps.value,
      Dependencies.Tyrian.deps.value,
      Dependencies.munit.value,
      Dependencies.pprint.value,
      Dependencies.dom.value,
      Dependencies.Pureframes.deps.value
    ),
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
        .withModuleSplitStyle(
          ModuleSplitStyle.SmallModulesFor(List("pureframes.tetrio"))
        )
        .withSourceMap(
          false
        ) // TODO: source map are not correctly loaded in vite :sad
    },
    scalaJSUseMainModuleInitializer := true,
    scalacOptions ++= Seq(
      // "-language:strictEquality" TODO: fix bugs
    )
  )

addCommandAlias("dev", "~ fastLinkJS; frontend / run")
addCommandAlias("build", "fullLinkJS; frontend / run")
