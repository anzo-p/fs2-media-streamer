package net.anzop.http

import cats.effect._
import cats.implicits._
import io.circe.generic.auto._
import net.anzop.audiostreamer.AddTrackMetadataInput
import net.anzop.models.TrackMetadataQueryArgs
import net.anzop.services.TrackService
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.multipart.{Multipart, Part}
import org.http4s.{EntityDecoder, HttpRoutes}

class TrackRoutes[F[_] : Async](implicit service: TrackService[F]) extends Http4sDsl[F] {
  implicit val ed: EntityDecoder[F, AddTrackMetadataInput] = jsonOf[F, AddTrackMetadataInput]

  private val responses = ResponseResolver[F]

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "tracks" =>
      (for {
        input    <- req.as[AddTrackMetadataInput]
        result   <- service.addTrackMetadata(input)
        response <- responses.resolveResponse(result)
      } yield response).recoverWith {
        case _ => InternalServerError("An unexpected error occurred")
      }

    case req @ POST -> Root / "tracks" / trackId / "file" =>
      req.decode[Multipart[F]] { m =>
        m.parts.find(_.name.contains("file")) match {
          case Some(file: Part[F]) =>
            (for {
              result   <- service.uploadTrackFile(trackId, file)
              response <- responses.resolveResponse(result)
            } yield response).recoverWith {
              case _ => InternalServerError("An unexpected error occurred")
            }

          case None =>
            BadRequest("No file part found")
        }
      }

    case req @ GET -> Root / "tracks" =>
      (for {
        input    <- req.as[TrackMetadataQueryArgs]
        result   <- service.getTrackList(input)
        response <- responses.resolveResponse(result)
      } yield response).recoverWith {
        case _ => InternalServerError("An unexpected error occurred")
      }
  }
}
