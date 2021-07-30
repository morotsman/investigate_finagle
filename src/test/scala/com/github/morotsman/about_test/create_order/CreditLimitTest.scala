package com.github.morotsman.about_test.create_order

import cats.implicits._
import com.github.morotsman.about_test._
import com.github.morotsman.about_test.create_order.Constants._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.util.{Success, Try}

class CreditLimitTest extends AnyFlatSpec with Matchers with Mocks {
  private def creditLimit(limit: Int) = Try(Credit(limit))

  private val properties = Properties(FREE_LIMIT)

  private val CreateOrder = new CreateOrderImpl[Try](orderDao, customerDao, creditDao, properties)

  it should "create an order if the cost is below the credit limit" in {
    val order = OrderHelper.createOrder(orderLines = Seq(
      OrderHelper.createOrderLine(quantity = 1, cost = LIMIT_500)
    ))

    (customerDao.isVip _).expects(*).returning(IS_NOT_VIP)

    (creditDao.creditLimit _).expects(*).returning(creditLimit(LIMIT_500))

    val orderWithId = order.copy(orderId = Some("someOrderId"))
    (orderDao.createOrder _).expects(*, *).returning(Try(orderWithId))

    CreateOrder(order) shouldBe Success(Right(orderWithId))
  }

  it should "reject an order if the cost is above the credit limit" in {
    val order = OrderHelper.createOrder(orderLines = Seq(
      OrderHelper.createOrderLine(quantity = 1, cost = LIMIT_500 + 1)
    ))

    (customerDao.isVip _).expects(*).returning(IS_NOT_VIP)

    (creditDao.creditLimit _).expects(*).returning(creditLimit(LIMIT_500))

    CreateOrder(order) shouldBe Success(Left(CreditLimitExceeded()))
  }

}
