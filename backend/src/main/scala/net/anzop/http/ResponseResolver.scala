package net.anzop.http

import cats.effect._
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import net.anzop.audiostreamer.TrackMetadataOutput
import net.anzop.services.ServiceResult._
import org.http4s.Response
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

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
}

object ResponseResolver {

  def apply[F[_]](implicit syncF: Sync[F]): ResponseResolver[F] = new ResponseResolver[F] {
    implicit override def F: Sync[F] = syncF
  }
}
