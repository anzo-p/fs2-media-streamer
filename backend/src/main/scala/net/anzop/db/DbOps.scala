package net.anzop.db

import cats.effect.Async
import cats.syntax.all._
import doobie.Update
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.transactor.Transactor
import net.anzop.audiostreamer.{AddTrackMetadataInput, TrackMetadataOutput}
import net.anzop.db.errors.DatabaseError

import java.util.UUID

object DbOps {

  def addTrackMetadata[F[_] : Async](
      metadata: AddTrackMetadataInput
    )(implicit
      xa: Transactor[F]
    ): F[Either[DatabaseError, TrackMetadataOutput]] = {

    val trackMetadata = TrackMetadataOutput(
      album    = metadata.album,
      artist   = metadata.artist,
      bitrate  = metadata.bitrate,
      duration = metadata.duration,
      fileSize = metadata.fileSize,
      format   = metadata.format,
      genre    = metadata.genre,
      title    = metadata.title,
      trackId  = UUID.randomUUID().toString,
      year     = metadata.year
    )

    val insertSql = """
      INSERT INTO tracks (artist, duration, filesize, format, genre, title, track_id, album, bitrate, filepath, year)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, '', ?)
    """

    for {
      result <- Update[TrackMetadataOutput](insertSql)
                 .run(trackMetadata)
                 .transact(xa)
                 .attempt

    } yield result match {
      case Left(th: Throwable) => {
        println(s"Error: ${th.getMessage}")
        Left(DatabaseError.handle(th))
      }
      case Right(_) => Right(trackMetadata)
    }
  }
}
