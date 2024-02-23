package net.anzop.services

import cats.data.EitherT
import cats.effect._
import cats.implicits._
import doobie.util.transactor.Transactor
import net.anzop.audiostreamer.AddTrackMetadataInput
import net.anzop.db.DbOps
import net.anzop.dto.TrackMetadataDto

object TrackRouteService {

  def addTrackMetadata[F[_] : Async](input: AddTrackMetadataInput)(implicit xa: Transactor[F]): F[ServiceResult] = {
    val operation = for {
      model <- EitherT.fromEither[F](TrackMetadataDto.toModel(input))
      _     <- EitherT(DbOps.addTrackMetadata(model).map(_.leftMap(e => InvalidObject(e.message))))
    } yield SuccessResult(model)

    operation.value.map(_.merge)
  }
}
