package com.github.morotsman.about_test

object OrderHelper {

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
                       quantity: Int = 2,
                       cost: Int = 20
                     ): OrderLine = OrderLine(
    itemCode = itemCode,
    quantity = quantity,
    cost = cost
  )

}
