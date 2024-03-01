package net.anzop.config

import net.anzop.helpers.Extensions.EnvOps

case class StreamConfig(chunkSize: Int)

object StreamConfig {
  def fromEnv: StreamConfig = StreamConfig(sys.env.getOrThrow("STREAM_CHUNK_SIZE", "STREAM_CHUNK_SIZE is not set").toInt)
}
