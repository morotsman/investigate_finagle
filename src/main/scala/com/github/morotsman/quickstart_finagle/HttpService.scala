package com.github.morotsman.quickstart_finagle

import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http
import com.twitter.util.{Await, Future}

// https://twitter.github.io/finagle/guide/Quickstart.html

object HttpService extends App {

  val service = new Service[http.Request, http.Response] {
    def apply(req: http.Request): Future[http.Response] = {
      println(s"Received request: $req")
      Future.value(
        http.Response(req.version, http.Status.Ok)
      )
    }
  }

  val server = Http.serve(":8080", service)
  Await.ready(server)

}
