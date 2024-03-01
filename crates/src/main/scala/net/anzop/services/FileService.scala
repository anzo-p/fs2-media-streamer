package net.anzop.services

import cats.effect._
import cats.implicits._
import fs2.Stream
import fs2.io.file.Flag._
import fs2.io.file.{Files, Flags, Path}
import net.anzop.config.StreamConfig

class FileService[F[_] : Async](streamConfig: StreamConfig) {

  def makePath(filename: String): Path =
    Path(s"uploadedFiles/$filename")

  def saveAsync(trackId: String, filename: String, file: Stream[F, Byte]): F[Path] = {
    val trackDir = makePath(trackId)
    val path     = trackDir.resolve(filename)

    for {
      _ <- Files[F].createDirectories(trackDir)
      _ <- file.through(Files[F].writeAll(path, Flags(Write, Create, Truncate))).compile.drain
    } yield path
  }

  def loadAsync(filename: String): Stream[F, Byte] =
    Files[F].readAll(makePath(filename), streamConfig.chunkSize, Flags(Read))
}
