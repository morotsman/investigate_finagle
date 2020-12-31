package com.github.morotsman.investigate_finagle_client

import com.twitter.conversions.DurationOps._
import com.twitter.finagle.http.Response
import com.twitter.finagle.loadbalancer.LoadBalancerFactory.WhenNoNodesOpenParam
import com.twitter.finagle.loadbalancer.{NoNodesOpenException, WhenNoNodesOpen}
import com.twitter.finagle.{Http, IndividualRequestTimeoutException, Service, http}
import com.twitter.util._

object InvestigateCircuitBreaker extends App {


  def withTimeOutAndFallback(): Unit = {
    val client: Service[http.Request, http.Response] = Http.client
      .configured(WhenNoNodesOpenParam(WhenNoNodesOpen.FailFast))
      .withRequestTimeout(200.millis)
      .newService("localhost:9000")

    val request = http.Request(http.Method.Get, "/mock-service/test")

    while (true) {
      val startTime = System.currentTimeMillis()
      val response: Future[http.Response] = client(request).rescue {
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
      }

      println(s"After call to client: ${System.currentTimeMillis() - startTime} ms has elapsed")
      response.onSuccess(result => {
        println(s"After await: ${System.currentTimeMillis() - startTime} ms has elapsed")
        println(s"result: $result")
        println()
      })

      Thread.sleep(1000)
    }


  }

  withTimeOutAndFallback()
}
