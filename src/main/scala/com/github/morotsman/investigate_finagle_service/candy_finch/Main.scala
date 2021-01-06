package com.github.morotsman.investigate_finagle_service.candy_finch

import scala.concurrent.ExecutionContext

import cats.effect.IO
import cats.effect.concurrent.Ref
import com.twitter.app.Flag
import com.twitter.finagle.Http
import com.twitter.server.TwitterServer
import com.twitter.util.Await

object Main extends TwitterServer {

  private val port: Flag[Int] = flag("port", 8080, "TCP port for HTTP server")

  def main(): Unit = {

    val server = for {
      id <- Ref[IO].of(0)
      store <- Ref[IO].of(Map.empty[Int, MachineState])
    } yield {
      val app = new App(id, store)(IO.contextShift(ExecutionContext.global))
      val srv = Http.server.withStatsReceiver(statsReceiver)

      srv.serve(s":${port()}", app.toService)
    }

    val handle = server.unsafeRunSync()
    onExit(handle.close())
    Await.ready(adminHttpServer)
  }
}

