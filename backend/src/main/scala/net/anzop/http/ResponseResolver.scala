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
import org.http4s.headers.{`Content-Type`, `Range`}
import org.http4s.{headers, Headers, MediaType, Response, Status}
import org.typelevel.ci.CIStringSyntax

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

  def respondFileDownload(metadata: TrackMetadata, file: Stream[F, Byte]): F[Response[F]] =
    Ok(file)
      .map(_.withContentType(`Content-Type`(MediaType.application.`octet-stream`)))
      .map(_.withHeaders(headers.`Content-Disposition`("attachment", Map(ci"filename" -> s"${metadata.title}.${metadata.format}"))))

  def respondFileStream(file: Stream[F, Byte]): F[Response[F]] =
    Ok(file).map(_.withContentType(`Content-Type`(MediaType.audio.mp3)))

  def parseRange(headers: Headers): Option[(Long, Option[Long])] =
    headers.get[Range].flatMap { rangeHeader =>
      rangeHeader.ranges.head match {
        case Range.SubRange(start, endOpt) => Some((start, endOpt))
      }
    }

  def respondChunk(
      file: Stream[F, Byte],
      start: Long,
      end: Long,
      chunk: Int
    ): F[Response[F]] = {
    val rangedStream  = file.drop(start).take(end)
    val chunkedStream = rangedStream.chunkN(chunk).flatMap(Stream.chunk)

    Ok(chunkedStream)
      .map(_.withStatus(Status.PartialContent))
      .map(_.withContentType(`Content-Type`(MediaType.audio.mp3)))
  }
}

object ResponseResolver {

  def apply[F[_]](implicit syncF: Sync[F]): ResponseResolver[F] = new ResponseResolver[F] {
    implicit override def F: Sync[F] = syncF
  }
}
