package net.anzop.db

import cats.effect.IO
import doobie.Update
import doobie.implicits._
import doobie.postgres.implicits._
import net.anzop.audiostreamer.{AddTrackMetadataInput, TrackMetadataOutput}
import net.anzop.db.Doobie.xa

import java.util.UUID

object Db {

  def addTrackMetadata(metadata: AddTrackMetadataInput): IO[String] = {
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
      result <- Update[TrackMetadataOutput](insertSql)
                 .run(trackMetadata)
                 .transact(xa)
                 .map(_ => trackMetadata.trackId)
                 .attempt

      _ <- IO(println(s"Insert result: $result"))

    } yield trackMetadata.trackId
  }
}
