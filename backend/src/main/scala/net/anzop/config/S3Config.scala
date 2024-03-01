package net.anzop.config

import eu.timepit.refined.types.string.NonEmptyString
import fs2.aws.s3.models.Models.BucketName
import net.anzop.helpers.Extensions.EnvOps
import software.amazon.awssdk.regions.Region

case class S3Config(region: Region, bucket: BucketName)

object S3Config {

  def fromEnv: S3Config = {
    val bucket: Either[String, NonEmptyString] =
      NonEmptyString.from(sys.env.getOrThrow("S3_TRACKS_BUCKET_NAME", "S3_TRACKS_BUCKET_NAME is not set"))

    bucket.map(BucketName.apply) match {
      case Left(error)   => throw new RuntimeException(error)
      case Right(bucket) => S3Config(Region.of(sys.env.getOrThrow("AWS_REGION", "AWS_REGION is not set")), bucket)
    }
  }
}
