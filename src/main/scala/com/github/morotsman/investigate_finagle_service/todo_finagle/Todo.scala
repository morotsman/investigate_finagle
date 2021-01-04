package com.github.morotsman.investigate_finagle_service.todo_finagle

import com.twitter.bijection.Injection
import com.twitter.bijection.json.JsonInjection
import com.twitter.bijection.json.JsonNodeInjection.{fromJsonNode, toJsonNode}
import org.codehaus.jackson.JsonNode

import scala.util.Try

case class Todo(id: Option[Long], title: String, completed: Boolean)


object Todo {
  implicit val todoBuilder: Injection[Todo, Map[String, JsonNode]] =
    Injection.build[Todo, Map[String, JsonNode]](todoToMap)(mapToTodo)

  private def mapToTodo(m: Map[String, JsonNode]): Try[Todo] =
    Try(Todo(
      m.get("id").map(id => fromJsonNode[Long](id).get),
      fromJsonNode[String](m("title")).get,
      fromJsonNode[Boolean](m("completed")).get
    ))

  private def todoToMap(t: Todo): Map[String, JsonNode] =
    if (t.id.isDefined) {
      Map("id" -> toJsonNode(t.id.get), "title" -> toJsonNode(t.title), "completed" -> toJsonNode(t.completed))
    } else {
      Map("title" -> toJsonNode(t.title), "completed" -> toJsonNode(t.completed))
    }

  implicit val mapToString: Injection[Map[String, JsonNode], String] =
    JsonInjection.toString[Map[String, JsonNode]]

  implicit val todoToJsonString: Injection[Todo, String] =
    Injection.connect[Todo, Map[String, JsonNode], String](todoBuilder, mapToString)

  def trysToTry[A](ts: List[Try[A]]): Try[List[A]] = Try(ts.map(t => t.get))

  implicit val todosBuilder: Injection[List[Todo], List[Map[String, JsonNode]]] =
    Injection.build[List[Todo], List[Map[String, JsonNode]]](ts => ts.map(todoToMap))(ts => trysToTry(ts.map(mapToTodo)))

  implicit val listToString: Injection[List[Map[String, JsonNode]], String] =
    JsonInjection.toString[List[Map[String, JsonNode]]]

  implicit val todosToJsonString: Injection[List[Todo], String] =
    Injection.connect[List[Todo], List[Map[String, JsonNode]], String](todosBuilder, listToString)

}
