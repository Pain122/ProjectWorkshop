import cats.data.ReaderT
import cats.effect.IO
import derevo.circe.{decoder, encoder}
import derevo.derive
import doobie.Read
import io.estatico.newtype.macros.newtype
import tofu.logging.derivation._

import java.time.Instant

package object domain {

  @derive(loggable, encoder, decoder)
  @newtype
  case class TodoId(value: Long)
  object TodoId {
    implicit val read: Read[TodoId] = Read[Long].map(TodoId.apply)
  }

  @derive(loggable, encoder, decoder)
  @newtype
  case class TodoName(value: String)
  object TodoName {
    implicit val read: Read[TodoName] = Read[String].map(TodoName.apply)
  }

  @derive(loggable, encoder, decoder)
  @newtype
  case class RemainderDate(value: Instant)
  object RemainderDate {
    implicit val read: Read[RemainderDate] =
      Read[Long].map(n => RemainderDate(Instant.ofEpochMilli(n)))
  }
}
