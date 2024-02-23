package net.anzop.models

final case class TrackMetadata(
    album: Option[String] = None,
    artist: String,
    bitrate: Option[Int] = None,
    duration: Int,
    fileSize: Int,
    format: String,
    genre: List[String],
    title: String,
    trackId: String,
    year: Option[Int] = None
  )
