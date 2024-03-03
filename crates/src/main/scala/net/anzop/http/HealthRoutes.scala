package net.anzop.http

import cats.implicits._
import cats.effect._
import doobie.util.transactor.Transactor
import net.anzop.db.DbOps
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

class HealthRoutes[F[_] : Async](implicit db: DbOps[F], xa: Transactor[F]) extends Http4sDsl[F] {

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "health" =>
      Ok()

    case GET -> Root / "live" =>
      db.getLive().flatMap {
        case Right(_) => Ok()
        case Left(_)  => InternalServerError()
      }
  }
}
