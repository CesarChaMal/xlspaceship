package formatters

import play.api.libs.json._
import models._

object JsonFormatters {
  implicit val coordinatesFormat: OFormat[Coordinates] = Json.format[Coordinates]
  implicit val cellFormat: OFormat[Cell] = Json.format[Cell]
  implicit val rowFormat: OFormat[Row] = Json.format[Row]
  implicit val boardFormat: OFormat[Board] = Json.format[Board]
  implicit val hitFormat: OFormat[Hit] = Json.format[Hit]
  implicit val xlSpaceshipFormat: OFormat[XLSpaceship] = Json.format[XLSpaceship]
  implicit val spaceshipFormat: OFormat[Spaceship] = Json.format[Spaceship]
  implicit val gameFormat: OFormat[Game] = Json.format[Game]
  implicit val protocolFormat: OFormat[Protocol] = Json.format[Protocol]
  implicit val salvoFormat: OFormat[Salvo] = Json.format[Salvo]
  implicit val gameRequestFormat: OFormat[GameRequest] = Json.format[GameRequest]

  implicit val playerFormat: OFormat[Player] = new OFormat[Player] {
    def reads(json: JsValue): JsResult[Player] = Json.reads[Player].reads(json)
    def writes(player: Player): JsObject =
      if (player == null) Json.obj() else Json.writes[Player].writes(player)
  }

  implicit val gameStatusFormat: OFormat[GameStatus] = new OFormat[GameStatus] {
    def reads(json: JsValue): JsResult[GameStatus] = Json.reads[GameStatus].reads(json)
    def writes(gs: GameStatus): JsObject = Json.obj(
      "self" -> (if (gs.self != null) Json.toJson(gs.self) else JsNull),
      "opponent" -> (if (gs.opponent != null) Json.toJson(gs.opponent) else JsNull),
      "nextTurn" -> (if (gs.nextTurn != null) JsString(gs.nextTurn.id) else JsNull)
    )
  }

  implicit val salvoStatusFormat: OFormat[SalvoStatus] = new OFormat[SalvoStatus] {
    def reads(json: JsValue): JsResult[SalvoStatus] = Json.reads[SalvoStatus].reads(json)

    def writes(status: SalvoStatus): JsObject = {
      val hitsJson = status.hits.map(hit => hit.position -> JsString(hit.status)).toMap

      val gameJson = if (status.player != null) Json.obj(
        "gameComplete" -> status.gameComplete,
        "won" -> !status.gameLost,
        "player_id" -> status.player.id
      ) else Json.obj(
        "gameComplete" -> status.gameComplete,
        "won" -> !status.gameLost
      )

      Json.obj(
        "salvo" -> JsObject(hitsJson),
        "game" -> gameJson
      )
    }
  }

}
