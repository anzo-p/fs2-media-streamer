package net.anzop.models

final case class TrackMetadata(
    album: Option[String] = None,
    artist: String,
    bitrate: Option[Int] = None,
    duration: Int,
    filepath: String,
    fileSize: Int,
    format: String,
    genre: List[String],
    title: String,
    trackId: String,
    year: Option[Int] = None
  )

final case class TrackMetadataQueryArgs(
    album: Option[String]   = None,
    artist: Option[String]  = None,
    genre: Option[String]   = None,
    title: Option[String]   = None,
    year: Option[Int]       = None,
    sortKey: Option[String] = None,
    page: Int,
    batchSize: Int
  )
