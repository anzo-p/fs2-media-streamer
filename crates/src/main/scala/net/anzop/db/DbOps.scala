package net.anzop.db

import cats.ApplicativeError
import cats.effect.Async
import cats.syntax.all._
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.transactor.Transactor
import doobie.{ConnectionIO, Update}
import net.anzop.db.errors._
import net.anzop.models.{TrackMetadata, TrackMetadataQueryArgs}
import net.anzop.services.ServiceResult._

class DbOps[F[_] : Async] extends RetryingDb[F] {

  private def runQuery[A](db: ConnectionIO[A])(implicit xa: Transactor[F]): F[Either[DatabaseError, A]] =
    runQueryWithRetry(db).attempt.flatMap {
      case Right(value) => ApplicativeError[F, Throwable].pure(Right(value))
      case Left(error)  => ApplicativeError[F, Throwable].pure(Left(DatabaseError.handle(error)))
    }

  def getLive()(implicit xa: Transactor[F]): F[Either[DatabaseError, ServiceResult]] =
    runQuery(sql"SELECT 1".query[Int].unique)
      .map(_.map(_ => SuccessResult()))

  def addTrackMetadata(model: TrackMetadata)(implicit xa: Transactor[F]): F[Either[DatabaseError, TrackMetadata]] = {
    val insertSql = """
      INSERT INTO tracks (album, artist, bitrate, duration, filepath, filesize, format, genre, title, track_id, year)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """

    runQuery(Update[TrackMetadata](insertSql).run(model))
      .map(_.map(_ => model))
  }

  def getOneTrack(trackId: String)(implicit xa: Transactor[F]): F[Either[DatabaseError, TrackMetadata]] = {
    val q = sql"""
      SELECT album, artist, bitrate, duration, filepath, filesize, format, genre, title, track_id, year
      FROM tracks
      WHERE track_id = $trackId
    """.query[TrackMetadata].option

    runQuery(q).map {
      case Right(Some(trackMetadata)) => Right(trackMetadata)
      case Right(None)                => Left(NoDataFound)
      case Left(error)                => Left(error)
    }
  }

  def queryManyTracks(args: TrackMetadataQueryArgs)(implicit xa: Transactor[F]): F[Either[DatabaseError, List[TrackMetadata]]] = {
    def find(s: Option[String]): Option[String] = s.map(a => "%" + a + "%")

    val offset: Int = Math.max(0, (args.page - 1) * args.batchSize)

    val q = fr"""
      SELECT album, artist, bitrate, duration, filepath, filesize, format, genre, title, track_id, year
      FROM tracks
      WHERE """ ++
      fr"      (album LIKE ${find(args.album)}   OR ${args.album.isEmpty} )" ++
      fr"AND   (artist LIKE ${find(args.artist)} OR ${args.artist.isEmpty})" ++
      fr"AND   (title LIKE ${find(args.title)}   OR ${args.title.isEmpty} )" ++
      fr"AND   (${args.genre} LIKE ANY(genre)    OR ${args.genre.isEmpty} )" ++
      fr"AND   (${args.year}     = year          OR ${args.year.isEmpty}  )" ++
      fr"""ORDER BY ${args.sortKey.getOrElse("title")} ASC
      OFFSET $offset LIMIT ${args.batchSize}"""

    runQuery(q.query[TrackMetadata].to[List]).map {
      case Right(tracks) => Right(tracks)
      case Left(error)   => Left(error)
    }
  }

  def getSampleTracks(n: Int)(implicit xa: Transactor[F]): F[Either[DatabaseError, List[TrackMetadata]]] = {
    val q = fr"""
      SELECT album, artist, bitrate, duration, filepath, filesize, format, genre, title, track_id, year
      FROM tracks
      ORDER BY RANDOM()
      LIMIT ${Math.max(0, n)}
    """

    runQuery(q.query[TrackMetadata].to[List]).map {
      case Right(tracks) => Right(tracks)
      case Left(error)   => Left(error)
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
