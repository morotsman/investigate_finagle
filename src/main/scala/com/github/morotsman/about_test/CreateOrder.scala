package com.github.morotsman.about_test

import cats._
import cats.implicits._
import scala.language.higherKinds

trait CreateOrder[F[_]] {
  val freeLimit = 1000L

  def apply(order: Order): F[Either[BusinessError, Order]]
}

class CreateOrderImpl[F[_]](
                             orderDao: OrderDao[F],
                             customerDao: CustomerDao[F],
                             creditDao: CreditDao[F]
                           )(implicit F: MonadError[F, Throwable]) extends CreateOrder[F] {
  override def apply(order: Order): F[Either[BusinessError, Order]] = for {
    vipAndCredit <- Apply[F].map2(
      checkIfVip(order.customer),
      creditDao.creditLimit(order.customer)
    )((_, _))
    totalCost = costForOrder(order)
    isVip = vipAndCredit._1
    credit = vipAndCredit._2
    o <- if (totalCost < credit.limit) {
      orderDao.createOrder(
        freeDelivery = isVip || totalCost >= freeLimit,
        order
      ).map(Either.right(_))
    } else {
      F.pure(Either.left(CreditLimitExceeded()))
    }
  } yield o

  private def checkIfVip(customer: Customer): F[Boolean] = {
    F.redeemWith(customerDao.isVip(customer))(
      recover = _ => F.pure(true),
      bind = a => F.pure(a)
    )
  }

  private def costForOrder(order: Order): Long =
    order.orderLines.map(l => l.quantity * l.cost).sum

}

trait OrderDao[F[_]] {
  def createOrder(freeDelivery: Boolean, order: Order): F[Order]
}

trait CustomerDao[F[_]] {
  def isVip(c: Customer): F[Boolean]
}

trait CreditDao[F[_]] {
  def creditLimit(c: Customer): F[Credit]
}

sealed trait BusinessError {
  val message: String
}

case class CreditLimitExceeded() extends BusinessError {
  override val message: String = "Credit limit exceeded."
}

case class Order(orderId: Option[String], customer: Customer, address: Address, orderLines: Seq[OrderLine])

case class Address(street: String, zipCode: String, city: String, country: String)

case class OrderLine(itemCode: String, quantity: Long, cost: Long)

case class Customer(customerId: String, firstName: String, lastName: String)

case class Credit(limit: Long)