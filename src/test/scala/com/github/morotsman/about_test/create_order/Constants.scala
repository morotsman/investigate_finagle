package com.github.morotsman.about_test.create_order

import scala.util.Try

object Constants {
  val IS_VIP = Try(true)
  val IS_NOT_VIP = Try(false)
  val FREE_DELIVERY = true
  val NO_FREE_DELIVERY = false
  val LIMIT_500 = 500
  val FREE_LIMIT = 100
  val SERVICE_DOWN_EXCEPTION = new RuntimeException("The service is down")
  val HAPPY_FLOW_ORDER = Helpers.createOrder()
}
