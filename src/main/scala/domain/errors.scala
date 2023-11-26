package domain

import cats.syntax.option._

object errors {
  sealed abstract class AppError(
      val message: String,
      val cause: Option[Throwable] = None
  )

  case class TodoAlreadyExists()
      extends AppError("Todo with same name and date already exists")
  case class TodoNotFound(id: TodoId)
      extends AppError(s"Todo with id ${id.value} not found")
  case class InternalError(cause0: Throwable)
      extends AppError("Internal error", cause0.some)
}
