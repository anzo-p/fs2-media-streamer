package net.anzop.db

import cats.effect.Async
import cats.syntax.all._
import doobie.Update
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.transactor.Transactor
import net.anzop.audiostreamer.{AddTrackMetadataInput, TrackMetadataOutput}

import java.util.UUID

object Db {

  def addTrackMetadata[F[_] : Async](metadata: AddTrackMetadataInput)(implicit xa: Transactor[F]): F[String] = {
    val trackMetadata = TrackMetadataOutput(
      album    = metadata.album,
      artist   = metadata.artist,
      bitrate  = metadata.bitrate,
      duration = metadata.duration,
      genre    = metadata.genre,
      fileSize = metadata.fileSize,
      format   = metadata.format,
      title    = metadata.title,
      trackId  = UUID.randomUUID().toString,
      year     = metadata.year
    )

    val insertSql = """
      INSERT INTO tracks (artist, duration, filesize, format, genre, title, track_id, album, bitrate, filepath, year)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, '', ?)
    """

    for {
      _ <- Update[TrackMetadataOutput](insertSql)
            .run(trackMetadata)
            .transact(xa)
            .onError { case e => Async[F].delay(println(s"Error: $e")) }
            .map(_ => trackMetadata.trackId)
    } yield trackMetadata.trackId
  }
}
