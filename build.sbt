name := "webmmo"

version := "1.0"

scalaVersion := "2.11.8"

scalacOptions ++= Seq("-feature", "-language:postfixOps")

libraryDependencies ++= {
  val akkaV = "2.4.8"
  val scalaTestV = "2.2.6"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-stream" % akkaV,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaV,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaV,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaV,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaV,
    "org.scalatest" %% "scalatest" % scalaTestV % "test",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0",
    "ch.qos.logback" % "logback-classic" % "1.1.7",
    "com.typesafe.play" % "play-json_2.11" % "2.5.9"
  )
}
mainClass in Compile := Some("szymonbaranczyk.boot.Server")
mainClass in packageBin := Some("szymonbaranczyk.boot.Server")
mainClass in assembly := Some("szymonbaranczyk.boot.Server")
