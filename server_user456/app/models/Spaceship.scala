package models

import play.api.libs.json._

case class Spaceship (id: String,name: String, x: Int, y: Int, var status: String)