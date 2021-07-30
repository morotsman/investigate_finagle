package com.github.morotsman.about_test.create_order

import com.github.morotsman.about_test.create_order.Constants.FREE_SHIPPING_LIMIT
import com.github.morotsman.about_test.{Address, Credit, Customer, Order, OrderLine}

import scala.util.Try

object Helpers {

  def createOrder(
                   orderId: Option[String] = None,
                   customer: Customer = createCustomer(),
                   address: Address = createAddress(),
                   orderLines: Seq[OrderLine] = Seq(createOrderLine())
                 ): Order = Order(
    orderId = orderId,
    customer = customer,
    address = address,
    orderLines = orderLines
  )

  def createCustomer(
                      customerId: String = "Some id",
                      firstName: String = "John",
                      lastName: String = "Doe"
                    ): Customer = Customer(
    customerId = customerId,
    firstName = firstName,
    lastName = lastName
  )

  def createAddress(
                     street: String = "Some street 42",
                     zipCode: String = "243221",
                     city: String = "Malmoe",
                     country: String = "Sweden"
                   ): Address = Address(
    street = street,
    zipCode = zipCode,
    city = city,
    country = country
  )

  def createOrderLine(
                       itemCode: String = "1",
                       quantity: Int = 1,
                       cost: Int = FREE_SHIPPING_LIMIT - 1
                     ): OrderLine = OrderLine(
    itemCode = itemCode,
    quantity = quantity,
    cost = cost
  )

  def creditLimit(limit: Int): Try[Credit] = Try(Credit(limit))

}
