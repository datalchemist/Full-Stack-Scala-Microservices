lazy val scalaV = "2.12.3"
val playJsonVersion = "2.6.8"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.0" % "provided"
val playJsonJs = "com.typesafe.play" %%%! "play-json" % playJsonVersion
val endpointsJvm = "org.julienrf" %% "endpoints-algebra" % "0.4.0"
val endpointsJs = "org.julienrf" %%%! "endpoints-algebra" % "0.4.0"
val endpointsPlayServer = "org.julienrf" %% "endpoints-play-server" % "0.4.0"
val endpointsXhrClientJs = "org.julienrf" %%%! "endpoints-xhr-client" % "0.4.0"

lazy val crossType = CrossType.Full

lazy val server = webApp.jvm
  .enablePlugins(PlayScala)
  .settings(
    name := "web-server",
  scalaVersion := scalaV,
  scalaJSProjects := Seq(webApp.js),
  EclipseKeys.eclipseOutput := Some("eclipse_target"),
  pipelineStages in Assets := Seq(scalaJSPipeline),
  pipelineStages := Seq(digest, gzip),
  // triggers scalaJSPipeline when using compile or continuous compilation
  compile in Compile <<= (compile in Compile) dependsOn scalaJSPipeline,
  libraryDependencies ++= Seq(
    "com.vmunier" %% "scalajs-scripts" % "1.1.1",
      macwire,
    endpointsJvm,
    endpointsPlayServer,
    filters,
    specs2 % Test
  ),
  // Compile the project before generating Eclipse files, so that generated .scala or .class files for views and routes are present
  EclipseKeys.preTasks := Seq(compile in Compile)
)

lazy val client = webApp.js
  .enablePlugins(ScalaJSPlugin, ScalaJSWeb)
  .settings(
    name := "web-client",
    scalaVersion := scalaV,
    EclipseKeys.eclipseOutput := Some("eclipse_target"),
    scalaJSUseMainModuleInitializer := true,
    relativeSourceMaps := true,
    skip in packageJSDependencies := false,
    scalacOptions ++= Seq("-Xmax-classfile-name","78"),
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
    libraryDependencies ++= Seq(
      playJsonJs,
      "org.scala-js" %%% "scalajs-dom" % "0.9.1",
      "org.scala-lang.modules" %% "scala-xml" % "1.0.6",
      "com.thoughtworks.binding" %%% "dom" % "11.0.0-M4",
      "com.thoughtworks.binding" %%% "futurebinding" % "11.0.0-M4",
      "fr.hmil" %%% "roshttp" % "2.0.2",
      endpointsJs,
      endpointsXhrClientJs
    )
  )

//lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared")).
//  settings(scalaVersion := scalaV).
//  jsConfigure(_ enablePlugins ScalaJSWeb)


lazy val webApp = (crossProject in file("."))
  .settings(
    EclipseKeys.eclipseOutput := Some("eclipse_target"),
    unmanagedSourceDirectories in Compile :=
      Seq((scalaSource in Compile).value) ++
        crossType.sharedSrcDir(baseDirectory.value, "main"),
    unmanagedSourceDirectories in Test :=
      Seq((scalaSource in Test).value) ++
        crossType.sharedSrcDir(baseDirectory.value, "test"),
    testOptions in Test := Seq(Tests.Filter(_.endsWith("Test"))))

//lazy val sharedJvm = shared.jvm
//lazy val sharedJs = shared.js

// loads the server project at sbt startup
onLoad in Global := (Command.process("project webAppJVM", _: State)) compose (onLoad in Global).value
