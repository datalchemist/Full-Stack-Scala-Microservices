lazy val scalaV = "2.12.3"
val playJsonVersion = "2.6.8"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.0" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test

val playJsonJs = "com.typesafe.play" %%%! "play-json" % playJsonVersion

val scalaJsDom = "org.scala-js" %%%! "scalajs-dom" % "0.9.4"
val scalaXmlJs="org.scala-lang.modules" %% "scala-xml" % "1.0.6"

val scalaJsScripts="com.vmunier" %% "scalajs-scripts" % "1.1.1"

val bindingScalaJs = "com.thoughtworks.binding" %%%! "dom" % "11.0.1"
val bindingScalaFutureJs = "com.thoughtworks.binding" %%%! "futurebinding" % "11.0.1"

val endpointsJvm = "org.julienrf" %% "endpoints-algebra" % "0.4.0"
val endpointsJs = "org.julienrf" %%%! "endpoints-algebra" % "0.4.0"
val endpointsPlayServer = "org.julienrf" %% "endpoints-play-server" % "0.4.0"
val endpointsXhrClientJs = "org.julienrf" %%%! "endpoints-xhr-client" % "0.4.0"

lazy val crossType = CrossType.Full

lazy val `simpleservice-app` = (project in file("."))
  .aggregate(`simpleservice-api`, `simpleservice-impl`, client,server)

lazy val `simpleservice-api-root` = (crossProject in file("simpleservice-api"))
  .settings(
    name := "simpleservice-api-root",
    EclipseKeys.eclipseOutput := Some("eclipse_target"),
    unmanagedSourceDirectories in Compile :=
      Seq((scalaSource in Compile).value) ++
        crossType.sharedSrcDir(baseDirectory.value, "main"),
    unmanagedSourceDirectories in Test :=
      Seq((scalaSource in Test).value) ++
        crossType.sharedSrcDir(baseDirectory.value, "test")
  )
lazy val `simpleservice-api` = `simpleservice-api-root`.jvm
  .settings(
    scalaVersion := scalaV,
    name := "simpleservice-api",
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )
lazy val `simpleservice-api-js` = `simpleservice-api-root`.js
  .settings(
    scalaVersion := scalaV,
    name := "simpleservice-api-js",
    libraryDependencies ++= Seq(
      playJsonJs
    )
  )


lazy val `simpleservice-impl` = (project in file("simpleservice-impl"))
  .enablePlugins(LagomScala)
  .settings(
    scalaVersion := scalaV,
    name := "simpleservice-impl",
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`simpleservice-api`)

lazy val server = webApp.jvm
  .enablePlugins(PlayScala && LagomPlay)
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
    scalaJsScripts,
    macwire,
    endpointsJvm,
    endpointsPlayServer,
    filters,
    specs2 % Test
  ),
  // Compile the project before generating Eclipse files, so that generated .scala or .class files for views and routes are present
  EclipseKeys.preTasks := Seq(compile in Compile)
)
.dependsOn(`simpleservice-api`)

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
      scalaJsDom,
      scalaXmlJs,
      bindingScalaJs,
      bindingScalaFutureJs,
      endpointsJs,
      endpointsXhrClientJs
    )
  )
  .dependsOn(`simpleservice-api-js`)

//lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared")).
//  settings(scalaVersion := scalaV).
//  jsConfigure(_ enablePlugins ScalaJSWeb)


lazy val webApp = (crossProject in file("webApp"))
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
