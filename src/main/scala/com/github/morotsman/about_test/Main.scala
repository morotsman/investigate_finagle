package com.github.morotsman.about_test

import com.github.morotsman.about_test.dao.{CreditDaoImpl, CustomerDaoImpl, OrderDaoImpl}

import java.util.concurrent.Executors
import scala.concurrent.{Await, ExecutionContext, Future}
import cats.instances.future.catsStdInstancesForFuture

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

object Main extends App {

  val customerContext = ExecutionContext.fromExecutorService {
    Executors.newFixedThreadPool(8)
  }
  val customerDao = new CustomerDaoImpl()(customerContext)

  val orderContext = ExecutionContext.fromExecutorService {
    Executors.newFixedThreadPool(8)
  }
  val orderDao = new OrderDaoImpl()(orderContext)

  val creditContext = ExecutionContext.fromExecutorService {
    Executors.newFixedThreadPool(8)
  }
  val creditDao = new CreditDaoImpl()(creditContext)

  val CreateOrder = new CreateOrderImpl[Future](orderDao, customerDao, creditDao)

  val order: Order = Order(
    orderId = None,
    customer = Customer(
      customerId = "Some id",
      firstName = "John",
      lastName = "Doe"
    ),
    address = Address(
      street = "Some street 42",
      zipCode = "243221",
      city = "Malmoe",
      country = "Sweden"
    ),
    orderLines = Seq(
      OrderLine(
        itemCode = "1",
        quantity = 2,
        cost = 20
      )
    )
  )
  try {
    val startTime = System.currentTimeMillis()
    val futureResult = CreateOrder(order)
    val result = Await.result(futureResult,10000 milli)
    println(s"Got $result in ${System.currentTimeMillis - startTime} ms")
  } finally {
    customerContext.shutdown()
    orderContext.shutdown()
    creditContext.shutdown()
  }

}
