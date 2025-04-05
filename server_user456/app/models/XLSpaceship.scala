package models

import play.api.libs.json._
import scala.collection.mutable.ListBuffer

case class XLSpaceship(name: String, parts: ListBuffer[Coordinates], var active: Boolean)