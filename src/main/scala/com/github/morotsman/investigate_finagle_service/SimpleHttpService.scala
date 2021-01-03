package com.github.morotsman.investigate_finagle_service

import com.twitter.finagle.{Http, Service, http}
import com.twitter.util.{Await, Future}

object SimpleHttpService extends App {

  val service = new Service[http.Request, http.Response] {
    def apply(req: http.Request): Future[http.Response] = {
      println(s"Received request: $req")
      Future.value{
        val response = http.Response(req.version, http.Status.Ok)
        response.contentString = "Hello world!"
        response
      }
    }
  }

  val server = Http.serve(":8080", service)
  Await.ready(server)
}
