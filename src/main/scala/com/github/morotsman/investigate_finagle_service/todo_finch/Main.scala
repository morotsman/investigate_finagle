package com.github.morotsman.investigate_finagle_service.todo_finch

import scala.concurrent.ExecutionContext

import cats.effect.IO
import cats.effect.concurrent.Ref
import com.twitter.app.Flag
import com.twitter.finagle.Http
import com.twitter.server.TwitterServer
import com.twitter.util.Await

// copy/paste from https://github.com/finagle/finch/tree/master/examples/src/main/scala/io/finch/todo
object Main extends TwitterServer {

  private val port: Flag[Int] = flag("port", 8081, "TCP port for HTTP server")

  def main(): Unit = {
    println(s"Open your browser at http://localhost:${port()}/todo/index.html") //scalastyle:ignore

    val server = for {
      id <- Ref[IO].of(0)
      store <- Ref[IO].of(Map.empty[Int, Todo])
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

