package com.github.morotsman.about_test.dao

import com.github.morotsman.about_test.{Credit, CreditDao, Customer}

import scala.concurrent.{ExecutionContext, Future}

class CreditDaoImpl(implicit ec: ExecutionContext) extends CreditDao[Future] {
  override def creditLimit(c: Customer): Future[Credit] = Future {
    println("creditLimit")
    Thread.sleep(4000)
    println("creditLimit completed")
    Credit(500)
  }
}
