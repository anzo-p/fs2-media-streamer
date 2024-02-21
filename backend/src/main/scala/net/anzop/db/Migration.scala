package net.anzop.db

import cats.effect.Async
import net.anzop.config.DbConfig
import org.flywaydb.core.Flyway

object Migration {

  def flywayMigrate[F[_] : Async](dbConfig: DbConfig): F[Unit] = Async[F].delay {
    val flyway = Flyway
      .configure()
      .locations("classpath:db/migration")
      .dataSource(dbConfig.url, dbConfig.username, dbConfig.password)
      .load()

    flyway.migrate()
  }
}
