package connectors

import play.api.libs.json.{Json, Writes} // Combinator syntax
import play.api.libs.json._ // JSON library
import play.api.libs.json.Reads._ // Custom validation helpers
import play.api.libs.functional.syntax._ // Combinator syntax
import models._

trait GameRequestFormatter {

  implicit val protocolReads: Reads[Protocol] = (
    (JsPath \ "hostname").read[String] and
      (JsPath \ "port").read[Int]
    )(Protocol.apply _)

  implicit val gameRequestReads: Reads[GameRequest] = (
    (JsPath \ "user_id").read[String] and
//      (JsPath \ "spaceship_protocol").read[Protocol] and
      (JsPath \ "protocol").read[Protocol] and
      (JsPath \ "game_id").readWithDefault[String]("")
    )(GameRequest.apply _)
}
