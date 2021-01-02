package com.github.morotsman.investigate_finagle_service.todo

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.util.Timeout
import com.github.morotsman.investigate_finagle_service.todo.ActorSystemInitializer.{Setup, SystemContext}
import com.github.morotsman.investigate_finagle_service.todo.TodoActor.{CreateTodo, CreateTodoReply, GetTodo, GetTodoReply, GetTodosReply, ListTodos}
import com.twitter.bijection.Conversion
import com.twitter.finagle.http.{Method, Response, Status}
import com.twitter.finagle.http.path.{/, Long, Path, Root}
import com.twitter.finagle.{Http, ListeningServer, Service, http}
import com.twitter.util.{Await, Future}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future => ScalaFuture}
import com.twitter.bijection.Conversion.asMethod
import com.twitter.bijection.twitter_util.UtilBijections._

import scala.util.{Failure, Success, Try}

object HttpService extends App {

  type Body = String

  implicit val system: ActorSystem[Setup] =
    ActorSystem(ActorSystemInitializer.setup, "todo")

  def toResponse(body: Try[Body]): http.Response = body match {
    case Success(b) =>
      val response = http.Response(Status.Ok)
      response.setContentTypeJson()
      response.contentString = b
      response
    case Failure(e: NoSuchMethodError) =>
      http.Response(Status.NotFound)
    case Failure(e: IllegalArgumentException) =>
      http.Response(Status.BadRequest)
    case Failure(e: NoSuchElementException) =>
      http.Response(Status.NotFound)
    case e@_ =>
      println("error: " + e)
      http.Response(Status.InternalServerError)

  }

  def service(context: SystemContext) = new Service[http.Request, http.Response] {
    def apply(req: http.Request): Future[http.Response] = (Path(req.path) match {
      case Root / "todo" =>
        req.method match {
          case Method.Get =>
            val result = context.todoActor.ask((ref: ActorRef[GetTodosReply]) => ListTodos(ref))
            result.map(reply => reply.todos.map(t => t.as[Body]))
          case Method.Post =>
            req.contentString.as[Try[Todo]] match {
              case Success(todo) =>
                val result = context.todoActor.ask((ref: ActorRef[CreateTodoReply]) => CreateTodo(ref, todo))
                result.map(reply => reply.todo.map(t => t.as[Body]))
              case Failure(e) =>
                ScalaFuture(Failure(new IllegalArgumentException(e)))
            }
        }
      case Root / "todo" / Long(id) =>
        req.method match {
          case Method.Get =>
            val result: ScalaFuture[GetTodoReply] = context.todoActor.ask((ref: ActorRef[GetTodoReply]) => GetTodo(ref, id))
            result.map(reply => reply.todo.map(t => t.as[Body]))
          case Method.Put =>
            ???
          case Method.Delete =>
            ???
        }
      case _ =>
        ScalaFuture(Failure(new NoSuchMethodError("Unknown method")))
    }).map(toResponse).as[Future[http.Response]]
  }

  implicit val timeout: Timeout = 3.seconds
  implicit val ec: ExecutionContextExecutor = system.executionContext

  val setupActorSystem = system.ask((ref: ActorRef[SystemContext]) => Setup(ref))
  val systemContext = scala.concurrent.Await.result(setupActorSystem, 3.seconds)

  val server: ListeningServer = Http.serve(":8080", service(systemContext))
  Await.ready(server)


}
