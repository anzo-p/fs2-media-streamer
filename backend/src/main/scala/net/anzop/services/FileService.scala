package net.anzop.services

import cats.effect._
import cats.implicits._
import fs2.io.file.Flag._
import fs2.io.file.{Files, Flags, Path}
import org.http4s.multipart.Part

class FileService[F[_] : Async] {

  def saveFile(trackId: String, filePart: Part[F]): F[Path] = {
    val trackDir = Path(s"uploadedFiles/$trackId")
    val fileName = filePart.filename.getOrElse("default.txt")
    val path     = trackDir.resolve(fileName)

    for {
      _ <- Files[F].createDirectories(trackDir)
      _ <- filePart.body.through(Files[F].writeAll(path, Flags(Write, Create, Truncate))).compile.drain
    } yield path
  }
}
