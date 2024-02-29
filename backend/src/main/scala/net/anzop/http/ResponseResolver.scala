package net.anzop.http

import cats.data.EitherT
import cats.effect._
import cats.implicits._
import fs2.Stream
import io.circe.Encoder
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import net.anzop.models.TrackMetadata
import net.anzop.services.ServiceResult._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.{`Content-Length`, `Content-Range`, `Content-Type`, `Range`}
import org.http4s.{headers, Headers, MediaType, Response, Status}
import org.typelevel.ci.CIStringSyntax
import smithy4s.Blob

trait ResponseResolver[F[_]] extends Http4sDsl[F] {
  implicit def F: Sync[F]

  def resolveResponse[T : Encoder](result: Either[ServiceError, SuccessResult[T]]): F[Response[F]] =
    result match {
      case Right(success)               => Ok(success.result.asJson)
      case Left(ConflictError)          => Conflict()
      case Left(InvalidObject(message)) => BadRequest(message.asJson)
      case Left(NotFoundError)          => NotFound()
      case Left(_)                      => InternalServerError("An unexpected error occurred")
    }

  def resolveResponse[A](
      resultOr: EitherT[F, ServiceError, A]
    )(
      handleSuccess: A => F[Response[F]],
      handleError: ServiceError => F[Response[F]],
      handleUnexpected: Throwable => F[Response[F]]
    ): F[Response[F]] = {
    resultOr
      .value
      .flatMap {
        case Right(success) => handleSuccess(success)
        case Left(error)    => handleError(error)
      }
      .recoverWith { case ex => handleUnexpected(ex) }
  }

  def respondFileDownload(metadata: TrackMetadata, blob: Blob): F[Response[F]] =
    Ok(Stream.emits(blob.toArray).covary[F])
      .map(_.withContentType(`Content-Type`(MediaType.application.`octet-stream`)))
      .map(_.withHeaders(headers.`Content-Disposition`("attachment", Map(ci"filename" -> s"${metadata.title}.${metadata.format}"))))

  def respondFileStream(blob: Blob): F[Response[F]] =
    Ok(Stream.emits(blob.toArray).covary[F])
      .map(_.withContentType(`Content-Type`(MediaType.audio.mp3)))

  def parseRange(headers: Headers, blobLength: Long): Option[(Long, Option[Long])] =
    headers.get[Range].flatMap { rangeHeader =>
      rangeHeader.ranges.head match {
        case Range.SubRange(start, Some(end)) => Some((start, Some(end)))
        case Range.SubRange(start, None)      => Some((start, Some(blobLength - 1)))
      }
    }

  def respondPartialContent(blob: Array[Byte], start: Long, end: Option[Long]): F[Response[F]] = {
    val fileSize      = blob.length
    val actualEnd     = end.getOrElse(fileSize.toLong - 1)
    val contentLength = actualEnd - start + 1
    val bodyStream    = Stream.emits(blob.slice(start.toInt, actualEnd.toInt + 1)).covary[F]

    Ok(bodyStream)
      .map(_.withStatus(Status.PartialContent))
      .map(_.withContentType(`Content-Type`(MediaType.audio.mp3)))
      .map(
        _.putHeaders(
          `Content-Range`(Range.SubRange(start, actualEnd), Some(fileSize.toLong)),
          `Content-Length`(contentLength)
        )
      )
  }
}

object ResponseResolver {

  def apply[F[_]](implicit syncF: Sync[F]): ResponseResolver[F] = new ResponseResolver[F] {
    implicit override def F: Sync[F] = syncF
  }
}
