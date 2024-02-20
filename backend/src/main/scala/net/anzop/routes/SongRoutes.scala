package net.anzop.routes

import cats.effect._
import io.circe.generic.auto._
import net.anzop.types.SongMetadata
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.io._
import org.http4s.{HttpRoutes, _}

object SongRoutes {

  val songRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "song" / songId =>
      val songMetadata = SongMetadata("Example Song", "Artist Name", 180) // Mocked metadata
      Ok(songMetadata)
  }
}
