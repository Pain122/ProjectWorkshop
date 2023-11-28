package domain

import derevo.circe.{decoder, encoder}
import derevo.derive
import doobie.Read
import sttp.tapir.Schema
import sttp.tapir.derevo.schema
import tofu.logging.derivation._

@derive(loggable, encoder, decoder, schema)
final case class CreateTodo(name: TodoName, remainderDate: RemainderDate)

@derive(loggable, encoder, decoder, schema)
final case class Todo(id: TodoId, name: TodoName, reminderDate: RemainderDate)
