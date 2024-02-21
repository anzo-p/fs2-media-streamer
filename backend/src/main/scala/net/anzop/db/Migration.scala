package net.anzop.db

import net.anzop.config.DbConfig
import org.flywaydb.core.Flyway

object Migration {

  def flywayMigrate(dbConfig: DbConfig): Unit = {
    val flyway = Flyway
      .configure()
      .locations("classpath:db/migration")
      .dataSource(dbConfig.url, dbConfig.username, dbConfig.password)
      .load()

    flyway.migrate()
  }
}
