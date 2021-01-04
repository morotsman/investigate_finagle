package com.github.morotsman.investigate_finagle_service.todo_airframe

import akka.actor.FSM.Failure
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.util.Timeout
import com.github.morotsman.investigate_finagle_service.todo_airframe.ActorSystemInitializer.{Setup, SystemContext}
import com.github.morotsman.investigate_finagle_service.todo_airframe.TodoActor.{CreateTodo, CreateTodoReply, DeleteTodo, DeleteTodoReply, GetTodo, GetTodoReply, GetTodosReply, ListTodos, ModifyTodo, ModifyTodoReply}
import com.twitter.bijection.Conversion.asMethod
import com.twitter.util.Future
import wvlet.airframe.http.finagle.{Finagle, FinagleFilter, FinagleServer}
import wvlet.airframe.http.{Endpoint, Http, HttpMethod, Router}

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import com.twitter.bijection.twitter_util.UtilBijections._
import com.twitter.finagle.http.{Request, Response, Status}

class TodoApi(
               val context: SystemContext,
               implicit val ec: ExecutionContextExecutor,
               implicit val system: ActorSystem[Setup]) {

  private implicit val timeout: Timeout = 3.seconds

  @Endpoint(method = HttpMethod.GET, path = "/todo")
  def getTodos: Future[List[Todo]] =
    context.todoActor
      .ask((ref: ActorRef[GetTodosReply]) => ListTodos(ref))
      .map(r => r.payload.get)
      .as[Future[List[Todo]]]

  @Endpoint(method = HttpMethod.POST, path = "/todo")
  def createTodo(todo: Todo): Future[Todo] =
    context.todoActor
      .ask((ref: ActorRef[CreateTodoReply]) => CreateTodo(ref, todo))
      .map(r => r.payload.get)
      .as[Future[Todo]]

  @Endpoint(method = HttpMethod.PUT, path = "/todo/:id")
  def modifyTodo(id: Long, todo: Todo): Future[Todo] =
    context.todoActor
      .ask((ref: ActorRef[ModifyTodoReply]) => ModifyTodo(ref, id, todo))
      .map(r => r.payload.get)
      .as[Future[Todo]]

  @Endpoint(method = HttpMethod.DELETE, path = "/todo/:id")
  def deleteTodo(id: Long): Future[Todo] =
    context.todoActor
      .ask((ref: ActorRef[DeleteTodoReply]) => DeleteTodo(ref, id))
      .map(r => r.payload.get)
      .as[Future[Todo]]

  @Endpoint(method = HttpMethod.GET, path = "/todo/:id")
  def getTodo(id: Long): Future[Todo] =
    context.todoActor
      .ask((ref: ActorRef[GetTodoReply]) => GetTodo(ref, id))
      .map(r => r.payload.get)
      .as[Future[Todo]]

}

object ErrorHandler extends FinagleFilter {
  def apply(request: Request, context: Context): Future[Response] = {
    context(request).handle {
      case e: NoSuchElementException =>
        Response(Status.NotFound)
    }
  }
}

object AirframeHttpService extends App {
  implicit val system: ActorSystem[Setup] =
    ActorSystem(ActorSystemInitializer.setup, "todo")

  implicit val timeout: Timeout = 3.seconds
  implicit val ec: ExecutionContextExecutor = system.executionContext

  val setupActorSystem = system.ask((ref: ActorRef[SystemContext]) => Setup(ref))
  val systemContext = scala.concurrent.Await.result(setupActorSystem, 3.seconds)

  val router = Router.add(ErrorHandler).andThen[TodoApi]

  val design = Finagle.server
    .withPort(8080)
    .withRouter(router)
    .design

  design
    .bind[SystemContext].toInstance(systemContext)
    .bind[ExecutionContextExecutor].toInstance(ec)
    .bind[ActorSystem[Setup]].toInstance(system)
    .build[FinagleServer] { server =>
      server.waitServerTermination
    }

}
