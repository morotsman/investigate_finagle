package com.github.morotsman.io_monad

import cats.effect.IO

object HelloWorld extends App {

  val ioa = IO { println("Hello world!") }

  val program: IO[Unit] =
    for {
      _ <- ioa
      _ <- ioa
    } yield ()

  val result: Unit = program.unsafeRunSync()
}
