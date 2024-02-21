name := "music-streaming-service"

version := "0.1"

scalaVersion := "2.13.6"

libraryDependencies ++= Seq(
  "org.typelevel"  %% "cats-core"           % "2.9.0",
  "org.typelevel"  %% "cats-effect"         % "3.4.8",
  "org.tpolecat"   %% "doobie-core"         % "1.0.0-M5",
  "org.postgresql" % "postgresql"           % "42.3.1",
  "org.flywaydb"   % "flyway-core"          % "9.0.0",
  "org.http4s"     %% "http4s-blaze-server" % "0.23.14",
  "org.http4s"     %% "http4s-dsl"          % "0.23.14",
  "org.http4s"     %% "http4s-circe"        % "0.23.14",
  "io.circe"       %% "circe-generic"       % "0.14.5"
)
