package net.anzop

import cats.effect._
import cats.implicits.{toFlatMapOps, toFunctorOps}
import doobie.util.transactor.Transactor
import net.anzop.config.DbConfig
import net.anzop.db.{DbOps, Doobie, Migration}
import net.anzop.http.TrackRoutes
import net.anzop.services.{FileService, TrackService}
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router

object App extends IOApp {

  private def startServer[F[_] : Async]: F[ExitCode] = {
    implicit val xa: Transactor[F]   = new Doobie[F].xa
    implicit val db: DbOps[F]        = new DbOps[F]
    implicit val fs: FileService[F]  = new FileService[F]
    implicit val ts: TrackService[F] = new TrackService[F]

    for {
      _ <- Migration.flywayMigrate(DbConfig.fromEnv)
      exitCode <- BlazeServerBuilder[F]
                   .bindHttp(8080, "localhost")
                   .withHttpApp(Router("/" -> new TrackRoutes[F].routes).orNotFound)
                   .serve
                   .compile
                   .drain
                   .as(ExitCode.Success)
    } yield exitCode
  }

  def run(args: List[String]): IO[ExitCode] = startServer[IO]
}
