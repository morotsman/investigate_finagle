package com.github.morotsman.about_test.create_order

import com.github.morotsman.about_test.{CreditDao, CustomerDao, OrderDao}
import org.scalamock.scalatest.MockFactory

import scala.util.Try

trait Mocks extends MockFactory{

  val customerDao = mock[CustomerDao[Try]]

  val orderDao = mock[OrderDao[Try]]

  val creditDao = mock[CreditDao[Try]]

}
