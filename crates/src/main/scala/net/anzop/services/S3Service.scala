package net.anzop.services

import cats.effect.Async
import cats.effect.kernel.Resource
import eu.timepit.refined.types.string.NonEmptyString
import fs2.Stream
import fs2.aws.s3.S3
import fs2.aws.s3.models.Models.{ETag, FileKey}
import io.laserdisc.pure.s3.tagless.{S3AsyncClientOp, Interpreter => S3Interpreter}
import net.anzop.config.S3Config
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.services.s3.S3AsyncClient

class S3Service[F[_] : Async](s3Config: S3Config) {
  implicit def logger: Logger[F] = Slf4jLogger.getLogger[F]

  private val s3StreamResource: Resource[F, S3AsyncClientOp[F]] =
    for {
      s3 <- S3Interpreter[F].S3AsyncClientOpResource(
             S3AsyncClient
               .builder()
               .credentialsProvider(
                 DefaultCredentialsProvider.create()
               )
               .region(s3Config.region)
           )
    } yield s3

  private def writeFile(key: FileKey, fileStream: Stream[F, Byte]): F[ETag] =
    Stream
      .resource(s3StreamResource.map(S3.create[F]))
      .flatMap { s3 =>
        fileStream.through(s3.uploadFile(s3Config.bucket, key))
      }
      .handleErrorWith { e =>
        Stream.eval(Async[F].pure(logger.error(s"Error uploading $key to S3: ${e.getMessage}"))) >> Stream.raiseError[F](e)
      }
      .compile
      .lastOrError

  private def readFile(key: FileKey): Stream[F, Byte] =
    Stream
      .resource(s3StreamResource.map(S3.create[F]))
      .flatMap { s3 =>
        s3.readFile(s3Config.bucket, key)
      }

  def writeFile(key: String, fileStream: Stream[F, Byte]): F[ETag] =
    NonEmptyString.from(key) match {
      case Left(error) => Async[F].raiseError(new RuntimeException(error))
      case Right(key)  => writeFile(FileKey(key), fileStream)
    }

  def readFile(key: String): Stream[F, Byte] =
    NonEmptyString.from(key) match {
      case Left(error) => Stream.raiseError[F](new RuntimeException(error))
      case Right(key)  => readFile(FileKey(key))
    }
}
