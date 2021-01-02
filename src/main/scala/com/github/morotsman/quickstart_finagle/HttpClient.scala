package com.github.morotsman.quickstart_finagle

import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.{Await, Future}

object HttpClient extends App {
  val client: Service[http.Request, http.Response] = Http.newService("localhost:8080")

  def createTodoRequest(title: String): Request = {
    val createTodoRequest = http.Request(http.Method.Post, "/todo")
    createTodoRequest.contentString = "{\"title\":\"test\",\"completed\":false}"
    createTodoRequest
  }

  def getTodosRequest(): Request =
    http.Request(http.Method.Get, "/todo")

  def getTodoRequest(id: Long): Request =
    http.Request(http.Method.Get, s"/todo/$id")



  def printResponse(r: Response) = {
    println(s"Response: $r")
    println(s"body: ${r.contentString}")
  }

  def printFailure(t: Throwable) = {
    println(s"failure: $t")
  }

  val result = for {
    _ <- client(createTodoRequest("todo1")).onSuccess(printResponse).onFailure(printFailure)
    _ <- client(createTodoRequest("todo2")).onSuccess(printResponse).onFailure(printFailure)
    _ <- client(getTodosRequest()).onSuccess(printResponse).onFailure(printFailure)
    _ <- client(getTodoRequest(0)).onSuccess(printResponse).onFailure(printFailure)
  }  yield ()

  println("hepp")
  val tmp = Await.result(result)
}
