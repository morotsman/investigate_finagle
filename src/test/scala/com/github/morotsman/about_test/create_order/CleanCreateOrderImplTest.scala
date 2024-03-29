package com.github.morotsman.about_test.create_order

import cats.implicits._
import com.github.morotsman.about_test.{CreateOrderImpl, Credit, CreditDao, CreditLimitExceeded, CustomerDao, OrderDao, Properties}
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.util.{Failure, Success, Try}

class CleanCreateOrderImplTest extends AnyFlatSpec with Matchers with MockFactory {
  private val IS_VIP = Try(true)
  private val IS_NOT_VIP = Try(false)
  private val FREE_DELIVERY = true
  private val NO_FREE_DELIVERY = false
  private val LIMIT_500 = 500
  private def creditLimit(limit: Int) = Try(Credit(limit))
  private val SERVICE_DOWN_EXCEPTION = new RuntimeException("The service is down")
  private val FREE_LIMIT = 100
  private val ORDER = Helpers.createOrder(orderLines = Seq(
    Helpers.createOrderLine(quantity = 1, cost = FREE_LIMIT - 1)
  ))

  private val customerDao = mock[CustomerDao[Try]]

  private val orderDao = mock[OrderDao[Try]]

  private val creditDao = mock[CreditDao[Try]]

  private val properties = Properties(FREE_LIMIT)

  private val CreateOrder = new CreateOrderImpl[Try](orderDao, customerDao, creditDao, properties)

  it should "create order - happy flow " in {
    (customerDao.isVip _).expects(ORDER.customer).returning(IS_NOT_VIP)

    (creditDao.creditLimit _).expects(ORDER.customer).returning(creditLimit(LIMIT_500))

    val orderWithId = ORDER.copy(orderId = Some("someOrderId"))
    (orderDao.createOrder _).expects(NO_FREE_DELIVERY, ORDER).returning(Try(orderWithId))

    CreateOrder(ORDER) shouldBe Success(Right(orderWithId))
  }

  it should "create an order for an ordinary customer" in {
    (customerDao.isVip _).expects(*).returning(IS_NOT_VIP)

    (creditDao.creditLimit _).expects(*).returning(creditLimit(LIMIT_500))

    val orderWithId = ORDER.copy(orderId = Some("someOrderId"))
    (orderDao.createOrder _).expects(NO_FREE_DELIVERY, *).returning(Try(orderWithId))

    CreateOrder(ORDER) shouldBe Success(Right(orderWithId))
  }

  it should "ship the order for free, if the customer is a VIP" in {
    (customerDao.isVip _).expects(*).returning(IS_VIP)

    (creditDao.creditLimit _).expects(*).returning(creditLimit(LIMIT_500))

    val orderWithId = ORDER.copy(orderId = Some("someOrderId"))
    (orderDao.createOrder _).expects(FREE_DELIVERY, *).returning(Try(orderWithId))

    CreateOrder(ORDER) shouldBe Success(Right(orderWithId))
  }

  it should "ship the order for free, if we can't determine if the customer is VIP or not" in {
    (customerDao.isVip _).expects(*).returning(Try(throw SERVICE_DOWN_EXCEPTION))

    (creditDao.creditLimit _).expects(*).returning(creditLimit(LIMIT_500))

    val orderWithId = ORDER.copy(orderId = Some("someOrderId"))
    (orderDao.createOrder _).expects(FREE_DELIVERY, *).returning(Try(orderWithId))

    CreateOrder(ORDER) shouldBe Success(Right(orderWithId))
  }

  it should "ship the order for free, if the cost is above the free shipping limit" in {
    val order = Helpers.createOrder(orderLines = Seq(
      Helpers.createOrderLine(quantity = 1, cost = FREE_LIMIT)
    ))

    (customerDao.isVip _).expects(*).returning(IS_NOT_VIP)

    (creditDao.creditLimit _).expects(*).returning(creditLimit(LIMIT_500))

    val orderWithId = order.copy(orderId = Some("someOrderId"))
    (orderDao.createOrder _).expects(FREE_DELIVERY, *).returning(Try(orderWithId))

    CreateOrder(order) shouldBe Success(Right(orderWithId))
  }

  it should "create an order if the cost is below the credit limit" in {
    val order = Helpers.createOrder(orderLines = Seq(
      Helpers.createOrderLine(quantity = 1, cost = LIMIT_500 / 2),
      Helpers.createOrderLine(quantity = 1, cost = LIMIT_500 / 2),
    ))

    (customerDao.isVip _).expects(*).returning(IS_NOT_VIP)

    (creditDao.creditLimit _).expects(*).returning(creditLimit(LIMIT_500))

    val orderWithId = order.copy(orderId = Some("someOrderId"))
    (orderDao.createOrder _).expects(*, *).returning(Try(orderWithId))

    CreateOrder(order) shouldBe Success(Right(orderWithId))
  }

  it should "reject an order if the cost is above the credit limit" in {
    val order = Helpers.createOrder(orderLines = Seq(
      Helpers.createOrderLine(quantity = 1, cost = LIMIT_500 + 1)
    ))

    (customerDao.isVip _).expects(*).returning(IS_NOT_VIP)

    (creditDao.creditLimit _).expects(*).returning(creditLimit(LIMIT_500))

    CreateOrder(order) shouldBe Success(Left(CreditLimitExceeded()))
  }

  it should "fail to create the order if the credit service is down" in {
    (customerDao.isVip _).expects(*).returning(IS_NOT_VIP)

    (creditDao.creditLimit _).expects(*).returning(Try(throw SERVICE_DOWN_EXCEPTION))

    CreateOrder(ORDER) shouldBe Failure(SERVICE_DOWN_EXCEPTION)
  }

  it should "fail if the order service is down" in {
    (customerDao.isVip _).expects(*).returning(IS_NOT_VIP)

    (creditDao.creditLimit _).expects(*).returning(creditLimit(LIMIT_500))

    (orderDao.createOrder _).expects(*, *).returning(Try(throw SERVICE_DOWN_EXCEPTION))

    CreateOrder(ORDER) shouldBe Failure(SERVICE_DOWN_EXCEPTION)
  }

}
