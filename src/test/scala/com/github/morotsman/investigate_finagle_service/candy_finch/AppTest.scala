package com.github.morotsman.investigate_finagle_service.candy_finch

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalacheck.{Arbitrary, Gen}
import cats.effect.concurrent.Ref
import cats.effect.{ContextShift, IO}
import io.finch.internal.DummyExecutionContext

import org.scalacheck.Properties
import org.scalacheck.Prop.forAll




// TODO redo the tests
class AppTestxw extends AnyFlatSpec with Matchers {


  case class AppState(id: Int, store: Map[Int, MachineState])

  case class TestApp(
                      id: Ref[IO, Int],
                      store: Ref[IO, Map[Int, MachineState]]
                    ) extends App(id, store)(IO.contextShift(DummyExecutionContext)) {
    def state: IO[AppState] = for {
      i <- id.get
      s <- store.get
    } yield AppState(i, s)
  }

  case class MachineWithoutId(locked: Boolean, candies: Int, coins: Int) {
    def withId(id: Int): MachineState = MachineState(id, locked, candies, coins)
  }

  val genMachineWithoutId = for {
    locked <- Gen.oneOf(true, false)
    candies <- Gen.choose(-10, 10)
    coins <- Gen.choose(-10, 10)
  } yield MachineWithoutId(locked, candies, coins)






}
