package net.anzop.db

import cats.effect.IO
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor.Aux
import net.anzop.helpers.Extensions.EnvOps

object Doobie {

  val xa: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    sys.env.getOrThrow("DB_URL", "DB_URL is not set"),
    sys.env.getOrThrow("DB_USERNAME", "DB_USERNAME is not set"),
    sys.env.getOrThrow("DB_PASSWORD", "DB_PASSWORD is not set")
  )
}
