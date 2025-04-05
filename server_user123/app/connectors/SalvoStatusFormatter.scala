package connectors

import play.api.libs.json._
import models._
import formatters.JsonFormatters._

trait SalvoStatusFormatter {

  implicit val salvoStatusFormat: OFormat[SalvoStatus] = new OFormat[SalvoStatus] {
    def reads(json: JsValue): JsResult[SalvoStatus] = Json.reads[SalvoStatus].reads(json)

    def writes(status: SalvoStatus): JsObject = {
      val hitsJson = status.hits.map(hit => hit.position -> JsString(hit.status)).toMap

      val gameJson = if (status.player != null) {
        Json.obj(
          "gameComplete" -> status.gameComplete,
          "won" -> !status.gameLost,
          "player_id" -> status.player.id
        )
      } else {
        Json.obj(
          "gameComplete" -> status.gameComplete,
          "won" -> !status.gameLost
        )
      }

      Json.obj(
        "salvo" -> JsObject(hitsJson),
        "game" -> gameJson
      )
    }
  }
}
