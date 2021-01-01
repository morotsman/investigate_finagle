package com.github.morotsman.quickstart_finagle

import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http
import com.twitter.util.{Await, Future}

object HttpClient extends App {
  val client: Service[http.Request, http.Response] = Http.newService("localhost:8080")
  val getTodos = http.Request(http.Method.Get, "/todo")
  val createTodo = http.Request(http.Method.Post, "/todo")


  createTodo.contentString = "{\"id\":0,\"title\":\"test\",\"completed\":false}"
  client(createTodo)


  val response: Future[http.Response] = client(getTodos)
  val result = Await.result(response.onSuccess { rep: http.Response => println("GET success: " + rep) })
  println(s"result: $result")
  println(s"contentString: ${result.contentString}")
}
