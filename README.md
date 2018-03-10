# Play Framework with Scala.js, Binding.scala & Endpoints

[![Join the chat at https://gitter.im/Full-Stack-Scala-Starter/Lobby](https://badges.gitter.im/Full-Stack-Scala-Starter/Lobby.svg)](https://gitter.im/Full-Stack-Scala-Starter/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

This is a simple example application showing how you can integrate a Play project with a Scala.js, Binding.scala project and Endpoints

Frontend communicates with backend via JSON through typesafe endpoints. Project aims to be a simple modern starting point for full-stack typesafe webapp

The application contains three directories:
* `jvm` Play application (server side)
* `js` Scala.js, Binding.scala application (client side)
* `shared` Scala code that you want to share between the server and the client

## Run the application
```shell
$ sbt
> run
$ open http://localhost:9000
```

## Features

The application uses the [sbt-web-scalajs](https://github.com/vmunier/sbt-web-scalajs) sbt plugin and the [scalajs-scripts](https://github.com/vmunier/scalajs-scripts) library.

- Run your application like a regular Play app
  - `compile` triggers the Scala.js fastOptJS command
  - `run` triggers the Scala.js fastOptJS command on page refresh
  - `~compile`, `~run`, continuous compilation is also available
- Compilation errors from the Scala.js projects are also displayed in the browser
- Production archives (e.g. using `stage`, `dist`) contain the optimised javascript
- Source maps
  - Open your browser dev tool to set breakpoints or to see the guilty line of code when an exception is thrown
  - Source Maps is _disabled in production_ by default to prevent your users from seeing the source files. But it can easily be enabled in production too by setting `emitSourceMaps in fullOptJS := true` in the Scala.js projects.


