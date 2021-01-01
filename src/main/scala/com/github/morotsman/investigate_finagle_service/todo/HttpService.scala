package com.github.morotsman.investigate_finagle_service.todo

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.util.Timeout
import com.github.morotsman.investigate_finagle_service.todo.ActorSystemInitializer.{Setup, SystemContext}
import com.github.morotsman.investigate_finagle_service.todo.TodoActor.{ListTodos, TodoReply, Todos}
import com.twitter.finagle.http.Method
import com.twitter.finagle.http.path.{/, Path, Root}
import com.twitter.finagle.{Http, ListeningServer, Service, http}
import com.twitter.util.{Await, Future}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future => ScalaFuture}
import com.twitter.bijection.Conversion.asMethod
import com.twitter.bijection.twitter_util.UtilBijections._

import scala.util.Try

object HttpService extends App {

  implicit val system: ActorSystem[Setup] =
    ActorSystem(ActorSystemInitializer.setup, "todo")


  def service(context: SystemContext) = new Service[http.Request, http.Response] {
    def apply(req: http.Request): Future[http.Response] = {
      println(s"Received request: $req")

      val response = Path(req.path) match {
        case Root / "todo" =>
          req.method match {
            case Method.Get =>
              val result = context.todoActor.ask((ref: ActorRef[Todos]) => ListTodos(ref))
              result.map(reply => {
                  val response = http.Response(req.version, http.Status.Ok)
                  response.setContentTypeJson()
                  response.contentString = reply.todos.as[String]
                  response
                })
            case Method.Post =>
              val test: Try[Todo] = Todo.todoToJsonString.invert(req.contentString)
              println(test)
              ???
          }
        case Root / "todo" / id =>
          req.method match {
            case Method.Get => ???
            case Method.Put => ???
            case Method.Delete => ???
          }
      }

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
