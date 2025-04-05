package models

import play.api.libs.json._

case class Cell (x: Int, y:Int, var status: String)