package net.anzop.db

import cats.effect.Async
import doobie.util.transactor.Transactor
import net.anzop.helpers.Extensions.EnvOps

class Doobie[F[_] : Async] {

  val xa: Transactor[F] = Transactor.fromDriverManager[F](
    "org.postgresql.Driver",
    sys.env.getOrThrow("DB_URL", "DB_URL is not set"),
    sys.env.getOrThrow("DB_USERNAME", "DB_USERNAME is not set"),
    sys.env.getOrThrow("DB_PASSWORD", "DB_PASSWORD is not set")
  )
}
