package net.anzop

import cats.effect._
import cats.implicits._
import doobie.util.transactor.Transactor
import net.anzop.config.{DbConfig, S3Config, StreamConfig}
import net.anzop.db.{DbOps, Doobie, Migration}
import net.anzop.http.{HealthRoutes, TrackRoutes}
import net.anzop.services.{S3Service, TrackService}
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import scala.concurrent.duration.Duration

object App extends IOApp {
  implicit def logger[F[_] : Async]: Logger[F] = Slf4jLogger.getLogger[F]

  private def startServer[F[_] : Async]: F[ExitCode] = {
    val dbConfig = DbConfig.fromEnv

    implicit val xa: Transactor[F]   = new Doobie[F](dbConfig).xa
    implicit val db: DbOps[F]        = new DbOps[F]
    implicit val s3: S3Service[F]    = new S3Service[F](S3Config.fromEnv)
    implicit val ts: TrackService[F] = new TrackService[F]

    val trackRoutes  = new TrackRoutes[F](StreamConfig.fromEnv)
    val healthRoutes = new HealthRoutes[F]
    val allRoutes    = trackRoutes.corsRoutes <+> healthRoutes.routes

    for {
      _ <- Migration.flywayMigrate(dbConfig)
      _ <- logger.info("Database migration completed, starting server...")
      exitCode <- BlazeServerBuilder[F]
                   .bindHttp(8080, "0.0.0.0")
                   .withHttpApp(Router("/" -> allRoutes).orNotFound)
                   .withIdleTimeout(Duration("2m"))
                   .withResponseHeaderTimeout(Duration("30s"))
                   .serve
                   .compile
                   .drain
                   .as(ExitCode.Success)
    } yield exitCode
  }

  def run(args: List[String]): IO[ExitCode] = startServer[IO]
}
