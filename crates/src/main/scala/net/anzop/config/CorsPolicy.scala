package net.anzop.config

import org.http4s.headers.Origin
import org.http4s.server.middleware.{CORS, CORSPolicy}
import org.http4s.{Method, Uri}
import org.typelevel.ci.CIStringSyntax

object CorsPolicy {

  val config: CORSPolicy = CORS
    .policy
    .withAllowOriginHost({
      case Origin.Host(Uri.Scheme.http, Uri.RegName(ci"localhost"), Some(3000))  => true
      case Origin.Host(Uri.Scheme.https, Uri.RegName(ci"localhost"), Some(3000)) => true
      case _                                                                     => false
    })
    .withAllowMethodsIn(Set(Method.GET, Method.POST))
    .withAllowHeadersIn(Set(ci"Content-Type"))
    .withAllowCredentials(false)
}
