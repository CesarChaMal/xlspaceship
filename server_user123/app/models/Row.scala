package models

import play.api.libs.json._

case class Row (id: Int, columns: Array[Cell])