package com.github.morotsman.investigate_finagle_service.candy_finch

import cats.effect.IO
import cats.effect.concurrent.Ref
import com.twitter.finagle.http.Status
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._
import io.finch.internal.DummyExecutionContext
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.Checkers._

class AppTestxw extends AnyFlatSpec with Matchers {


  private case class AppState(id: Int, store: Map[Int, MachineState])

  private case class TestApp(
                              id: Ref[IO, Int],
                              store: Ref[IO, Map[Int, MachineState]]
                            ) extends App(id, store)(IO.contextShift(DummyExecutionContext)) {
    def state: IO[AppState] = for {
      i <- id.get
      s <- store.get
    } yield AppState(i, s)
  }

  private case class MachineWithoutId(locked: Boolean, candies: Int, coins: Int) {
    def withId(id: Int): MachineState = MachineState(id, locked, candies, coins)
  }

  private val genMachineWithoutId = for {
    locked <- Gen.oneOf(true, false)
    candies <- Gen.choose(0, 3)
    coins <- Gen.choose(0, 1000)
  } yield MachineWithoutId(locked, candies, coins)

  private def genTestApp: Gen[TestApp] =
    Gen.listOf(genMachineWithoutId).map { machines =>
      val id = machines.length
      val store = machines.zipWithIndex.map { case (t, i) => i -> t.withId(i) }

      TestApp(Ref.unsafe[IO, Int](id), Ref.unsafe[IO, Map[Int, MachineState]](store.toMap))
    }

  private implicit def arbitraryTodoWithoutId: Arbitrary[MachineWithoutId] = Arbitrary(genMachineWithoutId)

  private implicit def arbitraryApp: Arbitrary[TestApp] = Arbitrary(genTestApp)

  it should "create a machine" in {
    check { (app: TestApp, machine: MachineWithoutId) =>
      val input = Input.post("/machine").withBody[Application.Json](machine)

      val shouldBeTrue = for {
        prev <- app.state
        newMachine <- app.createMachine(input).output.get
        next <- app.state
      } yield prev.id + 1 == next.id &&
        prev.store + (prev.id -> newMachine.value) == next.store &&
        newMachine.value == machine.withId(prev.id)

      shouldBeTrue.unsafeRunSync()
    }
  }

  it should "give back the state of all the machines" in {
    check { (app: TestApp) =>
      val input = Input.get("/machine")

      val shouldBeTrue = for {
        prev <- app.state
        machines <- app.getMachines(input).output.get
        next <- app.state
      } yield {
        validateNoChange(prev, next) &&
          machines.value == prev.store.values.toList.sortBy(_.id)
      }

      shouldBeTrue.unsafeRunSync()
    }
  }

  it should "accept coins" in {
    check { (app: TestApp) =>
      val id = 0
      val input = Input.put(s"/machine/$id/coin")

      val shouldBeTrue = for {
        prev <- app.state
        result <- app.insertCoin(input).output.get
        next <- app.state
      } yield {
        (result.status == Status.NotFound && validateNoChange(prev, next)) && validateNotFound(id, prev)||
          (result.status == Status.BadRequest && validateNoChange(prev, next)) && validateBadRequest(id, prev, Coin) ||
          (result.status == Status.Ok && validateUnlocked(id, prev, next))
      }

      shouldBeTrue.unsafeRunSync()
    }
  }

  private def validateNoChange(prev: AppState, next: AppState): Boolean =
    validateIdSame(prev, next) && validateStoreSame(prev, next)

  private def validateStoreSame(prev: AppState, next: AppState): Boolean =
    prev.store == next.store

  private def validateIdSame(prev: AppState, next: AppState): Boolean =
    prev.id == next.id

  def validateNotFound(id: Int, prev: AppState): Boolean =
    prev.store.get(id).isEmpty

  def validateBadRequest(id: Int, prev: AppState, command: Input): Boolean = command match {
    case Turn =>
      prev.store(id).locked || prev.store(id).candies <= 0
    case Coin =>
      !prev.store(id).locked || prev.store(id).candies <= 0
  }

  def validateUnlocked(id: Int, prev: AppState, next: AppState): Boolean = (for {
    pm <- prev.store.get(id)
    nm <- next.store.get(id)
    if pm.locked
    if !nm.locked
    if pm.candies > 0
    if nm.candies == pm.candies
    if nm.coins == pm.coins + 1
    if (validateIdSame(prev, next))
    if (prev.store.filter(kv => kv._1 != id) == next.store.filter(kv => kv._1 != id))
  } yield true).getOrElse(false)

  it should "turn" in {
    check { (app: TestApp) =>
      val id = 0
      val input = Input.put(s"/machine/$id/turn")

      val shouldBeTrue = for {
        prev <- app.state
        result <- app.turn(input).output.get
        next <- app.state
      } yield {
        (result.status == Status.NotFound && validateNoChange(prev, next)) && validateNotFound(id, prev) ||
          (result.status == Status.BadRequest && validateNoChange(prev, next) && validateBadRequest(id, prev, Turn)) ||
          (result.status == Status.Ok && validateReturnCandy(id, prev, next))
      }

      shouldBeTrue.unsafeRunSync()
    }
  }

  def validateReturnCandy(id: Int, prev: AppState, next: AppState): Boolean = (for {
    pm <- prev.store.get(id)
    nm <- next.store.get(id)
    if !pm.locked
    if nm.locked
    if pm.candies - 1 == nm.candies
    if pm.coins == nm.coins
    if validateIdSame(prev, next)
    if (prev.store.filter(kv => kv._1 != id) == next.store.filter(kv => kv._1 != id))
  } yield true).getOrElse(false)

}
