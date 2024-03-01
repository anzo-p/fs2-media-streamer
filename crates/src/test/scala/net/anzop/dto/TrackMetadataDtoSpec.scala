package net.anzop.dto

import net.anzop.audiostreamer.{AddTrackMetadataInput, TrackMetadataOutput}
import net.anzop.dto.TrackMetadataDto._
import net.anzop.models.TrackMetadata
import net.anzop.services.ServiceResult._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate

class TrackMetadataDtoSpec extends AnyFlatSpec with Matchers {

  val testTrackInput = AddTrackMetadataInput(
    artist   = "artist",
    album    = Some("album"),
    bitrate  = Some(320),
    duration = 180,
    fileSize = 10000000,
    format   = "mp3",
    genre    = List("genre"),
    title    = "title",
    year     = Some(2021)
  )

  val testTrack = TrackMetadata(
    artist   = "artist",
    album    = Some("album"),
    bitrate  = Some(320),
    duration = 180,
    filepath = "path",
    fileSize = 10000000,
    format   = "mp3",
    genre    = List("genre"),
    title    = "title",
    trackId  = "trackId",
    year     = Some(2021)
  )

  "fromModel" should "return a TrackMetadataOutput" in {
    fromModel(testTrack) shouldBe TrackMetadataOutput(
      artist   = "artist",
      album    = Some("album"),
      bitrate  = Some(320),
      duration = 180,
      fileSize = 10000000,
      format   = "mp3",
      genre    = List("genre"),
      title    = "title",
      trackId  = "trackId",
      year     = Some(2021)
    )
  }

  "validateBitrate" should "fail on bitrate of zero" in {
    val testData = testTrackInput.copy(bitrate = Some(0))
    toModel(testData) shouldBe Left(InvalidObject("Invalid bitrate"))
  }

  "validateBitrate" should "fail on negative bitrate" in {
    val testData = testTrackInput.copy(bitrate = Some(-1))
    toModel(testData) shouldBe Left(InvalidObject("Invalid bitrate"))
  }

  "validateBitrate" should "fail on too high bitrate" in {
    val testData = testTrackInput.copy(bitrate = Some(400001))
    toModel(testData) shouldBe Left(InvalidObject("Bitrate is too high"))
  }

  "validateFileSize" should "fail on bitrate of zero" in {
    val testData = testTrackInput.copy(fileSize = 0)
    toModel(testData) shouldBe Left(InvalidObject("Invalid file size"))
  }

  "validateFileSize" should "fail on negative bitrate" in {
    val testData = testTrackInput.copy(fileSize = -1)
    toModel(testData) shouldBe Left(InvalidObject("Invalid file size"))
  }

  "validateFileSize" should "fail on too high bitrate" in {
    val testData = testTrackInput.copy(fileSize = 100000001)
    toModel(testData) shouldBe Left(InvalidObject("File size is too large"))
  }

  "validateFormat" should "return a ValidatedNel" in {
    List("mp3", "flac", "ogg", "wav").map { format =>
      val testData = testTrackInput.copy(format = format)
      toModel(testData).getClass shouldBe classOf[Right[_, _]]
    }
  }

  "validateFormat" should "fail on unknown format" in {
    val testData = testTrackInput.copy(format = "wrong")
    toModel(testData) shouldBe Left(InvalidObject("Invalid format"))
  }

  "validateFormat" should "fail on empty format" in {
    val testData = testTrackInput.copy(format = "")
    toModel(testData) shouldBe Left(InvalidObject("Invalid format"))
  }

  "validateYear" should "fail on too historical year" in {
    val testData = testTrackInput.copy(year = Some(1899))
    toModel(testData) shouldBe Left(InvalidObject("Invalid year"))
  }

  "validateYear" should "fail on future year" in {
    val testData = testTrackInput.copy(year = Some(LocalDate.now().getYear + 1))
    toModel(testData) shouldBe Left(InvalidObject("Year cannot be in the future"))
  }
}
