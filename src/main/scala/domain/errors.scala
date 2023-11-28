package domain

import cats.syntax.option._
import derevo.circe.{decoder, encoder}
import derevo.derive
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}
import sttp.tapir.Schema
import sttp.tapir.derevo.schema

object errors {
  @derive(encoder, decoder)
  sealed abstract class AppError(
      val message: String,
      val cause: Option[Throwable] = None
  )

  @derive(encoder, decoder)
  case class TodoAlreadyExists()
      extends AppError("Todo with same name and date already exists")
  @derive(encoder, decoder)
  case class TodoNotFound(id: TodoId)
      extends AppError(s"Todo with id ${id.value} not found")
  @derive(encoder, decoder)
  case class InternalError(
      cause0: Throwable
  ) extends AppError("Internal error", cause0.some)
  @derive(encoder, decoder)
  case class DecodedError(override val message: String)
      extends AppError(message = message)

//  implicit val encoder: Encoder[AppError] = (a: AppError) =>
//    Json.obj(
//      ("message", Json.fromString(a.message))
//    )
//
//  implicit val decoder: Decoder[AppError] = (c: HCursor) => c.downField("message").as[String].map(DecodedError.apply)

  implicit val throwableEncoder: Encoder[Throwable] =
    Encoder.encodeString.contramap(_.getMessage)
  implicit val throwableDecoder: Decoder[Throwable] =
    Decoder.decodeString.map(new Throwable(_))
  implicit val schema: Schema[AppError] =
    Schema.schemaForString.map[AppError](str => Some(DecodedError(str)))(
      _.message
    )
}
