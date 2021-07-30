package com.github.morotsman.about_test.create_order

import com.github.morotsman.about_test.{CreditDao, CustomerDao, OrderDao}
import org.scalamock.scalatest.MockFactory

import scala.util.Try

trait Mocks extends MockFactory{

  val customerDao: CustomerDao[Try] = mock[CustomerDao[Try]]

  val orderDao: OrderDao[Try] = mock[OrderDao[Try]]

  val creditDao: CreditDao[Try] = mock[CreditDao[Try]]

}
