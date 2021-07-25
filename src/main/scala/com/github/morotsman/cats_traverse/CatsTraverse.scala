package com.github.morotsman.cats_traverse

import cats.Applicative
import cats.implicits._

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.language.postfixOps

object CatsTraverse extends App {

  implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global

  def blocking(i: Int)(implicit ec: ExecutionContext): Future[Int] = Future {
    Thread.sleep(5000)
    i
  }(ec)

  val fResult: Future[List[Int]] = (1 to 8).toList.traverse(blocking(_))
  //val fResult: Future[List[Int]] = (1 to 9).toList.traverse(blocking(_))

  val startTime = System.currentTimeMillis()
  val result = Await.result(fResult, 10000 milli)

  println(s"got back ${result} after ${System.currentTimeMillis() - startTime} ms")

  val tmp: Applicative[({
    type F[a] = Future[Option[a]]
  })#F] = Applicative[Future].compose[Option]

  type FO[a] = Future[Option[a]]
  val tmp2: Applicative[FO] = Applicative[Future].compose[Option]

  val x: Future[Option[Int]] = Future.successful(Some(5))
  val y: Future[Option[Char]] = Future.successful(Some('a'))

  val composed: Future[Option[Int]] = Applicative[Future].compose[Option].map2(x, y)(_ + _)
}
