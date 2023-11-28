package service

import cats.Id
import cats.effect.IO
import cats.syntax.either._
import dao.TodoSql
import domain._
import domain.errors._
import doobie._
import doobie.implicits._
import tofu.logging.Logging
import tofu.logging.Logging.Make
import tofu.syntax.logging._

trait TodoStorage {
  def list: IO[Either[InternalError, List[Todo]]]
  def findById(
      id: TodoId
  ): IO[Either[InternalError, Option[Todo]]]
  def removeById(id: TodoId): IO[Either[AppError, Unit]]
  def create(todo: CreateTodo): IO[Either[AppError, Todo]]
}

object TodoStorage {
  private final class Impl(todoSql: TodoSql, transactor: Transactor[IO])
      extends TodoStorage {
    override def list: IO[Either[InternalError, List[Todo]]] =
      todoSql.listAll
        .transact(transactor)
        .attempt
        .map(_.leftMap(InternalError.apply))
    override def findById(
        id: TodoId
    ): IO[Either[InternalError, Option[Todo]]] =
      todoSql
        .findById(id)
        .transact(transactor)
        .attempt
        .map(_.leftMap(InternalError.apply))

    override def removeById(
        id: TodoId
    ): IO[Either[AppError, Unit]] =
      todoSql.removeById(id).transact(transactor).attempt.map {
        case Left(th)           => InternalError(th).asLeft
        case Right(Left(error)) => error.asLeft
        case _                  => ().asRight
      }

    override def create(
        todo: CreateTodo
    ): IO[Either[AppError, Todo]] =
      todoSql.create(todo).transact(transactor).attempt.map {
        case Left(th)           => InternalError(th).asLeft
        case Right(Left(error)) => error.asLeft
        case Right(Right(todo)) => todo.asRight
      }
  }

  private final class LoggingImpl(storage: TodoStorage)(implicit
      logging: Logging[IO]
  ) extends TodoStorage {

    private def surroundWithLogs[Error, Res](
        io: IO[Either[Error, Res]]
    )(
        inputLog: String
    )(errorOutputLog: Error => (String, Option[Throwable]))(
        successOutputLog: Res => String
    ): IO[Either[Error, Res]] =
      info"$inputLog" *> io.flatTap {
        case Left(error) =>
          val (logString: String, throwable: Option[Throwable]) =
            errorOutputLog(error)
          throwable.fold(error"$logString")(err =>
            errorCause"$logString"(err)
          )
        case Right(success) => info"${successOutputLog(success)}"
      }

    override def list: IO[Either[InternalError, List[Todo]]] =
      surroundWithLogs(storage.list)("Getting all todos")(err =>
        ("Error while getting all todos", Some(err.cause0))
      )(s => s"Got all todos $s")

    override def findById(
        id: TodoId
    ): IO[Either[InternalError, Option[Todo]]] =
      surroundWithLogs(storage.findById(id))("Finding todo by id")(err =>
        (s"Failed to find todo by id${err.message}", Some(err.cause0))
      )(success => s"Found todo: $success")

    override def removeById(
        id: TodoId
    ): IO[Either[AppError, Unit]] =
      surroundWithLogs(storage.removeById(id))("removing todo")(err =>
        (err.message, err.cause)
      )(_ => "Delete successful")

    override def create(
        todo: CreateTodo
    ): IO[Either[AppError, Todo]] =
      surroundWithLogs(storage.create(todo))("Creating Todo")(err =>
        (err.message, err.cause)
      )(success => s"Created todo: $success")

  }

  def make(
      sql: TodoSql,
      transactor: Transactor[IO]
  ): TodoStorage = {
    val logs: Make[IO] = Logging.Make.plain[IO]
    implicit val logging: Id[Logging[IO]] = logs.forService[TodoStorage]
    val impl = new Impl(sql, transactor)
    new LoggingImpl(impl)
  }

  doobie.free.connection.WeakAsyncConnectionIO
}
