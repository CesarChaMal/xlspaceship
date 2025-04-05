package models

import play.api.libs.json._

case class GameStatus(self: Player, opponent: Player, nextTurn: Player)

