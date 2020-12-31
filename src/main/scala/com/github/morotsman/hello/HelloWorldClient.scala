package com.github.morotsman.hello

import com.twitter.finagle.Thrift
import com.twitter.util.Await


object HelloWorldClient {

  def main(args: Array[String]): Unit = {
    //#thriftclientapi

    val client = Thrift.client
      .build[Hello.MethodPerEndpoint]("localhost:9090")
    val response = client.hi().onSuccess { response => println("Received response: " + response) }
    val result = Await.result(response)
    println(result)
    //#thriftclientapi
  }

}
