package models

import play.api.libs.json._

case class GameRequest(player_id: String, protocol: Protocol)

