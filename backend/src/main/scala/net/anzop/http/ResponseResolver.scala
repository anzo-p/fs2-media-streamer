package net.anzop.http

import cats.effect._
import cats.implicits._
import fs2.Stream
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import net.anzop.audiostreamer.TrackMetadataOutput
import net.anzop.models.TrackMetadata
import net.anzop.services.ServiceResult._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`
import org.http4s.{headers, MediaType, Response}
import org.typelevel.ci.CIStringSyntax
import smithy4s.Blob

trait ResponseResolver[F[_]] extends Http4sDsl[F] {
  implicit def F: Sync[F]

  private def successResponse[T](result: SuccessResult[T]): F[Response[F]] =
    result.result match {
      case xs: List[TrackMetadataOutput] => Ok(xs.asJson)
      case x: TrackMetadataOutput        => Created(x.asJson)
      case s: String                     => Ok(s.asJson)
      case _                             => Ok()
    }

  def resolveResponse[T](result: Either[ServiceError, SuccessResult[T]]): F[Response[F]] =
    result match {
      case Right(success: SuccessResult[_]) => successResponse(success)
      case Left(ConflictError)              => Conflict()
      case Left(InvalidObject(message))     => BadRequest(message.asJson)
      case Left(NotFoundError)              => NotFound()
      case Left(_: ServiceError)            => InternalServerError("An unexpected error occurred")
    }

  def respondFileStream(metadata: TrackMetadata, file: Blob): F[Response[F]] =
    Ok(Stream.emits(file.toArray).covary[F])
      .map(_.withContentType(`Content-Type`(MediaType.application.`octet-stream`)))
      .map(_.withHeaders(headers.`Content-Disposition`("attachment", Map(ci"filename" -> s"${metadata.title}.${metadata.format}"))))
}

object ResponseResolver {

  def apply[F[_]](implicit syncF: Sync[F]): ResponseResolver[F] = new ResponseResolver[F] {
    implicit override def F: Sync[F] = syncF
  }
}
