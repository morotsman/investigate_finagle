package com.github.morotsman.investigate_finagle_service.todo_airframe


import wvlet.airframe.surface.required

case class Todo(id: Option[Long], @required title: String, @required completed: Boolean)



