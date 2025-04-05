package models

import play.api.libs.json._

case class Game (id: String, self: Player, opponents : Array[Player], var complete: Boolean, winner: Option[Player], nextTurn: Player)
