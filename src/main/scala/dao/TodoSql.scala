package dao

import cats.{Applicative, Monad}
import cats.syntax.applicative._
import cats.syntax.either._
import domain._
import domain.errors._
import doobie._
import doobie.implicits._

trait TodoSql {
  def listAll: ConnectionIO[List[Todo]]
  def findById(id: TodoId): ConnectionIO[Option[Todo]]
  def removeById(id: TodoId): ConnectionIO[Either[TodoNotFound, Unit]]
  def create(todo: CreateTodo): ConnectionIO[Either[TodoAlreadyExists, Todo]]
}

object TodoSql {

  object sqls {
    val listAllSql: Query0[Todo] =
      sql"""
           select *
           from TODOS
      """.query[Todo]

    def findByIdSql(id: TodoId): Query0[Todo] =
      sql"""
           select *
           from TODOS
           where id=${id.value}
      """.query[Todo]

    def removeByIdSql(id: TodoId): Update0 =
      sql"""
           delete from TODOS
           where id=${id.value}
         """.update

    def insertSql(todo: CreateTodo): Update0 =
      sql"""
            insert into TODOS (name, remainder_date)
            values (${todo.name.value}, ${todo.remainderDate.value.toEpochMilli}})
           """.update

    def findByNameAndDate(name: TodoName, date: RemainderDate): Query0[Todo] =
      sql"select * from TODOS where name=${name.value} and remainder_date=${date.value.toEpochMilli}}"
        .query[Todo]
  }

  private final class Impl extends TodoSql {

    import sqls._

    override def listAll: ConnectionIO[List[Todo]] = listAllSql.to[List]

    override def findById(
        id: TodoId
    ): ConnectionIO[Option[Todo]] = findByIdSql(id).option

    override def removeById(
        id: TodoId
    ): ConnectionIO[Either[TodoNotFound, Unit]] = removeByIdSql(id).run.map {
      case 0 => TodoNotFound(id).asLeft
      case _ => ().asRight
    }

    override def create(
        todo: CreateTodo
    ): ConnectionIO[Either[TodoAlreadyExists, Todo]] =
      findByNameAndDate(todo.name, todo.remainderDate).option.flatMap {
        case Some(_) => TodoAlreadyExists().asLeft[Todo].pure[ConnectionIO]
        case None =>
          insertSql(todo)
            .withUniqueGeneratedKeys[TodoId]("id")
            .map((id: TodoId) =>
              Todo(id, todo.name, todo.remainderDate).asRight
            )
      }
  }

  def make: TodoSql = new Impl
}
