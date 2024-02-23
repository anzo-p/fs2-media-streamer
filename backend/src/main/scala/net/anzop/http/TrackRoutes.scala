package net.anzop.http

import cats.effect._
import cats.implicits._
import doobie.util.transactor.Transactor
import io.circe.generic.auto._
import net.anzop.audiostreamer.AddTrackMetadataInput
import net.anzop.services.TrackRouteService.addTrackMetadata
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}

class TrackRoutes[F[_] : Async](implicit xa: Transactor[F]) extends Http4sDsl[F] {
  implicit val ed: EntityDecoder[F, AddTrackMetadataInput] = jsonOf[F, AddTrackMetadataInput]

  private val responses = ResponseResolver[F]

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "tracks" =>
      (for {
        input    <- req.as[AddTrackMetadataInput]
        result   <- addTrackMetadata(input)
        response <- responses.resolveResponse(result)
      } yield response).recoverWith {
        case _ => InternalServerError("An unexpected error occurred")
      }
  }
}
