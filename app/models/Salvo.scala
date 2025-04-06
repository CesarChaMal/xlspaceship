package models

import play.api.libs.json._

//case class Salvo(hits: Seq[String])
case class Salvo(hits: Seq[String], player_id: String)
