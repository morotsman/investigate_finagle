package com.github.morotsman.investigate_finagle_service.todo

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

import scala.util.{Success, Try}

object TodoActor {

  sealed trait Request

  case class ListTodos(replyTo: ActorRef[GetTodosReply]) extends Request

  case class CreateTodo(replyTo: ActorRef[CreateTodoReply], todo: Todo) extends Request

  case class GetTodo(replyTo: ActorRef[GetTodoReply], id: Long) extends Request

  case class ModifyTodo(replyTo: ActorRef[ModifyTodoReply], id: Long, todo: Todo) extends Request

  case class DeleteTodo(replyTo: ActorRef[DeleteTodoReply], id: Long) extends Request

  sealed trait Reply

  case class GetTodosReply(todos: Try[List[Todo]]) extends Reply

  case class CreateTodoReply(todo: Try[Todo]) extends Reply

  case class GetTodoReply(todo: Try[Todo]) extends Reply

  case class ModifyTodoReply(todo: Try[Todo]) extends Reply

  case class DeleteTodoReply(todo: Try[Todo]) extends Reply

  def apply(): Behavior[Request] = {
    behave(0L, Map[Long, Todo]())
  }

  def behave(currentId: Long, todos: Map[Long, Todo]): Behavior[Request] = Behaviors.receive { (context, message) =>
    message match {
      case ListTodos(replyTo) =>
        replyTo ! GetTodosReply(Try(todos.values.toList))
        Behaviors.same
      case CreateTodo(replyTo, todo) =>
        val newTodo = todo.copy(id = Some(currentId))
        replyTo ! CreateTodoReply(Success(newTodo))
        behave(currentId + 1, todos + (currentId -> newTodo))
      case GetTodo(replyTo, id) =>
        val todo = Try(todos(id))
        replyTo ! GetTodoReply(todo)
        Behaviors.same
      case ModifyTodo(replyTo, id, todo) =>
        val newTodo = Try(todos(id)).map(t => t.copy(title = todo.title, completed = todo.completed))
        replyTo ! ModifyTodoReply(newTodo)
        newTodo match {
          case Success(t) =>
            behave(currentId, todos + (id -> t))
          case _ =>
            Behaviors.same
        }
      case DeleteTodo(replyTo, id) =>
        val todo = Try(todos(id))
        replyTo ! DeleteTodoReply(todo)
        todo match {
          case Success(_) =>
            behave(currentId, todos - id)
          case _ =>
            Behaviors.same
        }
      case _ => ???
    }
  }

}
