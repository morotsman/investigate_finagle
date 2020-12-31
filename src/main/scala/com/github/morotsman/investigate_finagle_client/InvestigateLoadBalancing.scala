package com.github.morotsman.investigate_finagle_client

import com.twitter.conversions.DurationOps._
import com.twitter.finagle.http.Response
import com.twitter.finagle.loadbalancer.LoadBalancerFactory.WhenNoNodesOpenParam
import com.twitter.finagle.loadbalancer.{NoNodesOpenException, WhenNoNodesOpen}
import com.twitter.finagle.{Http, IndividualRequestTimeoutException, Service, http}
import com.twitter.util._

object InvestigateLoadBalancing extends App {


  def withTimeOutAndFallback(): Unit = {
    val client = Http.client
      .configured(WhenNoNodesOpenParam(WhenNoNodesOpen.FailFast))
      .withRequestTimeout(200.millis)
      .newService("localhost:9000,localhost:19001")

    val request = http.Request(http.Method.Get, "/mock-service/test")

    while (true) {
      val startTime = System.currentTimeMillis()
      val responses = (1 to 10).map(_ => client(request).rescue {
        case _: IndividualRequestTimeoutException =>
          println(s"Got timeout after: ${System.currentTimeMillis() - startTime} ms")
          val fallbackResponse = Response()
          Future(fallbackResponse)
        case _: NoNodesOpenException =>
          println(s"No node available: ${System.currentTimeMillis() - startTime} ms")
          val fallbackResponse = Response()
          Future(fallbackResponse)
        case e: Exception =>
          println("hepp: " + e)
          throw e
      })

      val response: Future[Seq[Response]] = Future.collect(responses)

      println(s"After call to client: ${System.currentTimeMillis() - startTime} ms has elapsed")
      response.onSuccess(result => {
        println(s"After await: ${System.currentTimeMillis() - startTime} ms has elapsed")
        println(s"success count: ${result}")
        println(s"success count: ${result.length}")
        println()
      })

      Thread.sleep(1000)
    }


  }

  withTimeOutAndFallback()
}
