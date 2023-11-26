package controller

import domain._
import domain.errors._
import sttp.tapir._

object endpoints {
  val listTodos: PublicEndpoint[RequestContext, AppError, List[Todo], Any] = ???

  val findTodoById
      : PublicEndpoint[(TodoId, RequestContext), AppError, Option[Todo], Any] = ???

  val removeTodo
      : PublicEndpoint[(TodoId, RequestContext), AppError, Unit, Any] = ???

  val createTodo
      : PublicEndpoint[(RequestContext, CreateTodo), AppError, Todo, Any] = ???
}
