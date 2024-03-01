package net.anzop.services

import cats.data.EitherT
import cats.effect._
import cats.implicits._
import doobie.util.transactor.Transactor
import fs2.Stream
import fs2.io.file.Files
import net.anzop.audiostreamer._
import net.anzop.db.DbOps
import net.anzop.dto.TrackMetadataDto
import net.anzop.models.{TrackMetadata, TrackMetadataQueryArgs}
import net.anzop.services.ServiceResult._

class TrackService[F[_] : Async](implicit xa: Transactor[F], fs: FileService[F], db: DbOps[F]) {

  def addTrackMetadata(input: AddTrackMetadataInput): F[Either[ServiceError, SuccessResult[TrackMetadataOutput]]] =
    (
      for {
        model <- EitherT.fromEither[F](TrackMetadataDto.toModel(input))
        _     <- EitherT(db.addTrackMetadata(model).map(_.leftMap(ServiceError.handle)))
      } yield SuccessResult(TrackMetadataDto.fromModel(model))
    ).value

  def uploadTrackFile(trackId: String, filename: String, file: Stream[F, Byte]): F[Either[ServiceError, SuccessResult[Unit]]] =
    (
      for {
        _    <- EitherT(db.getOneTrack(trackId).map(_.leftMap(ServiceError.handle)))
        path <- EitherT.right(fs.saveAsync(trackId, filename, file))
        _    <- EitherT(db.updateFilepath(trackId, path.toString).map(_.leftMap(ServiceError.handle)))
      } yield SuccessResult()
    ).value

  def getTrackMetadata(trackId: String): F[Either[ServiceError, SuccessResult[TrackMetadata]]] =
    (
      for {
        track <- EitherT(db.getOneTrack(trackId).map(_.leftMap(ServiceError.handle)))
      } yield SuccessResult(track)
    ).value

  def getTrackList(args: TrackMetadataQueryArgs): F[Either[ServiceError, SuccessResult[List[TrackMetadataOutput]]]] =
    (
      for {
        tracks <- EitherT(db.queryManyTracks(args).map(_.leftMap(ServiceError.handle)))
      } yield SuccessResult(tracks.map(TrackMetadataDto.fromModel))
    ).value

  def getRandomizedTrackList(n: Int): F[Either[ServiceError, SuccessResult[List[TrackMetadataOutput]]]] =
    (
      for {
        tracks <- EitherT(db.getSampleTracks(n).map(_.leftMap(ServiceError.handle)))
      } yield SuccessResult(tracks.map(TrackMetadataDto.fromModel))
    ).value

  def downloadTrack(track: TrackMetadata): F[Either[ServiceError, Stream[F, Byte]]] = {
    val filePath = fs.makePath(track.filepath)

    Files[F].exists(filePath).flatMap {
      case false => Async[F].pure(Left(NotFoundError: ServiceError))
      case true  => Async[F].pure(Right(fs.loadAsync(track.filepath)))
    }
  }
}
