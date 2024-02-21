package net.anzop.routes

import cats.effect._
import io.circe.generic.auto._
import net.anzop.audiostreamer.AddTrackMetadataInput
import net.anzop.db.Db
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.dsl.io._
import org.http4s.{HttpRoutes, _}

object AudioRack {

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ POST -> Root / "tracks" =>
      for {
        metadata <- req.as[AddTrackMetadataInput]
        _        <- Db.addTrackMetadata(metadata)
        response <- Created()
      } yield response
  }
}
