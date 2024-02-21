package net.anzop

import cats.effect._
import net.anzop.config.DbConfig
import net.anzop.routes.AudioRack
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router

object App extends IOApp {

  private val startServer =
    for {
      _ <- IO(db.Migration.flywayMigrate(DbConfig.fromEnv))
      exitCode <- BlazeServerBuilder[IO]
                   .bindHttp(8080, "localhost")
                   .withHttpApp(Router("/" -> AudioRack.routes).orNotFound)
                   .serve
                   .compile
                   .drain
                   .as(ExitCode.Success)
    } yield exitCode

  override def run(args: List[String]): IO[ExitCode] = startServer
}
