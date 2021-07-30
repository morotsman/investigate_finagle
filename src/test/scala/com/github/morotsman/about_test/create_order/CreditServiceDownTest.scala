package com.github.morotsman.about_test.create_order

import cats.implicits._
import com.github.morotsman.about_test._
import com.github.morotsman.about_test.create_order.Constants._
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.util.{Failure, Success, Try}

class CreditServiceDownTest extends AnyFlatSpec with Matchers with MockFactory {
  private def creditLimit(limit: Int) = Try(Credit(limit))
  private val ORDER = OrderHelper.createOrder(orderLines = Seq(
    OrderHelper.createOrderLine(quantity = 1, cost = FREE_LIMIT - 1)
  ))

  private val customerDao = mock[CustomerDao[Try]]

  private val orderDao = mock[OrderDao[Try]]

  private val creditDao = mock[CreditDao[Try]]

  private val properties = Properties(FREE_LIMIT)

  private val CreateOrder = new CreateOrderImpl[Try](orderDao, customerDao, creditDao, properties)

  it should "fail if the order service is down" in {
    (customerDao.isVip _).expects(*).returning(IS_NOT_VIP)

    (creditDao.creditLimit _).expects(*).returning(creditLimit(LIMIT_500))

    (orderDao.createOrder _).expects(*, *).returning(Try(throw SERVICE_DOWN_EXCEPTION))

    CreateOrder(ORDER) shouldBe Failure(SERVICE_DOWN_EXCEPTION)
  }

}