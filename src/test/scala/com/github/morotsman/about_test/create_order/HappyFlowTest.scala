package com.github.morotsman.about_test.create_order

import cats.implicits._
import com.github.morotsman.about_test._
import com.github.morotsman.about_test.create_order.Constants._
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.util.{Success, Try}

class HappyFlowTest extends AnyFlatSpec with Matchers with MockFactory {
  private def creditLimit(limit: Int) = Try(Credit(limit))
  private val ORDER = OrderHelper.createOrder(orderLines = Seq(
    OrderHelper.createOrderLine(quantity = 1, cost = FREE_LIMIT - 1)
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

}
