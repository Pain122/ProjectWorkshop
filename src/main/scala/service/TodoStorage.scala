package service

import cats.effect.kernel.MonadCancelThrow
import cats.implicits.{catsSyntaxApply, toFunctorOps}
import cats.syntax.applicativeError._
import cats.syntax.either._
import cats.syntax.flatMap._
import cats.{FlatMap, Id}
import dao.TodoSql
import domain._
import domain.errors._
import doobie._
import doobie.implicits._
import tofu.WithContext
import tofu.logging.Logging
import tofu.logging.Logging.Make
import tofu.syntax.logging._

trait TodoStorage[F[_]] {
  def list: F[Either[InternalError, List[Todo]]]
  def findById(
      id: TodoId
  ): F[Either[InternalError, Option[Todo]]]
  def removeById(id: TodoId): F[Either[AppError, Unit]]
  def create(todo: CreateTodo): F[Either[AppError, Todo]]
}

object TodoStorage {
  def make[F[_]: MonadCancelThrow](
      todoSql: TodoSql,
      transactor: Transactor[F]
  ): TodoStorage[F] =
    new TodoStorage[F] {
      override def list: F[Either[InternalError, List[Todo]]] = todoSql.listAll
        .transact(transactor)
        .attempt
        .map(_.leftMap(InternalError.apply))

      override def findById(
          id: TodoId
      ): F[Either[InternalError, Option[Todo]]] = todoSql
        .findById(id)
        .transact(transactor)
        .attempt
        .map(_.leftMap(InternalError.apply))

      override def removeById(id: TodoId): F[Either[AppError, Unit]] =
        todoSql.removeById(id).transact(transactor).attempt.map {
          case Left(th)           => InternalError(th).asLeft
          case Right(Left(error)) => error.asLeft
          case _                  => ().asRight
        }

      override def create(todo: CreateTodo): F[Either[AppError, Todo]] =
        todoSql.create(todo).transact(transactor).attempt.map {
          case Left(th)           => InternalError(th).asLeft
          case Right(Left(error)) => error.asLeft
          case Right(Right(todo)) => todo.asRight
        }
    }

  private final class LoggingImpl[F[_]: FlatMap](storage: TodoStorage[F])(
      implicit logging: Logging[F]
  ) extends TodoStorage[F] {
    private def surroundWithLogs[Error, Res](
        io: F[Either[Error, Res]]
    )(
        inputLog: String
    )(errorOutputLog: Error => (String, Option[Throwable]))(
        successOutputLog: Res => String
    ): F[Either[Error, Res]] = {
      info"$inputLog" *> io.flatTap {
        case Left(error) =>
          val (logString: String, throwable: Option[Throwable]) =
            errorOutputLog(error)
          throwable.fold(error"$logString")(err => errorCause"$logString" (err))
        case Right(success) => info"${successOutputLog(success)}"
      }
    }

    override def list: F[Either[InternalError, List[Todo]]] =
      surroundWithLogs(storage.list)("Getting all todos")(err =>
        ("Error while getting all todos", Some(err.cause0))
      )(s => s"Got all todos $s")

    override def findById(id: TodoId): F[Either[InternalError, Option[Todo]]] =
      surroundWithLogs(storage.findById(id))("Finding todo by id")(err =>
        (s"Failed to find todo by id${err.message}", Some(err.cause0))
      )(success => s"Found todo: $success")

    override def removeById(id: TodoId): F[Either[AppError, Unit]] =
      surroundWithLogs(storage.removeById(id))("removing todo")(err =>
        (err.message, err.cause)
      )(_ => "Delete successful")

    override def create(todo: CreateTodo): F[Either[AppError, Todo]] =
      surroundWithLogs(storage.create(todo))("Creating Todo")(err =>
        (err.message, err.cause)
      )(success => s"Created todo: $success")
  }

  def wire[F[_]: MonadCancelThrow: WithContext[*, RequestContext]](
      sql: TodoSql,
      transactor: Transactor[F]
  ): TodoStorage[F] = {
    val logs: Make[F] = Logging.Make.contextual[F, RequestContext]
    implicit val logging: Id[Logging[F]] = logs.forService[TodoStorage[F]]
    val impl = TodoStorage.make[F](sql, transactor)
    new LoggingImpl(impl)
  }

  doobie.free.connection.WeakAsyncConnectionIO
}
