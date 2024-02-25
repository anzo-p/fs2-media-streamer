package net.anzop.db

import cats.data.EitherT
import cats.effect.Async
import cats.syntax.all._
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.transactor.Transactor
import doobie.{ConnectionIO, Update}
import net.anzop.db.errors._
import net.anzop.models.TrackMetadata
import net.anzop.services.ServiceResult._

class DbOps[F[_] : Async] {

  private def runQuery[A](db: ConnectionIO[A])(implicit xa: Transactor[F]): F[Either[DatabaseError, A]] = {
    EitherT(db.attempt.transact(xa))
      .leftMap(th => DatabaseError.handle(th))
      .value
  }

  def addTrackMetadata(model: TrackMetadata)(implicit xa: Transactor[F]): F[Either[DatabaseError, TrackMetadata]] = {
    val insertSql = """
      INSERT INTO tracks (album, artist, bitrate, duration, filepath, filesize, format, genre, title, track_id, year)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """

    runQuery(Update[TrackMetadata](insertSql).run(model))
      .map(_.map(_ => model))
  }

  def getTrackMetadata(trackId: String)(implicit xa: Transactor[F]): F[Either[DatabaseError, TrackMetadata]] = {
    val dbOp = sql"""
      SELECT album, artist, bitrate, duration, filepath, filesize, format, genre, title, track_id, year
      FROM tracks
      WHERE track_id = $trackId
    """.query[TrackMetadata].option

    runQuery(dbOp).map {
      case Right(Some(trackMetadata)) => Right(trackMetadata)
      case Right(None)                => Left(NoDataFound)
      case Left(error)                => Left(error)
    }
  }

  def updateFilepath(trackId: String, filepath: String)(implicit xa: Transactor[F]): F[Either[DatabaseError, ServiceResult]] = {
    val updateSql = """
      UPDATE tracks
      SET filepath = ?
      WHERE track_id = ?
    """

    runQuery(Update[(String, String)](updateSql).run((filepath, trackId)))
      .map(_.map(countAffectedRows => SuccessResult(countAffectedRows)))
  }
}
