import org.scalajs.linker.interface.ModuleSplitStyle

import sbtwelcome._

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.5.0"

lazy val indigoTetris = project
  .in(file("frontend"))
  .enablePlugins(ScalaJSPlugin, SbtIndigo)
  .settings(
    name         := "indigotetris",
    version      := "0.0.1",
    scalaVersion := IO.read(new File("./scalaVersion.txt")),
    organization := "com",
    libraryDependencies ++= Seq.concat(
        Dependencies.Indigo.deps.value,
        Dependencies.Tyrian.deps.value,
        Dependencies.munit.value,
        Dependencies.pprint.value,
        Dependencies.dom.value
    ),
    scalafixOnCompile := true,
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    scalaJSLinkerConfig ~= { 
      _.withModuleKind(ModuleKind.ESModule)
        .withModuleSplitStyle(
          ModuleSplitStyle.SmallModulesFor(List("pureframes.tetris"))
        )
      },
    // .withOutputPatterns(OutputPatterns.fromJSFile("%s.mjs"))
    Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) } 
  )
  .settings(
    showCursor            := true,
    title                 := "Indigo Tetris",
    gameAssetsDirectory   := "assets",
    windowStartWidth      := 550,
    windowStartHeight     := 400,
    disableFrameRateLimit := false,
    electronInstall       := indigoplugin.ElectronInstall.Global
  )
  .settings(
    logo := "Indigo Tetris (v" + version.value.toString + ")",
    usefulTasks := Seq(
      // UsefulTask("a", "runGame", "Run the game (requires Electron)"),
      // UsefulTask("b", "buildGame", "Build web version"),
      // UsefulTask(
      //   "c",
      //   "runGameFull",
      //   "Run the fully optimised game (requires Electron)"
      // ),
      // UsefulTask("d", "buildGameFull", "Build the fully optimised web version"),
      // UsefulTask("dev", "~ fastOptJS;indigoBuild", "Dev mode watcher")
    ),
    logoColor        := scala.Console.MAGENTA,
    aliasColor       := scala.Console.YELLOW,
    commandColor     := scala.Console.CYAN,
    descriptionColor := scala.Console.WHITE
  )


// addCommandAlias("buildGame", ";compile;fastOptJS;indigoBuild")
// addCommandAlias("buildGameFull", ";compile;fullOptJS;indigoBuildFull")
addCommandAlias("dev", "~fastLinkJS")
addCommandAlias("build", "fullLinkJS")
// addCommandAlias("runGameFull", ";compile;fullOptJS;indigoRunFull")
