package net.anzop.dto

import cats.data._
import cats.implicits._
import net.anzop.audiostreamer.{AddTrackMetadataInput, TrackMetadataOutput}
import net.anzop.models.TrackMetadata
import net.anzop.services.ServiceResult._

import java.time.LocalDate
import java.util.UUID

object TrackMetadataDto {

  def toModel(dto: AddTrackMetadataInput): Either[InvalidObject, TrackMetadata] =
    validate(dto)
      .toEither
      .leftMap(invalid => InvalidObject(invalid.toList.map(_.message).mkString(", ")))

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

  private def validateBitrate(bitrate: Option[Int]): ValidatedNel[InvalidObject, Option[Int]] =
    bitrate match {
      case Some(b) if b <= 0     => InvalidObject("Invalid bitrate").invalidNel
      case Some(b) if b > 400000 => InvalidObject("Bitrate is too high").invalidNel
      case _                     => bitrate.validNel
    }

  private def validateFileSize(fileSize: Int): ValidatedNel[InvalidObject, Int] =
    fileSize match {
      case size if size <= 0        => InvalidObject("Invalid file size").invalidNel
      case size if size > 100000000 => InvalidObject("File size is too large").invalidNel
      case _                        => fileSize.validNel
    }

  private def validateFormat(format: String): ValidatedNel[InvalidObject, String] =
    format.toLowerCase() match {
      case "mp3" | "flac" | "ogg" | "wav" => format.validNel
      case _                              => InvalidObject("Invalid format").invalidNel
    }

  private def validateYear(year: Option[Int]): ValidatedNel[InvalidObject, Option[Int]] =
    year match {
      case Some(y) if y < 1900                    => InvalidObject("Invalid year").invalidNel
      case Some(y) if y > LocalDate.now().getYear => InvalidObject("Year cannot be in the future").invalidNel
      case _                                      => year.validNel
    }

  private def validate(input: AddTrackMetadataInput): ValidatedNel[InvalidObject, TrackMetadata] =
    (
      input.album.validNel,
      input.artist.validNel,
      validateBitrate(input.bitrate),
      input.duration.validNel,
      "".validNel,
      validateFileSize(input.fileSize),
      validateFormat(input.format),
      input.genre.validNel,
      input.title.validNel,
      UUID.randomUUID().toString.validNel,
      validateYear(input.year)
    ).mapN(TrackMetadata)
}
