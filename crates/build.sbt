import smithy4s.codegen.Smithy4sCodegenPlugin

lazy val root = (project in file("."))
  .settings(
    name := "crates",
    version := "0.1",
    scalaVersion := "2.13.6",
    Compile / mainClass := Some("net.anzop.App")
  )
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(Smithy4sCodegenPlugin)

libraryDependencies ++= Seq(
  "org.typelevel"                %% "cats-core"           % "2.9.0",
  "org.typelevel"                %% "cats-effect"         % "3.4.8",
  "org.tpolecat"                 %% "doobie-core"         % "1.0.0-M5",
  "org.tpolecat"                 %% "doobie-postgres"     % "1.0.0-M5",
  "org.postgresql"               % "postgresql"           % "42.7.2",
  "org.flywaydb"                 % "flyway-core"          % "9.0.0",
  "io.laserdisc"                 %% "fs2-aws-core"        % "6.1.1",
  "io.laserdisc"                 %% "fs2-aws-s3"          % "6.1.1",
  "com.disneystreaming.smithy4s" %% "smithy4s-core"       % smithy4sVersion.value,
  "com.disneystreaming.smithy4s" %% "smithy4s-http4s"     % smithy4sVersion.value,
  "org.http4s"                   %% "http4s-blaze-server" % "0.23.14",
  "org.http4s"                   %% "http4s-dsl"          % "0.23.14",
  "org.http4s"                   %% "http4s-circe"        % "0.23.14",
  "io.circe"                     %% "circe-generic"       % "0.14.5",
  "org.typelevel"                %% "log4cats-core"       % "2.2.0",
  "org.typelevel"                %% "log4cats-slf4j"      % "2.2.0",
  "ch.qos.logback"               % "logback-classic"      % "1.4.14"
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.18" % "test"
)
