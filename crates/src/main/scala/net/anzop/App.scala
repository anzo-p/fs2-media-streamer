package net.anzop

import cats.effect._
import cats.implicits.{toFlatMapOps, toFunctorOps}
import doobie.util.transactor.Transactor
import net.anzop.config.{DbConfig, S3Config, StreamConfig}
import net.anzop.db.{DbOps, Doobie, Migration}
import net.anzop.http.TrackRoutes
import net.anzop.services.{S3Service, TrackService}
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router

object App extends IOApp {

  private def startServer[F[_] : Async]: F[ExitCode] = {
    val dbConfig = DbConfig.fromEnv

    implicit val xa: Transactor[F]   = new Doobie[F](dbConfig).xa
    implicit val db: DbOps[F]        = new DbOps[F]
    implicit val s3: S3Service[F]    = new S3Service[F](S3Config.fromEnv)
    implicit val ts: TrackService[F] = new TrackService[F]

    val trackRoutes = new TrackRoutes[F](StreamConfig.fromEnv)

    for {
      _ <- Migration.flywayMigrate(dbConfig)
      exitCode <- BlazeServerBuilder[F]
                   .bindHttp(8080, "127.0.0.1")
                   .withHttpApp(Router("/" -> trackRoutes.corsRoutes).orNotFound)
                   .serve
                   .compile
                   .drain
                   .as(ExitCode.Success)
    } yield exitCode
  }

  def run(args: List[String]): IO[ExitCode] = startServer[IO]
}
