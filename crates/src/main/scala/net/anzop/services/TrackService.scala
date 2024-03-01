package net.anzop.services

import cats.data.EitherT
import cats.effect._
import cats.implicits._
import doobie.util.transactor.Transactor
import fs2.Stream
import net.anzop.audiostreamer._
import net.anzop.db.DbOps
import net.anzop.dto.TrackMetadataDto
import net.anzop.models.{TrackMetadata, TrackMetadataQueryArgs}
import net.anzop.services.ServiceResult._

class TrackService[F[_] : Async](implicit xa: Transactor[F], s3: S3Service[F], db: DbOps[F]) {

  def addTrackMetadata(input: AddTrackMetadataInput): F[Either[ServiceError, SuccessResult[TrackMetadataOutput]]] =
    (
      for {
        model <- EitherT.fromEither[F](TrackMetadataDto.toModel(input))
        _     <- EitherT(db.addTrackMetadata(model).map(_.leftMap(ServiceError.handle)))
      } yield SuccessResult(TrackMetadataDto.fromModel(model))
    ).value

  def uploadTrackFile(trackId: String, file: Stream[F, Byte]): F[Either[ServiceError, SuccessResult[Unit]]] =
    (
      for {
        _ <- EitherT(db.getOneTrack(trackId).map(_.leftMap(ServiceError.handle)))
        _ <- EitherT(s3.writeFile(trackId, file).attempt).leftMap((e: Throwable) => ServiceError.handle(e))
        _ <- EitherT(db.updateFilepath(trackId, filepath = trackId).map(_.leftMap(ServiceError.handle)))
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

  def downloadTrack(track: TrackMetadata): F[Either[ServiceError, Stream[F, Byte]]] =
    db.getOneTrack(track.trackId).map(_.leftMap(ServiceError.handle)).flatMap {
      case Right(trackMetadata) =>
        Async[F].pure(Right(s3.readFile(trackMetadata.filepath)))

      case Left(serviceError) =>
        Async[F].pure(Left(serviceError))
    }
}
