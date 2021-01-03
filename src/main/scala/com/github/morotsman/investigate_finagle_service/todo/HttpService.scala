package com.github.morotsman.investigate_finagle_service.todo

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.util.Timeout
import com.github.morotsman.investigate_finagle_service.todo.ActorSystemInitializer.{Setup, SystemContext}
import com.github.morotsman.investigate_finagle_service.todo.TodoActor.{CreateTodo, CreateTodoReply, DeleteTodo, DeleteTodoReply, GetTodo, GetTodoReply, GetTodosReply, ListTodos, ModifyTodo, ModifyTodoReply, Reply}
import com.twitter.bijection.Conversion
import com.twitter.finagle.http.{Method, Request, Response, Status}
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

  def service(context: SystemContext): Service[Request, Response] = (req: Request) => (Path(req.path) match {
    case Root / "todo" =>
      req.method match {
        case Method.Get =>
          context.todoActor.ask((ref: ActorRef[GetTodosReply]) => ListTodos(ref))
            .map(asBody(_))
        case Method.Post =>
          withBody[Todo](req) { todo =>
            context.todoActor.ask((ref: ActorRef[CreateTodoReply]) => CreateTodo(ref, todo))
              .map(asBody(_))
          }
        case _ =>
          ScalaFuture(Failure(new NoSuchMethodError(s"Unknown resource: ${req.path}")))
      }
    case Root / "todo" / Long(id) =>
      req.method match {
        case Method.Get =>
          context.todoActor.ask((ref: ActorRef[GetTodoReply]) => GetTodo(ref, id))
            .map(asBody(_))
        case Method.Put =>
          withBody[Todo](req) { todo =>
            context.todoActor.ask((ref: ActorRef[ModifyTodoReply]) => ModifyTodo(ref, id, todo))
              .map(asBody(_))
          }
        case Method.Delete =>
          context.todoActor.ask((ref: ActorRef[DeleteTodoReply]) => DeleteTodo(ref, id))
            .map(asBody(_))
        case _ =>
          ScalaFuture(Failure(new NoSuchMethodError(s"Unknown resource: ${req.path}")))
      }
    case _ =>
      ScalaFuture(Failure(new NoSuchMethodError(s"Unknown resource: ${req.path}")))
  }).map(toResponse).as[Future[http.Response]]

  def asBody[A](ta: Reply[A])(implicit ev: Conversion[A, String]): Try[Body] =
    ta.payload.map(t => t.as[Body])

  def withBody[A](req: Request)(handler: A => ScalaFuture[Try[Body]])(implicit ev: Conversion[String, Try[A]]) = {
    req.contentString.as[Try[A]] match {
      case Success(a) =>
        handler(a)
      case Failure(e) =>
        ScalaFuture(Failure(new IllegalArgumentException(e)))
    }
  }

  def toResponse(body: Try[Body]): http.Response = body match {
    case Success(b) =>
      val response = http.Response(Status.Ok)
      response.setContentTypeJson()
      response.contentString = b
      response
    case Failure(e: NoSuchMethodError) =>
      println(e)
      http.Response(Status.NotFound)
    case Failure(e: IllegalArgumentException) =>
      println(e)
      http.Response(Status.BadRequest)
    case Failure(e: NoSuchElementException) =>
      println(e)
      http.Response(Status.NotFound)
    case e@_ =>
      println("error: " + e)
      http.Response(Status.InternalServerError)
  }

  implicit val timeout: Timeout = 3.seconds
  implicit val ec: ExecutionContextExecutor = system.executionContext

  val setupActorSystem = system.ask((ref: ActorRef[SystemContext]) => Setup(ref))
  val systemContext = scala.concurrent.Await.result(setupActorSystem, 3.seconds)

  val server: ListeningServer = Http.serve(":8080", service(systemContext))
  Await.ready(server)


}
