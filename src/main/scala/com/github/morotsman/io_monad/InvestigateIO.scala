package com.github.morotsman.io_monad

import cats.effect.IO
import cats.implicits._

import scala.annotation.tailrec

object InvestigateIO extends App {

  def sayHello(): Unit = println("Hello")

  sayHello();

  def sayHelloWithIO(): IO[Unit] = IO {
    println("Hello with IO")
  }

  val program1: IO[Unit] = sayHelloWithIO()

  program1.unsafeRunSync()


  trait Console {
    def printLn(s: String): Unit
    def readLine(): String
  }

  class ConsoleImpl extends Console {
    override def printLn(s: String): Unit = println(s)

    override def readLine(): String = scala.io.StdIn.readLine()
  }


  def guessTheNumber(console: Console)(answer: Int): Unit = {

    @tailrec
    def loop(numberOfGuesses: Int): Unit = {
      console.printLn("Guess of a number between 0 and 1000: ")
      val line = console.readLine()
      val guess = line.toInt
      if (guess == answer) {
        console.printLn(s"You are correct, the number is: $answer. You got it in $numberOfGuesses guesses.")
      } else if (guess < answer) {
        console.printLn(s"Your guess is too low.")
        loop(numberOfGuesses + 1)
      } else {
        console.printLn(s"Your guess is too high.")
        loop(numberOfGuesses + 1)
      }
    }

    loop(1)
  }

  val rnd = new scala.util.Random()
  val test: Unit = guessTheNumber(new ConsoleImpl)(rnd.nextInt(1001))


  def guessTheNumberWithIO(console: Console)(answer: Int): IO[Unit] = {

    def printLn(s: String): IO[Unit] = IO {
      console.printLn(s)
    }

    def readLn(): IO[String] = IO {
      console.readLine()
    }

    def loop(numberOfGuesses: Int): IO[Unit] = for {
      _ <- printLn("Guess of a number between 0 and 1000: ")
      g <- readLn()
      _ <- {
        val guess = g.toInt
        if (guess == answer) {
          printLn(s"You are correct, the number is: $answer. You got it in $numberOfGuesses guesses.")
        } else if (guess < answer) {
          printLn(s"Your guess is too low.") >> loop(numberOfGuesses + 1)
        } else {
          printLn(s"Your guess is too high.") >> loop(numberOfGuesses + 1)
        }
      }

    } yield ()

    loop(1)
  }

  val program2: IO[Unit] = guessTheNumberWithIO(new ConsoleImpl)(rnd.nextInt(1001))
  // program2.unsafeRunSync()

}
