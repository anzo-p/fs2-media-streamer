package net.anzop.models

import net.anzop.services.InvalidObject

import java.util.UUID

final case class TrackMetadataParams(
    album: Option[String] = None,
    artist: String,
    bitrate: Option[Int] = None,
    duration: Int,
    fileSize: Int,
    format: String,
    genre: List[String],
    title: String,
    year: Option[Int] = None
  )

final case class TrackMetadata(
    artist: String,
    album: Option[String] = None,
    bitrate: Option[Int]  = None,
    duration: Int,
    fileSize: Int,
    format: String,
    genre: List[String],
    title: String,
    trackId: String,
    year: Option[Int] = None
  )

object TrackMetadata {

  def make(params: TrackMetadataParams): Either[InvalidObject, TrackMetadata] = {
    val a = TrackMetadata(
      artist   = params.artist,
      album    = params.album,
      bitrate  = params.bitrate,
      duration = params.duration,
      genre    = params.genre,
      fileSize = params.fileSize,
      format   = params.format,
      title    = params.title,
      trackId  = UUID.randomUUID().toString,
      year     = params.year
    )

    Right(a)
  }
}
