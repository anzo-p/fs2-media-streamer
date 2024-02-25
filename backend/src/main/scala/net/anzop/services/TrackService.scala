package net.anzop.services

import cats.data.EitherT
import cats.effect._
import cats.implicits._
import doobie.util.transactor.Transactor
import net.anzop.audiostreamer._
import net.anzop.db.DbOps
import net.anzop.dto.TrackMetadataDto
import org.http4s.multipart.Part
import smithy4s.Blob

class TrackService[F[_] : Async](implicit xa: Transactor[F], fs: FileService[F], db: DbOps[F]) {

  def addTrackMetadata(input: AddTrackMetadataInput): F[ServiceResult] = {
    val operation = for {
      model <- EitherT.fromEither[F](TrackMetadataDto.toModel(input))
      _     <- EitherT(db.addTrackMetadata(model).map(_.leftMap(ServiceError.handle)))
    } yield SuccessResult(TrackMetadataDto.fromModel(model))

    operation.value.map(_.merge)
  }

  def uploadTrackFile(trackId: String, file: Part[F]): F[ServiceResult] = {
    val operation = for {
      _    <- EitherT(db.getTrackMetadata(trackId).map(_.leftMap(ServiceError.handle)))
      path <- EitherT.right(fs.saveAsync(trackId, file))
      _    <- EitherT(db.updateFilepath(trackId, path.toString).map(_.leftMap(ServiceError.handle)))
    } yield SuccessResult()

    operation.value.map(_.merge)
  }

  def uploadTrackFile(trackId: String, filename: String, file: Blob): F[ServiceResult] = {
    val operation = for {
      _    <- EitherT(db.getTrackMetadata(trackId).map(_.leftMap(ServiceError.handle)))
      path <- EitherT.right(fs.saveSync(trackId, filename, file))
      _    <- EitherT(db.updateFilepath(trackId, path.toString).map(_.leftMap(ServiceError.handle)))
    } yield SuccessResult()

    operation.value.map(_.merge)
  }
}
