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

  def modifyTodoRequest(id: Long, title: String, completed: Boolean): Request = {
    val modifyTodoRequest = http.Request(http.Method.Put, s"/todo/$id")
    modifyTodoRequest.contentString = "{\"title\":\"" +  title + "\", \"completed\":" + completed + "}"
    modifyTodoRequest
  }

  def getTodosRequest(): Request =
    http.Request(http.Method.Get, "/todo")

  def getTodoRequest(id: Long): Request =
    http.Request(http.Method.Get, s"/todo/$id")

  def deleteTodoRequest(id: Long): Request =
    http.Request(http.Method.Delete, s"/todo/$id")

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
    _ <- client(getTodoRequest(1)).onSuccess(printResponse).onFailure(printFailure)
    _ <- client(modifyTodoRequest(1, "todo2", true)).onSuccess(printResponse).onFailure(printFailure)
    _ <- client(getTodoRequest(1)).onSuccess(printResponse).onFailure(printFailure)
    _ <- client(getTodosRequest()).onSuccess(printResponse).onFailure(printFailure)
    _ <- client(deleteTodoRequest(0)).onSuccess(printResponse).onFailure(printFailure)
    _ <- client(getTodosRequest()).onSuccess(printResponse).onFailure(printFailure)
    _ <- client(getTodoRequest(0)).onSuccess(printResponse).onFailure(printFailure)
  }  yield ()

  val tmp = Await.result(result)
}
