package com.github.morotsman.about_test.create_order

import cats.implicits._
import com.github.morotsman.about_test._
import com.github.morotsman.about_test.create_order.Constants._
import com.github.morotsman.about_test.create_order.Helpers.creditLimit
import org.scalatest.OneInstancePerTest
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.util.{Failure, Try}

class OrderServiceDownTest extends AnyFlatSpec with Matchers with Mocks with OneInstancePerTest {
  private val CreateOrder = new CreateOrderImpl[Try](orderDao, customerDao, creditDao, Properties(FREE_LIMIT))

  (customerDao.isVip _).expects(*).returning(IS_NOT_VIP)

  (creditDao.creditLimit _).expects(*).returning(creditLimit(LIMIT_500))

  it should "fail if the order service is down" in {
    (orderDao.createOrder _).expects(*, *).returning(Try(throw SERVICE_DOWN_EXCEPTION))

    CreateOrder(HAPPY_FLOW_ORDER) shouldBe Failure(SERVICE_DOWN_EXCEPTION)
  }

}
