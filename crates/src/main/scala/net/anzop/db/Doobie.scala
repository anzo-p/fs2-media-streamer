package net.anzop.db

import cats.effect.Async
import doobie.util.transactor.Transactor
import net.anzop.config.DbConfig

class Doobie[F[_] : Async](dbConfig: DbConfig) {

  val xa: Transactor[F] = Transactor.fromDriverManager[F](
    "org.postgresql.Driver",
    dbConfig.url,
    dbConfig.username,
    dbConfig.password
  )
}
