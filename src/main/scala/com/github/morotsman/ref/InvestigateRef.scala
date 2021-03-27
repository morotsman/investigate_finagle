package com.github.morotsman.ref

import cats.effect.IO
import cats.effect.concurrent.Ref
import scala.concurrent.ExecutionContext
import cats.implicits._

object InvestigateRef extends App {
  implicit val ctx = IO.contextShift(ExecutionContext.global)

  def printLn(s: String): IO[Unit] = IO(println(s"${Thread.currentThread()}: $s"))

  def addOne(ref: Ref[IO, Int]): IO[Unit] = for {
    v1 <- ref.get
    _ <- printLn(s"Before modify (get): $v1")
    v2 <- ref.modify(v => {
      println(s"${Thread.currentThread()}: Inside modify: $v");
      (v + 1, v)
    })
    _ <- printLn(s"Result from modify: $v2")
    v3 <- ref.get
    _ <- printLn(s"After modify (get): $v3")
  } yield ();

  def program1(ref: Ref[IO, Int]): IO[Unit] = for {
    _ <- addOne(ref)
    _ <- addOne(ref)
    _ <- addOne(ref)
  } yield ()

  val myRef: Ref[IO, Int] = Ref.unsafe[IO, Int](42)
  program1(myRef).unsafeRunSync()
  program1(myRef).unsafeRunSync()

  def program2(ref: IO[Ref[IO, Int]]): IO[Unit] = for {
    r <- ref
    _ <- program1(r)
  } yield ()

  val myRef2: IO[Ref[IO, Int]] = Ref[IO].of(42)
  program2(myRef2).unsafeRunSync()
  program2(myRef2).unsafeRunSync()


  def program3(ref: IO[Ref[IO, Int]]): IO[Unit] = for {
    r <- ref
    _ <- List(
      addOne(r),
      addOne(r),
      addOne(r)
    ).parSequence
  } yield ();

  program3(myRef2).unsafeRunSync()


}
