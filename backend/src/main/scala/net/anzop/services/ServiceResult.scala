package net.anzop.services

sealed trait ServiceResult

case class SuccessResult[T](result: T) extends ServiceResult

object SuccessResult {
  def apply(): SuccessResult[Unit] = SuccessResult(())
}

trait ServiceError extends ServiceResult

case object ConflictError extends ServiceError
case class InvalidObject(message: String) extends ServiceError
case object NotFoundError extends ServiceError
case class ResourceError(message: String) extends ServiceError
case class UnexpectedError(message: Throwable) extends ServiceError
