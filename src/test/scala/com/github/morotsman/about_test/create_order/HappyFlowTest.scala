package com.github.morotsman.about_test.create_order

import cats.implicits._
import com.github.morotsman.about_test._
import com.github.morotsman.about_test.create_order.Constants._
import com.github.morotsman.about_test.create_order.Helpers._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.util.{Success, Try}

class HappyFlowTest extends AnyFlatSpec with Matchers with Mocks {
  private val CreateOrder = new CreateOrderImpl[Try](orderDao, customerDao, creditDao, Properties(FREE_LIMIT))

  it should "create order - happy flow " in {
    val order = createOrder()

    (customerDao.isVip _).expects(order.customer).returning(IS_NOT_VIP)

    (creditDao.creditLimit _).expects(order.customer).returning(creditLimit(LIMIT_500))

    val orderWithId = order.copy(orderId = Some("someOrderId"))
    (orderDao.createOrder _).expects(NO_FREE_DELIVERY, order).returning(Try(orderWithId))

    CreateOrder(order) shouldBe Success(Right(orderWithId))
  }

}
