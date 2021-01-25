package com.github.morotsman.investigate_finagle_service.candy_finch

import cats.effect.concurrent.Ref
import cats.effect.{ContextShift, IO}
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response, Status}
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._


class App(
           idRef: Ref[IO, Int],
           storeRef: Ref[IO, Map[Int, MachineState]]
         )(implicit
           S: ContextShift[IO]
         ) extends Endpoint.Module[IO] {


  final val assignId: Endpoint[IO, MachineState] =
    jsonBody[Int => MachineState].mapAsync(pt => idRef.modify(id => (id + 1, pt(id))))


  final val createMachine: Endpoint[IO, MachineState] = post(path("machine") :: assignId) { m: MachineState =>
    storeRef.modify { store =>
      (store + (m.id -> m), Created(m))
    }
  }

  final val getMachines: Endpoint[IO, List[MachineState]] = get(path("machine")) {
    storeRef.get.map(m => Ok(m.values.toList.sortBy(_.id)))
  }

  final val insertCoin: Endpoint[IO, MachineState] = put(path("machine") :: path[Int] :: path("coin")) {
    handleInput(Coin)
  }

  private def handleInput(input: Input) = {
    id: Int => {
      storeRef.modify { store =>
        val result = for {
          machine <- store.get(id).toRight(new NoSuchElementException)
          newMachine <- CandyRule.applyRule(input)(machine)
        } yield (newMachine)

        result match {
          case Right(m) => (store + (id -> m), Ok(m))
          case Left(e: NoSuchElementException) => (store, Output.empty(Status.NotFound))
          case Left(e: IllegalStateException) => (store, Output.empty(Status.BadRequest))
        }
      }
    }
  }

  final val turn: Endpoint[IO, MachineState] = put("machine" :: path[Int] :: "turn") {
    handleInput(Turn)
  }


  final def toService: Service[Request, Response] = Bootstrap
    .serve[Application.Json](createMachine :+: getMachines :+: insertCoin :+: turn)
    .toService
}
