package net.anzop.db

import cats.effect.Async
import cats.syntax.all._
import doobie.Update
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.transactor.Transactor
import net.anzop.db.errors.DatabaseError
import net.anzop.models.TrackMetadata

object DbOps {

  def addTrackMetadata[F[_] : Async](model: TrackMetadata)(implicit xa: Transactor[F]): F[Either[DatabaseError, TrackMetadata]] = {

    val insertSql = """
      INSERT INTO tracks (artist, duration, filesize, format, genre, title, track_id, album, bitrate, filepath, year)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, '', ?)
    """

    for {
      result <- Update[TrackMetadata](insertSql)
                 .run(model)
                 .transact(xa)
                 .attempt

    } yield result match {
      case Left(th: Throwable) => Left(DatabaseError.handle(th))
      case Right(_)            => Right(model)
    }
  }
}
