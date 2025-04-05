package models

import play.api.libs.json._

case class Player (id: String,name: String, spaceships: Option[Array[XLSpaceship]], board: Board)