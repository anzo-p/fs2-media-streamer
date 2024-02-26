package net.anzop.services

import cats.effect._
import cats.effect.kernel.Sync
import cats.implicits._
import fs2.io.file.Flag._
import fs2.io.file.{Files, Flags, Path}
import org.http4s.multipart.Part
import smithy4s.Blob

import java.nio.file.{Files => JFiles, Path => JPath, Paths => JPaths}

class FileService[F[_] : Async] {

  def saveAsync(trackId: String, filePart: Part[F]): F[Path] = {
    val trackDir = Path(s"uploadedFiles/$trackId")
    val fileName = filePart.filename.getOrElse("default.txt")
    val path     = trackDir.resolve(fileName)

    for {
      _ <- Files[F].createDirectories(trackDir)
      _ <- filePart.body.through(Files[F].writeAll(path, Flags(Write, Create, Truncate))).compile.drain
    } yield path
  }

  def saveSync(trackId: String, fileName: String, fileBlob: Blob): F[JPath] = {
    val trackDir = JPaths.get(s"uploadedFiles/$trackId")
    val path     = trackDir.resolve(fileName)

    Sync[F]
      .blocking {
        if (!JFiles.exists(trackDir)) {
          JFiles.createDirectories(trackDir)
        }
        JFiles.write(path, fileBlob.toArray)
      }
      .as(path)
  }

  def load(filepath: String): F[Blob] =
    Files[F]
      .readAll(Path(filepath), 4096, Flags(Read))
      .compile
      .to(Array)
      .map(Blob.apply)
}
