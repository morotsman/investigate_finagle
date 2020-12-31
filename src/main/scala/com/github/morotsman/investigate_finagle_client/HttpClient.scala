package com.github.morotsman.investigate_finagle_client

import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}

import com.twitter.conversions.DurationOps._
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.param.HighResTimer
import com.twitter.finagle.service.{Backoff, RetryBudget, RetryFilter, RetryPolicy}
import com.twitter.finagle.stats.NullStatsReceiver
import com.twitter.finagle.{Failure, Http, Service, TimeoutException, http}
import com.twitter.util.{Await, Awaitable, Future, Return, Throw, Try}

import scala.io.StdIn

object HttpClient extends App {

  def singleRequest(): Unit = {
    val client: Service[http.Request, http.Response] = Http.newService("localhost:9000")
    val request = http.Request(http.Method.Get, "/mock-service/test")
    request.host = "localhost"
    val response: Future[http.Response] = client(request)
    val result = Await.result(response.onSuccess { rep: http.Response => println("GET success: " + rep) })
    println(s"result: $result")
  }

  singleRequest()

  type TestClient = Service[Request, Response]

  def requestProgram(client: TestClient): Unit = {
    val request = http.Request(http.Method.Get, "/mock-service/test")
    print("Number of requests/sec: ")
    val input = StdIn.readLine().toInt

    while (true) {
      val startTime = System.currentTimeMillis()
      val requests: Seq[Future[Response]] = (1 to input).map(_ => client(request))
      val allCompleted: Future[Seq[Response]] = Future.collect(requests)
      allCompleted.onSuccess(result => {
        //println(result)
        //println(s"Finished in ${System.currentTimeMillis() - startTime} ms")
      })
      Thread.sleep(1000)
    }
  }

  val clientWithTimeOut = Http.client
    .withRequestTimeout(10000.millis)
    .newService("localhost:9000")


  val errorPolicy: PartialFunction[(Request, Try[Response]), Boolean] = {
    case (_, Throw(_: TimeoutException)) =>
      val now = System.currentTimeMillis()
      println(s"Request timed out, will try again.")
      true
    case (_, Return(response)) if response.statusCode == 503 =>
      println(s"Will retry: $response")
      true
    case error@_ => {
      false
    }
  }

  private def simpleRetryPolicy: RetryPolicy[(Request, Try[Response])] = RetryPolicy.tries(200, errorPolicy)

  val retryBudget = RetryBudget(ttl = 5.seconds, minRetriesPerSec = 5, percentCanRetry = 0.1)

  val clientWithTimeOutAndRetry = Http.client
    .withRequestTimeout(100.millis)
    .withRetryBackoff(Backoff.decorrelatedJittered(2.seconds, 32.seconds))
    .withRetryBudget(retryBudget)
    .filtered(new RetryFilter[Request, Response](
      simpleRetryPolicy,
      HighResTimer.Default,
      NullStatsReceiver,
      retryBudget))
    .newService("localhost:9000")


  private def retryPolicyWithConfiguredBackOff: RetryPolicy[(Request, Try[Response])] = RetryPolicy.backoff(Backoff.equalJittered(1.second, 10.seconds))(errorPolicy)

  val clientWithLoadBalancing = Http.client
    .withRequestTimeout(200.millis)
    .withRetryBackoff(Backoff.decorrelatedJittered(2.seconds, 32.seconds))
    .withRetryBudget(retryBudget)
    .filtered(new RetryFilter[Request, Response](
      retryPolicyWithConfiguredBackOff,
      HighResTimer.Default,
      NullStatsReceiver,
      retryBudget))
    .newService("localhost:9000,localhost:19001")

  requestProgram(clientWithLoadBalancing)
}
