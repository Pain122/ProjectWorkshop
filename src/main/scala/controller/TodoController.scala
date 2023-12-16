package controller

import cats.Functor
import cats.data.ReaderT
import cats.effect.IO
import domain.errors.AppError
import service.TodoStorage
import sttp.tapir.server.ServerEndpoint
import cats.implicits.toFunctorOps
import domain.RequestContext
//import tofu.syntax.feither._

trait TodoController[F[_]] {
  def listAllTodos: ServerEndpoint[Any, F]
  def findTodoById: ServerEndpoint[Any, F]
  def removeTodoById: ServerEndpoint[Any, F]
  def createTodo: ServerEndpoint[Any, F]

  def all: List[ServerEndpoint[Any, F]]
}

object TodoController {
  final private class Impl(storage: TodoStorage[ReaderT[IO, RequestContext, *]]) extends TodoController[IO] {

    override val listAllTodos: ServerEndpoint[Any, IO] =
      endpoints.listTodos.serverLogic(ctx =>
        storage.list.map(_.left.map[AppError](identity)).run(ctx)
//          storage.list.leftMapIn(identity[AppError])
      )

    override val findTodoById: ServerEndpoint[Any, IO] =
      endpoints.findTodoById.serverLogic { case (todoId, ctx) =>
        storage.findById(todoId).map(_.left.map[AppError](identity)).run(ctx)
      }

    override val removeTodoById: ServerEndpoint[Any, IO] =
      endpoints.removeTodo.serverLogic { case (todoId, ctx) =>
        storage.removeById(todoId).run(ctx)
      }

    override val createTodo: ServerEndpoint[Any, IO] =
      endpoints.createTodo.serverLogic { case (context, todo) =>
        storage.create(todo).run(context)
      }

    override val all: List[ServerEndpoint[Any, IO]] =
      List(listAllTodos, findTodoById, removeTodoById, createTodo)
  }

  def make(storage: TodoStorage[ReaderT[IO, RequestContext, *]]): TodoController[IO] = new Impl(storage)
}
