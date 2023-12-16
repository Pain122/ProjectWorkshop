scalaVersion := "2.13.10"
name := "wk"

libraryDependencies ++= Seq(
  "com.softwaremill.sttp.tapir" %% "tapir-core" % "1.4.0",
  "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % "1.4.0",
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % "1.4.0",
  "org.typelevel" %% "cats-effect" % "3.5.0",
  "org.tpolecat" %% "doobie-core" % "1.0.0-RC2",
  "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC2", // HikariCP transactor.
  "org.tpolecat" %% "doobie-postgres" % "1.0.0-RC2", // Postgres driver 42.3.1 + type mappings.
  "tf.tofu" %% "tofu-logging" % "0.12.0.1",
  "tf.tofu" %% "tofu-logging-derivation" % "0.12.0.1",
  "tf.tofu" %% "tofu-logging-layout" % "0.12.0.1",
  "tf.tofu" %% "tofu-logging-logstash-logback" % "0.12.0.1",
  "tf.tofu" %% "tofu-logging-structured" % "0.12.0.1",
  "tf.tofu" %% "tofu-core-ce3" % "0.12.0.1",
  "tf.tofu" %% "tofu-doobie-logging-ce3" % "0.12.0.1",
  "com.softwaremill.sttp.client3" %% "core" % "3.8.15",
  "tf.tofu" %% "derevo-circe" % "0.13.0",
  "io.estatico" %% "newtype" % "0.4.4",
  "com.github.pureconfig" %% "pureconfig" % "0.17.4",
  "org.http4s" %% "http4s-ember-server" % "0.23.19",
  "com.softwaremill.sttp.tapir" %% "tapir-derevo" % "1.9.2"
)

addCompilerPlugin("org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full)

dependencyOverrides += "io.circe" %% "circe-core" % "0.14.3"

scalacOptions ++= Seq("-Ymacro-annotations")

enablePlugins(UniversalPlugin)
enablePlugins(DockerPlugin)
enablePlugins(JavaAppPackaging)

dockerExposedPorts ++= Seq(80)
dockerBaseImage := "openjdk:17-jdk-slim"
