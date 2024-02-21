package net.anzop.config

import net.anzop.helpers.Extensions.EnvOps

case class DbConfig(url: String, username: String, password: String)

object DbConfig {

  def fromEnv: DbConfig = {
    val url      = sys.env.getOrThrow("DB_URL", "DB_URL is not set")
    val username = sys.env.getOrThrow("DB_USERNAME", "DB_USERNAME is not set")
    val password = sys.env.getOrThrow("DB_PASSWORD", "DB_PASSWORD is not set")
    DbConfig(url, username, password)
  }
}
