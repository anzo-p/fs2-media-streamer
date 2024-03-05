package net.anzop.db

import cats.effect._
import cats.implicits._
import doobie.implicits._
import doobie.util.transactor.Transactor
import doobie.ConnectionIO
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import scala.concurrent.duration._

class RetryingDb[F[_] : Async] {
  implicit def logger: Logger[F] = Slf4jLogger.getLogger[F]

  def runQueryWithRetry[A](
      query: ConnectionIO[A],
      maxRetries: Int       = 3,
      delay: FiniteDuration = 5.seconds
    )(implicit
      xa: Transactor[F]
    ): F[A] = {

    def attempt: F[A] = query.transact(xa)

    def retry(attempt: F[A], retriesLeft: Int): F[A] = {
      attempt.handleErrorWith { error =>
        if (retriesLeft > 0) {
          (Temporal[F].sleep(delay) *> logger.warn(s"Retrying $retriesLeft/$maxRetries due to error: $error")).flatMap(_ =>
            retry(attempt, retriesLeft - 1))
        }
        else {
          error.raiseError[F, A]
        }
      }
    }

    retry(attempt, maxRetries)
  }
}
