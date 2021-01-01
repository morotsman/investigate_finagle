package com.github.morotsman.investigate_finagle_service.todo

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object TodoActor {

  sealed trait TodoRequest

  case class ListTodos(replyTo: ActorRef[Todos]) extends TodoRequest

  case class CreateTodo(title: String) extends TodoRequest

  case class GetTodo(id: Long) extends TodoRequest

  case class ModifyTodo(id: Long, title: String, completed: Boolean) extends TodoRequest

  case class DeleteTodo(id: Long) extends TodoRequest

  sealed trait TodoReply

  case class Todos(todos: List[Todo]) extends TodoReply

  def apply(): Behavior[TodoRequest] = {
    behave(0L, Map[Long, Todo]())
  }

  def behave(currentId: Long, todos: Map[Long, Todo]): Behavior[TodoRequest] = Behaviors.receive { (context, message) =>
    message match {
      case ListTodos(replyTo) =>
        replyTo ! Todos(todos.values.toList)
        Behaviors.same
      case _ => ???
    }
  }

}
