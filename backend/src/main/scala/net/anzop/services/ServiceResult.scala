package net.anzop.services

import net.anzop.db.errors._

sealed trait ServiceResult

case class SuccessResult[T](result: T) extends ServiceResult

object SuccessResult {
  def apply(): SuccessResult[Unit] = SuccessResult(())
}

trait ServiceError extends ServiceResult

case object ConflictError extends ServiceError
case object NotFoundError extends ServiceError

case class InvalidObject(message: String) extends ServiceError
case class ResourceError(message: String) extends ServiceError
case class UnexpectedError(message: Throwable) extends ServiceError

object ServiceError {

  def handle(th: Throwable): ServiceError =
    th match {
      case e: IllegalArgumentException => InvalidObject(e.getMessage)
      case e: IllegalStateException    => InvalidObject(e.getMessage)
      case _                           => UnexpectedError(th)
    }

  def handle(err: DatabaseError): ServiceError =
    err match {
      case _: DuplicateEntity => ConflictError
      case NoDataFound        => NotFoundError
      case _                  => ResourceError(err.message)
    }
}
