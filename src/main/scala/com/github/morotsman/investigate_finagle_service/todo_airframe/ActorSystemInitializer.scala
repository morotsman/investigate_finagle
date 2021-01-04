package com.github.morotsman.investigate_finagle_service.todo_airframe

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import com.github.morotsman.investigate_finagle_service.todo_airframe.TodoActor.Request

object ActorSystemInitializer {

  case class Setup(replyTo: ActorRef[SystemContext])

  case class SystemContext(todoActor: ActorRef[Request])

  def setup: Behavior[Setup] =
    Behaviors.setup { context =>
      val ref: ActorRef[Request] = context.spawn(TodoActor(), "todo")

      Behaviors.receiveMessage { message =>
        message.replyTo ! SystemContext(ref)
        Behaviors.same
      }
    }
}

