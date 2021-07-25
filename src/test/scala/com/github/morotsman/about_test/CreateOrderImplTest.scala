package com.github.morotsman.about_test

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import cats._
import cats.implicits._
import scala.util.Try


class CreateOrderImplTest extends AnyFlatSpec with Matchers {

  class TestCustomerDao() extends CustomerDao[Try] {
    override def isVip(c: Customer): Try[Boolean] = ???
  }

  class TestOrderDao() extends OrderDao[Try] {
    override def createOrder(freeDelivery: Boolean, order: Order): Try[Order] = ???
  }

  class TestCreditDao() extends CreditDao[Try] {
    override def creditLimit(c: Customer): Try[Credit] = ???
  }

  val customerDao = new TestCustomerDao()

  val orderDao = new TestOrderDao()

  val creditDao = new TestCreditDao()

  val CreateOrder = new CreateOrderImpl[Try](orderDao, customerDao, creditDao)


  it should "create an order" in {
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
  }

}
