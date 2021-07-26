package com.github.morotsman.about_test

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import cats.implicits._
import org.scalamock.scalatest.MockFactory

import scala.util.{Failure, Success, Try}


class UncleanCreateOrderImplTest extends AnyFlatSpec with Matchers with MockFactory {

  private val customerDao = mock[CustomerDao[Try]]

  private val orderDao = mock[OrderDao[Try]]

  private val creditDao = mock[CreditDao[Try]]

  private val CreateOrder = new CreateOrderImpl[Try](orderDao, customerDao, creditDao)

  it should "create an order for a VIP customer, the shipping should be free" in {
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

    (customerDao.isVip _).expects(Customer(
      customerId = "Some id",
      firstName = "John",
      lastName = "Doe"
    )).returning(Try(true))

    (creditDao.creditLimit _).expects(Customer(
      customerId = "Some id",
      firstName = "John",
      lastName = "Doe"
    )).returning(Try(Credit(500L)))

    (orderDao.createOrder _).expects(true, order).returning(Try(order.copy(orderId = Some("someOrderId"))))

    val result: Try[Either[BusinessError, Order]] = CreateOrder(order)

    result shouldBe Success(Right(order.copy(orderId = Some("someOrderId"))))
  }

  it should "if we temporarily don't know if the customer is vip or not, the shipping should be free" in {
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

    (customerDao.isVip _).expects(Customer(
      customerId = "Some id",
      firstName = "John",
      lastName = "Doe"
    )).returning(Try(throw new RuntimeException("vip service is down")))

    (creditDao.creditLimit _).expects(Customer(
      customerId = "Some id",
      firstName = "John",
      lastName = "Doe"
    )).returning(Try(Credit(500L)))

    (orderDao.createOrder _).expects(true, order).returning(Try(order.copy(orderId = Some("someOrderId"))))

    val result: Try[Either[BusinessError, Order]] = CreateOrder(order)

    result shouldBe Success(Right(order.copy(orderId = Some("someOrderId"))))
  }

  it should "create an order for a non VIP customer, the shipping should not be free" in {
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

    (customerDao.isVip _).expects(Customer(
      customerId = "Some id",
      firstName = "John",
      lastName = "Doe"
    )).returning(Try(false))

    (creditDao.creditLimit _).expects(Customer(
      customerId = "Some id",
      firstName = "John",
      lastName = "Doe"
    )).returning(Try(Credit(500L)))

    (orderDao.createOrder _).expects(false, order).returning(Try(order.copy(orderId = Some("someOrderId"))))

    val result: Try[Either[BusinessError, Order]] = CreateOrder(order)

    result shouldBe Success(Right(order.copy(orderId = Some("someOrderId"))))
  }

  it should "create an order if the cost is below the credit limit" in {
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
          cost = 250
        )
      )
    )

    (customerDao.isVip _).expects(Customer(
      customerId = "Some id",
      firstName = "John",
      lastName = "Doe"
    )).returning(Try(false))

    (creditDao.creditLimit _).expects(Customer(
      customerId = "Some id",
      firstName = "John",
      lastName = "Doe"
    )).returning(Try(Credit(500L)))

    (orderDao.createOrder _).expects(true, order).returning(Try(order.copy(orderId = Some("someOrderId"))))

    val result: Try[Either[BusinessError, Order]] = CreateOrder(order)

    result shouldBe Success(Right(order.copy(orderId = Some("someOrderId"))))
  }

  it should "reject an order if the cost is above the credit limit" in {
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
          quantity = 1,
          cost = 501
        )
      )
    )

    (customerDao.isVip _).expects(Customer(
      customerId = "Some id",
      firstName = "John",
      lastName = "Doe"
    )).returning(Try(false))

    (creditDao.creditLimit _).expects(Customer(
      customerId = "Some id",
      firstName = "John",
      lastName = "Doe"
    )).returning(Try(Credit(500L)))

    val result: Try[Either[BusinessError, Order]] = CreateOrder(order)

    result shouldBe Success(Left(CreditLimitExceeded()))
  }

  it should "shipping should be free if above free limit" in {
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
          cost = 250
        )
      )
    )

    (customerDao.isVip _).expects(Customer(
      customerId = "Some id",
      firstName = "John",
      lastName = "Doe"
    )).returning(Try(false))

    (creditDao.creditLimit _).expects(Customer(
      customerId = "Some id",
      firstName = "John",
      lastName = "Doe"
    )).returning(Try(Credit(500L)))

    (orderDao.createOrder _).expects(true, order).returning(Try(order.copy(orderId = Some("someOrderId"))))

    val result: Try[Either[BusinessError, Order]] = CreateOrder(order)

    result shouldBe Success(Right(order.copy(orderId = Some("someOrderId"))))
  }

  it should "fail to create the order if the credit service is down" in {
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
          cost = 250
        )
      )
    )

    (customerDao.isVip _).expects(Customer(
      customerId = "Some id",
      firstName = "John",
      lastName = "Doe"
    )).returning(Try(false))

    val exception = new RuntimeException("The credit service is down")
    (creditDao.creditLimit _).expects(Customer(
      customerId = "Some id",
      firstName = "John",
      lastName = "Doe"
    )).returning(Try(throw exception))

    val result: Try[Either[BusinessError, Order]] = CreateOrder(order)

    result shouldBe Failure(exception)
  }

  it should "fail if the order service is down" in {
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
          cost = 250
        )
      )
    )

    (customerDao.isVip _).expects(Customer(
      customerId = "Some id",
      firstName = "John",
      lastName = "Doe"
    )).returning(Try(false))

    (creditDao.creditLimit _).expects(Customer(
      customerId = "Some id",
      firstName = "John",
      lastName = "Doe"
    )).returning(Try(Credit(500L)))

    val exception = new RuntimeException("The order service is down")
    (orderDao.createOrder _).expects(true, order).returning(Try(throw exception))

    val result: Try[Either[BusinessError, Order]] = CreateOrder(order)

    result shouldBe Failure(exception)
  }

}
