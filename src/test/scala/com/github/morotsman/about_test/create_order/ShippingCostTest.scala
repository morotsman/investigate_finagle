package com.github.morotsman.about_test.create_order

import cats.implicits._
import com.github.morotsman.about_test._
import com.github.morotsman.about_test.create_order.Constants._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.util.{Success, Try}

class ShippingCostTest extends AnyFlatSpec with Matchers with Mocks {
  private def creditLimit(limit: Int) = Try(Credit(limit))
  private val ORDER = OrderHelper.createOrder(orderLines = Seq(
    OrderHelper.createOrderLine(quantity = 1, cost = FREE_LIMIT - 1)
  ))

  private val properties = Properties(FREE_LIMIT)

  private val CreateOrder = new CreateOrderImpl[Try](orderDao, customerDao, creditDao, properties)

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
    val order = OrderHelper.createOrder(orderLines = Seq(
      OrderHelper.createOrderLine(quantity = 1, cost = FREE_LIMIT)
    ))

    (customerDao.isVip _).expects(*).returning(IS_NOT_VIP)

    (creditDao.creditLimit _).expects(*).returning(creditLimit(LIMIT_500))

    val orderWithId = order.copy(orderId = Some("someOrderId"))
    (orderDao.createOrder _).expects(FREE_DELIVERY, *).returning(Try(orderWithId))

    CreateOrder(order) shouldBe Success(Right(orderWithId))
  }

}
