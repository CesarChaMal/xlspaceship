package models

import play.api.libs.json._

case class SalvoStatus(hits: Seq[Hit], player: Player, gameComplete: Boolean, gameLost: Boolean)
