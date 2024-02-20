package net.anzop

import cats.effect._
import net.anzop.routes.SongRoutes.songRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router

object App extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    BlazeServerBuilder[IO]
      .bindHttp(8080, "localhost")
      .withHttpApp(Router("/" -> songRoutes).orNotFound)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }
}
