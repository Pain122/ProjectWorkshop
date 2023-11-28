package controller

import domain._
import domain.errors._
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._

object endpoints {

  val listTodos: PublicEndpoint[RequestContext, AppError, List[Todo], Any] =
    endpoint.get
      .in("todos")
      .in(header[RequestContext]("X-Request-Id"))
      .errorOut(jsonBody[AppError])
      .out(jsonBody[List[Todo]])

  val findTodoById
      : PublicEndpoint[(TodoId, RequestContext), AppError, Option[Todo], Any] =
    endpoint.get
      .in("todo" / path[TodoId])
      .in(header[RequestContext]("X-Request-Id"))
      .errorOut(jsonBody[AppError])
      .out(jsonBody[Option[Todo]])

  val removeTodo
      : PublicEndpoint[(TodoId, RequestContext), AppError, Unit, Any] = {
    endpoint.delete
      .in("todo" / path[TodoId])
      .in(header[RequestContext]("X-Request-Id"))
      .errorOut(jsonBody[AppError])
  }

  val createTodo
      : PublicEndpoint[(RequestContext, CreateTodo), AppError, Todo, Any] =
    endpoint.post
      .in("todo")
      .in(header[RequestContext]("X-Request-Id"))
      .in(jsonBody[CreateTodo])
      .errorOut(jsonBody[AppError])
      .out(jsonBody[Todo])
}
