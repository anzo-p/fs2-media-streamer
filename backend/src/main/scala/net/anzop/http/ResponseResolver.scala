package net.anzop.http

import cats.effect.Sync
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import net.anzop.audiostreamer.TrackMetadataOutput
import net.anzop.services.{ConflictError, InvalidObject, NotFoundError, ResourceError, ServiceError, ServiceResult, SuccessResult}
import org.http4s.Response
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.Http4sDsl

trait ResponseResolver[F[_]] extends Http4sDsl[F] {
  implicit def F: Sync[F]

  private def successResponse[T](result: SuccessResult[T]): F[Response[F]] =
    result.result match {
      case metadata: TrackMetadataOutput => Created(metadata.asJson)
      case s: String                     => Ok(s.asJson)
      case _                             => Ok()
    }

  def resolveResponse(result: ServiceResult): F[Response[F]] =
    result match {
      case success: SuccessResult[_] => successResponse(success)
      case ConflictError             => Conflict()
      case InvalidObject(message)    => BadRequest(message.asJson)
      case NotFoundError             => NotFound()
      case _: ServiceError           => InternalServerError("An unexpected error occurred")
    }
}

object ResponseResolver {

  def apply[F[_]](implicit syncF: Sync[F]): ResponseResolver[F] = new ResponseResolver[F] {
    implicit override def F: Sync[F] = syncF
  }
}
