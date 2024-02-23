package net.anzop.dto

import cats.data._
import cats.implicits._
import net.anzop.audiostreamer.{AddTrackMetadataInput, TrackMetadataOutput}
import net.anzop.models.{TrackMetadata, TrackMetadataParams}
import net.anzop.services.InvalidObject

object TrackMetadataDto {

  def toModel(dto: AddTrackMetadataInput): Either[InvalidObject, TrackMetadata] =
    validate(dto).toEither match {
      case Left(invalid) => Left(InvalidObject(invalid.toList.mkString(", ")))
      case Right(params) => TrackMetadata.make(params)
    }

  def fromModel(model: TrackMetadata): TrackMetadataOutput = {
    TrackMetadataOutput(
      artist   = model.artist,
      album    = model.album,
      bitrate  = model.bitrate,
      duration = model.duration,
      fileSize = model.fileSize,
      format   = model.format,
      genre    = model.genre,
      title    = model.title,
      trackId  = model.trackId,
      year     = model.year
    )
  }

  def validate(input: AddTrackMetadataInput): ValidatedNel[InvalidObject, TrackMetadataParams] =
    (
      input.album.validNel,
      input.artist.validNel,
      input.bitrate.validNel,
      input.duration.validNel,
      input.fileSize.validNel,
      input.format.validNel,
      input.genre.validNel,
      input.title.validNel,
      input.year.validNel
    ).mapN(TrackMetadataParams)
}
