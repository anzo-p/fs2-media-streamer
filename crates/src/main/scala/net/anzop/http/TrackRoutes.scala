package net.anzop.http

import cats.data.EitherT
import cats.effect._
import cats.implicits._
import io.circe.generic.auto._
import net.anzop.audiostreamer.AddTrackMetadataInput
import net.anzop.config.{CorsPolicy, StreamConfig}
import net.anzop.models.TrackMetadataQueryArgs
import net.anzop.services.ServiceResult.{NotFoundError, ServiceError}
import net.anzop.services.TrackService
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.multipart.{Multipart, Part}
import org.http4s.{EntityDecoder, HttpRoutes}

class TrackRoutes[F[_] : Async](streamConfig: StreamConfig)(implicit service: TrackService[F]) extends Http4sDsl[F] {
  implicit val ed: EntityDecoder[F, AddTrackMetadataInput] = jsonOf[F, AddTrackMetadataInput]

  private val responses = new ResponseResolver[F]

  private val routes: HttpRoutes[F] = HttpRoutes.of[F] {
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
              result   <- service.uploadTrackFile(trackId, file.body)
              response <- responses.resolveResponse(result)
            } yield response).recoverWith {
              case _ => InternalServerError("An unexpected error occurred")
            }

          case _ =>
            BadRequest("No file part found")
        }
      }

    case req @ POST -> Root / "tracks" / "search" =>
      (for {
        input    <- req.as[TrackMetadataQueryArgs]
        result   <- service.getTrackList(input)
        response <- responses.resolveResponse(result)
      } yield response).recoverWith {
        case _ => InternalServerError("An unexpected error occurred")
      }

    case GET -> Root / "tracks" / "search" / "sample" =>
      (for {
        result   <- service.getRandomizedTrackList(10)
        response <- responses.resolveResponse(result)
      } yield response).recoverWith {
        case _ => InternalServerError("An unexpected error occurred")
      }

    case GET -> Root / "tracks" / trackId / "download" =>
      val resultOr = for {
        track    <- EitherT(service.getTrackMetadata(trackId)).map(_.result)
        blob     <- EitherT(service.downloadTrack(track))
        response <- EitherT.rightT[F, ServiceError](responses.respondFileDownload(track, blob))
      } yield response

      responses.resolveResponse(resultOr)(
        handleSuccess = identity,
        handleError = {
          case NotFoundError => NotFound()
          case error         => responses.resolveResponse(Left(error))
        },
        handleUnexpected = _ => InternalServerError("An unexpected error occurred")
      )

    case req @ GET -> Root / "tracks" / trackId / "stream" =>
      val resultOr = for {
        track <- EitherT(service.getTrackMetadata(trackId)).map(_.result)
        blob  <- EitherT(service.downloadTrack(track))
        range = responses.parseRange(req.headers)
        response <- EitherT.rightT[F, ServiceError](range match {
                     case Some((start, Some(end))) => responses.respondChunk(blob, start, end, streamConfig.chunkSize)
                     case _                        => responses.respondFileStream(blob)
                   })
      } yield response

      responses.resolveResponse(resultOr)(
        handleSuccess = identity,
        handleError = {
          case NotFoundError => NotFound()
          case error         => responses.resolveResponse(Left(error))
        },
        handleUnexpected = _ => InternalServerError("An unexpected error occurred")
      )
  }

  val corsRoutes = CorsPolicy.config.apply(routes)
}
