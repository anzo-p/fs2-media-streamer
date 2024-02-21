package net.anzop.routes

import cats.effect._
import cats.implicits._
import doobie.util.transactor.Transactor
import io.circe.generic.auto._
import net.anzop.audiostreamer.AddTrackMetadataInput
import net.anzop.db.Db
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

class AudioRack[F[_] : Async](implicit xa: Transactor[F]) extends Http4sDsl[F] {
  implicit val addTrackMetadataInputDecoder = jsonOf[F, AddTrackMetadataInput]

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "tracks" =>
      for {
        metadata <- req.as[AddTrackMetadataInput]
        _        <- Db.addTrackMetadata(metadata)
        response <- Created()
      } yield response
  }
}
