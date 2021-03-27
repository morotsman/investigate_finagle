package com.github.morotsman.io_monad

import cats.effect.{IO, SyncIO, Timer}
import cats.implicits._
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext


object RunCancelablePure extends App {

  // Needed for `sleep`
  implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)

  // Delayed println
  val io: IO[Unit] = IO.sleep(10.seconds) *> IO(println("Hello!"))

  val pureResult: SyncIO[IO[Unit]] = io.runCancelable {
    r => IO(println(s"Done: $r"))
  }

  // On evaluation, this will first execute the source, then it
  // will cancel it, because it makes perfect sense :-)

  // pureResult.toIO.flatten

  Thread.sleep(20000)

}
