package com.github.morotsman.about_test.create_order

import scala.util.Try

object Constants {
  val IS_VIP: Try[Boolean] = Try(true)
  val IS_NOT_VIP: Try[Boolean] = Try(false)
  val FREE_DELIVERY: Boolean = true
  val NO_FREE_DELIVERY: Boolean = false
  val LIMIT_500: Int = 500
  val FREE_SHIPPING_LIMIT: Int = 100
  val SERVICE_DOWN_EXCEPTION: RuntimeException = new RuntimeException("The service is down")
}
