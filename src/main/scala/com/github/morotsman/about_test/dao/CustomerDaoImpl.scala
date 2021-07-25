package com.github.morotsman.about_test.dao

import com.github.morotsman.about_test.{Customer, CustomerDao}

import scala.concurrent.{ExecutionContext, Future}

class CustomerDaoImpl(implicit ec: ExecutionContext) extends CustomerDao[Future] {
  override def isVip(c: Customer): Future[Boolean] = {
    Future {
      println("isVip")
      Thread.sleep(4000)
      println("isVip completed")
      true
    }
  }
}
