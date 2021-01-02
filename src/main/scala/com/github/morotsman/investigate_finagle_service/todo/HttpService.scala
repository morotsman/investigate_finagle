package com.github.morotsman.investigate_finagle_service.todo

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.util.Timeout
import com.github.morotsman.investigate_finagle_service.todo.ActorSystemInitializer.{Setup, SystemContext}
import com.github.morotsman.investigate_finagle_service.todo.TodoActor.{CreateTodo, CreateTodoReply, GetTodo, GetTodoReply, GetTodosReply, ListTodos}
import com.twitter.bijection.Conversion
import com.twitter.finagle.http.{Method, Status}
import com.twitter.finagle.http.path.{/, Path, Root}
import com.twitter.finagle.{Http, ListeningServer, Service, http}
import com.twitter.util.{Await, Future}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future => ScalaFuture}
import com.twitter.bijection.Conversion.asMethod
import com.twitter.bijection.twitter_util.UtilBijections._

import scala.util.{Success, Try}

object HttpService extends App {

  implicit val system: ActorSystem[Setup] =
    ActorSystem(ActorSystemInitializer.setup, "todo")

  def toResponse[A](status: Status, body: A)(implicit ev: Conversion[A, String]): http.Response = {
    val response = http.Response(status)
    response.setContentTypeJson()
    response.contentString = body.as[String]
    response
  }

  def service(context: SystemContext) = new Service[http.Request, http.Response] {
    def apply(req: http.Request): Future[http.Response] = {
      println(s"Received request: $req")

      val response = (Path(req.path) match {
        case Root / "todo" =>
          req.method match {
            case Method.Get =>
              val result = context.todoActor.ask((ref: ActorRef[GetTodosReply]) => ListTodos(ref))
              Success(result.map(reply => toResponse(http.Status.Ok, reply.todos.get)))
            case Method.Post =>
              val todo = req.contentString.as[Try[Todo]]
              val result = context.todoActor.ask((ref: ActorRef[CreateTodoReply]) => CreateTodo(ref, todo.get)) // TODO handle bad request
              Success(result.map(reply => toResponse(http.Status.Ok, reply.todo.get)))
          }
        case Root / "todo" / id =>
          Try(id.toLong).map(id => {
            req.method match {
              case Method.Get =>
                val result: ScalaFuture[GetTodoReply] = context.todoActor.ask((ref: ActorRef[GetTodoReply]) => GetTodo(ref, id)) // TODO fix bad request
                result.map(reply => toResponse(http.Status.Ok, reply.todo.get))
              case Method.Put =>
                ???
              case Method.Delete =>
                ???
            }
          })
      }).getOrElse(ScalaFuture(http.Response(Status.BadRequest)))

      response.as[Future[http.Response]]
    }
  }

  implicit val timeout: Timeout = 3.seconds
  implicit val ec: ExecutionContextExecutor = system.executionContext

  val setupActorSystem = system.ask((ref: ActorRef[SystemContext]) => Setup(ref))
  val systemContext = scala.concurrent.Await.result(setupActorSystem, 3.seconds)

  val server: ListeningServer = Http.serve(":8080", service(systemContext))
  Await.ready(server)


}
