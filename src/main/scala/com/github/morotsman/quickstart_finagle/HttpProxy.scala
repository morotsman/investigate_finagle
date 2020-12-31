package com.github.morotsman.quickstart_finagle

import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Await

object HttpProxy extends App {
  val client: Service[Request, Response] =
    Http.newService("aftonbladet.se:80").map(r => {
      println(r)
     r
    })

  val server = Http.serve(":8080", client)
  Await.ready(server)
}
