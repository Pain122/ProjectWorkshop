package domain

import derevo.circe.{decoder, encoder}
import derevo.derive
import doobie.Read
import sttp.tapir.Schema
import tofu.logging.derivation._

@derive(loggable, encoder, decoder)
final case class CreateTodo(name: TodoName, remainderDate: RemainderDate)

@derive(loggable, encoder, decoder)
final case class Todo(id: TodoId, name: TodoName, reminderDate: RemainderDate)
