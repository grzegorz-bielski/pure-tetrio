import org.scalajs.linker.interface.ModuleSplitStyle

Global / onChangedBuildSource := ReloadOnSourceChanges
ThisBuild / scalafixDependencies += Dependencies.organizeImports.value.head
ThisBuild / organization      := "eu.pureframes"
ThisBuild / scalaVersion      := IO.read(file("./scalaVersion.txt"))
ThisBuild / version           := "0.0.1"
ThisBuild / scalafixOnCompile := true
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

ThisBuild / resolvers +=
  "sonatype-s01-snapshots" at "https://s01.oss.sonatype.org/content/repositories/snapshots"

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
          // check: https://www.npmjs.com/package/source-map-support (?)
    },
    scalaJSUseMainModuleInitializer := true,
    scalacOptions ++= Seq(
      // "-language:strictEquality" TODO: fix bugs
    )
  )

addCommandAlias("dev", "~ fastLinkJS; frontend / run")
addCommandAlias("build", "fullLinkJS; frontend / run")
