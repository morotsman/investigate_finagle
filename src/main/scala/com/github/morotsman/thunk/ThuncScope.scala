package com.github.morotsman.thunk

trait ThunkConsumer {

  def status: String = "hepp"

  def thunkTest(t: => Unit): Unit = t

}

object MyApp extends App with ThunkConsumer {

  thunkTest {
    println(status)
  }

}
