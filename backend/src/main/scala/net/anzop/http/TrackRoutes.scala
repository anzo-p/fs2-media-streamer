package net.anzop.http

import cats.data.{EitherT, OptionT}
import cats.effect._
import cats.implicits._
import io.circe.generic.auto._
import net.anzop.audiostreamer.AddTrackMetadataInput
import net.anzop.config.CorsPolicy
import net.anzop.models.TrackMetadataQueryArgs
import net.anzop.services.ServiceResult.{NotFoundError, ServiceError}
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
              result   <- service.uploadTrackFile(trackId, file)
              response <- responses.resolveResponse(result)
            } yield response).recoverWith {
              case _ => InternalServerError("An unexpected error occurred")
            }

          case None =>
            BadRequest("No file part found")
        }
      }

    case req @ POST -> Root / "tracks" / "query" =>
      (for {
        input    <- req.as[TrackMetadataQueryArgs]
        result   <- service.getTrackList(input)
        response <- responses.resolveResponse(result)
      } yield response).recoverWith {
        case _ => InternalServerError("An unexpected error occurred")
      }

    case GET -> Root / "tracks" / "sample" =>
      (for {
        result   <- service.getRandomizedTrackList(10)
        response <- responses.resolveResponse(result)
      } yield response).recoverWith {
        case _ => InternalServerError("An unexpected error occurred")
      }

    case GET -> Root / "tracks" / trackId =>
      val resultOr = for {
        track      <- EitherT(service.getTrackMetadata(trackId)).map(_.result)
        blobOption <- OptionT(service.downloadTrack(track)).toRight(NotFoundError: ServiceError)
        response   <- EitherT.rightT[F, ServiceError](responses.respondFileDownload(track, blobOption))
      } yield response

      responses.resolveResponse(resultOr)(
        handleSuccess    = resp => resp,
        handleError      = error => responses.resolveResponse(Left(error)),
        handleUnexpected = _ => InternalServerError("An unexpected error occurred")
      )

    case req @ GET -> Root / "tracks" / trackId / "stream" =>
      val resultOr = for {
        track <- EitherT(service.getTrackMetadata(trackId)).map(_.result)
        blob  <- OptionT(service.downloadTrack(track)).toRight(NotFoundError: ServiceError)
        range = responses.parseRange(req.headers, blob.toArray.length.toLong)
        response <- EitherT.rightT[F, ServiceError](range match {
                     case Some((start, end)) => responses.respondPartialContent(blob.toArray, start, end)
                     case None               => responses.respondFileStream(blob)
                   })
      } yield response

      responses.resolveResponse(resultOr)(
        handleSuccess    = resp => resp,
        handleError      = error => responses.resolveResponse(Left(error)),
        handleUnexpected = _ => InternalServerError("An unexpected error occurred")
      )
  }

  val corsRoutes = CorsPolicy.config.apply(routes)
}
