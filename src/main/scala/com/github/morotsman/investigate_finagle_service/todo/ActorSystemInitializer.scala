package com.github.morotsman.investigate_finagle_service.todo

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import com.github.morotsman.investigate_finagle_service.todo.TodoActor.TodoRequest

object ActorSystemInitializer {

  case class Setup(replyTo: ActorRef[SystemContext])

  case class SystemContext(todoActor: ActorRef[TodoRequest])

  def setup: Behavior[Setup] =
    Behaviors.setup { context =>
      val ref: ActorRef[TodoRequest] = context.spawn(TodoActor(), "todo")

      Behaviors.receiveMessage { message =>
        message.replyTo ! SystemContext(ref)
        Behaviors.same
      }
    }
}

