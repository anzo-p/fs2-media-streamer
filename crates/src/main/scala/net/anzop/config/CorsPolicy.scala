package net.anzop.config

import net.anzop.helpers.Extensions.EnvOps
import org.http4s.headers.Origin
import org.http4s.server.middleware.{CORS, CORSPolicy}
import org.http4s.{Method, Uri}
import org.typelevel.ci.CIStringSyntax

object CorsPolicy {

  private val corsOrigins: Seq[String] =
    sys.env.getOrThrow("CORS_ALLOWED_ORIGINS", "CORS_ALLOWED_ORIGINS is not set").split(",")

  private val allowedOrigins: PartialFunction[Origin.Host, Boolean] = {
    case Origin.Host(Uri.Scheme.http | Uri.Scheme.https, Uri.RegName(host), _) if corsOrigins.contains(host.toString.toLowerCase) =>
      true
    case _ =>
      false
  }

  val config: CORSPolicy = CORS
    .policy
    .withAllowOriginHost(allowedOrigins)
    .withAllowMethodsIn(Set(Method.GET, Method.POST))
    .withAllowHeadersIn(Set(ci"Content-Type"))
    .withAllowCredentials(false)
}
