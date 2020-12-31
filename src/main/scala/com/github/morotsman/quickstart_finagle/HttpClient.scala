package com.github.morotsman.quickstart_finagle

import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http
import com.twitter.util.{Await, Future}

object HttpClient extends App {
  val client: Service[http.Request, http.Response] = Http.newService("localhost:8080")
  val request = http.Request(http.Method.Get, "/")
  request.host = "localhost"
  val response: Future[http.Response] = client(request)
  val result = Await.result(response.onSuccess { rep: http.Response => println("GET success: " + rep) })
  println(s"result: $result")
}
