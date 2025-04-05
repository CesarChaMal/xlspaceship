package models

import play.api.libs.json._

case class Board (id: String, rows: Array[Row])