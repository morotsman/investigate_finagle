package com.github.morotsman.about_test.dao

import com.github.morotsman.about_test.{Order, OrderDao}

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class OrderDaoImpl(implicit ec: ExecutionContext) extends OrderDao[Future] {

  override def createOrder(freeDelivery: Boolean, order: Order): Future[Order] = {
    Future {
      println("createOrder")
      Thread.sleep(2000)
      println("createOrder completed")
      order.copy(orderId = Some(UUID.randomUUID().toString))
    }
  }

}
