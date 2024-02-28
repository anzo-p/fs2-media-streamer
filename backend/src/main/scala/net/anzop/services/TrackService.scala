package net.anzop.services

import cats.data.EitherT
import cats.effect._
import cats.implicits._
import doobie.util.transactor.Transactor
import net.anzop.audiostreamer._
import net.anzop.db.DbOps
import net.anzop.dto.TrackMetadataDto
import net.anzop.models.{TrackMetadata, TrackMetadataQueryArgs}
import net.anzop.services.ServiceResult._
import org.http4s.multipart.Part
import smithy4s.Blob

class TrackService[F[_] : Async](implicit xa: Transactor[F], fs: FileService[F], db: DbOps[F]) {

  def addTrackMetadata(input: AddTrackMetadataInput): F[Either[ServiceError, SuccessResult[TrackMetadataOutput]]] =
    (
      for {
        model <- EitherT.fromEither[F](TrackMetadataDto.toModel(input))
        _     <- EitherT(db.addTrackMetadata(model).map(_.leftMap(ServiceError.handle)))
      } yield SuccessResult(TrackMetadataDto.fromModel(model))
    ).value

  def uploadTrackFile(trackId: String, file: Part[F]): F[Either[ServiceError, SuccessResult[Unit]]] =
    (
      for {
        _    <- EitherT(db.getOneTrack(trackId).map(_.leftMap(ServiceError.handle)))
        path <- EitherT.right(fs.saveAsync(trackId, file))
        _    <- EitherT(db.updateFilepath(trackId, path.toString).map(_.leftMap(ServiceError.handle)))
      } yield SuccessResult()
    ).value

  def uploadTrackFile(trackId: String, filename: String, file: Blob): F[Either[ServiceError, SuccessResult[Unit]]] =
    (
      for {
        _    <- EitherT(db.getOneTrack(trackId).map(_.leftMap(ServiceError.handle)))
        path <- EitherT.right(fs.saveSync(trackId, filename, file))
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

  def downloadTrack(track: TrackMetadata): F[Option[Blob]] =
    fs.load(track.filepath).map(Option.apply).recover {
      case _: Throwable => None
    }
}
