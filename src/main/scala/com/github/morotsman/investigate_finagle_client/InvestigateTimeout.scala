package com.github.morotsman.investigate_finagle_client

import com.twitter.conversions.DurationOps._
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.param.HighResTimer
import com.twitter.finagle.service.{Backoff, RetryBudget, RetryFilter, RetryPolicy}
import com.twitter.finagle.stats.NullStatsReceiver
import com.twitter.finagle.{Http, IndividualRequestTimeoutException, Service, TimeoutException, http}
import com.twitter.util._

import scala.io.StdIn

object InvestigateTimeout extends App {

  def withoutTimeOut(): Unit = {
    val client: Service[http.Request, http.Response] = Http.client.newService("localhost:9000")
    val request = http.Request(http.Method.Get, "/mock-service/test")

    println("first call")

    var startTime = System.currentTimeMillis()
    var response: Future[http.Response] = client(request)
    println(s"After call to client: ${System.currentTimeMillis() - startTime} ms has elapsed")
    var result = Await.result(response)
    println(s"After await: ${System.currentTimeMillis() - startTime} ms has elapsed")
    println(s"result: $result")

    println()
    println("second call")

    startTime = System.currentTimeMillis()
    response = client(request)
    println(s"After call to client: ${System.currentTimeMillis() - startTime} ms has elapsed")
    result = Await.result(response)
    println(s"After await: ${System.currentTimeMillis() - startTime} ms has elapsed")
    println(s"result: $result")
  }

  // withoutTimeOut()

  def withTimeOut(): Unit = {
    val client: Service[http.Request, http.Response] = Http.client
      .withRequestTimeout(200.millis)
      .newService("localhost:9000")
    val request = http.Request(http.Method.Get, "/mock-service/test")

    println("first call")

    val startTime = System.currentTimeMillis()
    val response: Future[http.Response] = client(request)
    println(s"After call to client: ${System.currentTimeMillis() - startTime} ms has elapsed")
    val result = Await.result(response)
    println(s"After await: ${System.currentTimeMillis() - startTime} ms has elapsed")
    println(s"result: $result")
  }

  def withTimeOutAndFallback(): Unit = {
    val client: Service[http.Request, http.Response] = Http.client
      .withRequestTimeout(200.millis)
      .newService("localhost:9000")

    val request = http.Request(http.Method.Get, "/mock-service/test")

    val startTime = System.currentTimeMillis()
    val response: Future[http.Response] = client(request).rescue {
      case _: IndividualRequestTimeoutException =>
        val fallbackResponse = Response()
        Future(fallbackResponse)
      case e: Exception => throw e
    }

    println(s"After call to client: ${System.currentTimeMillis() - startTime} ms has elapsed")
    val result = Await.result(response)
    println(s"After await: ${System.currentTimeMillis() - startTime} ms has elapsed")
    println(s"result: $result")

  }

  withTimeOutAndFallback()
}
