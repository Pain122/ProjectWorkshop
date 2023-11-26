package controller

import cats.effect.IO
import service.TodoStorage
import sttp.tapir.server.ServerEndpoint

trait TodoController {
  def listAllTodos: ServerEndpoint[Any, IO]
  def findTodoById: ServerEndpoint[Any, IO]
  def removeTodoById: ServerEndpoint[Any, IO]
  def createTodo: ServerEndpoint[Any, IO]

  def all: List[ServerEndpoint[Any, IO]]
}

object TodoController {
  final private class Impl extends TodoController {

    override val listAllTodos: ServerEndpoint[Any, IO] = ???

    override val findTodoById: ServerEndpoint[Any, IO] = ???

    override val removeTodoById: ServerEndpoint[Any, IO] = ???

    override val createTodo: ServerEndpoint[Any, IO] = ???

    override val all: List[ServerEndpoint[Any, IO]] = ???
  }

  def make(storage: TodoStorage[IO]): TodoController = new Impl
}
