package com.github.morotsman.about_test.create_order

import cats.implicits._
import com.github.morotsman.about_test._
import com.github.morotsman.about_test.create_order.Constants._
import com.github.morotsman.about_test.create_order.Helpers.creditLimit
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.util.{Success, Try}

class HappyFlowTest extends AnyFlatSpec with Matchers with Mocks {
  private val CreateOrder = new CreateOrderImpl[Try](orderDao, customerDao, creditDao, Properties(FREE_LIMIT))

  it should "create order - happy flow " in {
    (customerDao.isVip _).expects(HAPPY_FLOW_ORDER.customer).returning(IS_NOT_VIP)

    (creditDao.creditLimit _).expects(HAPPY_FLOW_ORDER.customer).returning(creditLimit(LIMIT_500))

    val orderWithId = HAPPY_FLOW_ORDER.copy(orderId = Some("someOrderId"))
    (orderDao.createOrder _).expects(NO_FREE_DELIVERY, HAPPY_FLOW_ORDER).returning(Try(orderWithId))

    CreateOrder(HAPPY_FLOW_ORDER) shouldBe Success(Right(orderWithId))
  }

}
