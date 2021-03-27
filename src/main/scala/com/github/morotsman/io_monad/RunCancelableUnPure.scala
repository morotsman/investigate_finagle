package com.github.morotsman.io_monad

import cats.effect.{IO, Timer}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import cats.implicits._

object RunCancelableUnPure extends App {

  // Needed for `sleep`
  implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)

  // Delayed println
  val io: IO[Unit] = IO.sleep(10.seconds) *> IO(println("Hello!"))

  val cancel: IO[Unit] =
    io.unsafeRunCancelable(r => println(s"Done: $r"))

  // ... if a race condition happens, we can cancel it,
  // thus canceling the scheduling of `IO.sleep`

  // cancel.unsafeRunSync()

  Thread.sleep(20000)
}
