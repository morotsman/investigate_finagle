package com.github.morotsman.hello

import com.twitter.finagle.Thrift
import com.twitter.util.{Await, Future}
import com.twitter.app.App

// https://medium.com/@muuki88/a-beginners-guide-for-twitter-finagle-7ff7189541e5
object HelloWorldServer extends App {


  def main(): Unit = {
    val server = Thrift.server.serveIface(
      "localhost:9090",
      new Hello[Future] {
        def hi(): Future[String] = Future.value("hi")
      })
    Await.ready(server)
  }

}
