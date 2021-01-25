package com.github.morotsman.investigate_finagle_service.candy_finch

object CandyRule {
  def applyRule(input: Input)(machine: MachineState): Either[Throwable, MachineState] = input match {
    case Coin =>
      if (machine.candies <= 0) {
        Left(new IllegalStateException("No candies left"))
      } else if (machine.locked) {
        Right(machine.copy(locked = false, coins = machine.coins + 1))
      } else {
        Left(new IllegalStateException("A coin has already been disposed"))
      }
    case Turn =>
      if (machine.candies <= 0) {
        Left(new IllegalStateException("No candies left"))
      } else if (!machine.locked) {
        Right(machine.copy(locked = true, candies = machine.candies - 1))
      } else {
        Left(new IllegalStateException("No coin has been disposed"))
      }
  }
}

sealed trait Input

case object Coin extends Input

case object Turn extends Input

