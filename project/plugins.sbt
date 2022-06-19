addSbtPlugin("org.scala-js"               % "sbt-scalajs"  % "1.10.0")
addSbtPlugin("io.indigoengine"           %% "sbt-indigo"   % "0.13.0")
addSbtPlugin("io.github.davidgregory084"  % "sbt-tpolecat" % "0.3.1")
addSbtPlugin("ch.epfl.scala"              % "sbt-scalafix" % "0.9.31")
addSbtPlugin("com.github.reibitto"        % "sbt-welcome"   % "0.2.2")

libraryDependencies += "org.scala-js" %% "scalajs-env-jsdom-nodejs" % "1.1.0"
