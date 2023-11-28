package controller

import cats.effect.IO
import domain.errors.AppError
import service.TodoStorage
import sttp.tapir.server.ServerEndpoint
//import tofu.syntax.feither._

trait TodoController {
  def listAllTodos: ServerEndpoint[Any, IO]
  def findTodoById: ServerEndpoint[Any, IO]
  def removeTodoById: ServerEndpoint[Any, IO]
  def createTodo: ServerEndpoint[Any, IO]

  def all: List[ServerEndpoint[Any, IO]]
}

object TodoController {
  final private class Impl(storage: TodoStorage) extends TodoController {

    override val listAllTodos: ServerEndpoint[Any, IO] =
      endpoints.listTodos.serverLogic(ctx =>
        storage.list.map(_.left.map[AppError](identity))
//          storage.list.leftMapIn(identity[AppError])
      )

    override val findTodoById: ServerEndpoint[Any, IO] =
      endpoints.findTodoById.serverLogic { case (todoId, ctx) =>
        storage.findById(todoId).map(_.left.map[AppError](identity))
      }

    override val removeTodoById: ServerEndpoint[Any, IO] =
      endpoints.removeTodo.serverLogic { case (todoId, ctx) =>
        storage.removeById(todoId)
      }

    override val createTodo: ServerEndpoint[Any, IO] =
      endpoints.createTodo.serverLogic { case (context, todo) =>
        storage.create(todo)
      }

    override val all: List[ServerEndpoint[Any, IO]] =
      List(listAllTodos, findTodoById, removeTodoById, createTodo)
  }

  def make(storage: TodoStorage): TodoController = new Impl(storage)
}
