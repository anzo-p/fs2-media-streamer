package net.anzop.models

final case class TrackMetadata(
    artist: String,
    duration: Int,
    genre: List[String],
    fileSize: Int,
    format: String,
    title: String,
    trackId: String,
    album: Option[String] = None,
    bitrate: Option[Int]  = None,
    year: Option[Int]     = None
  )
