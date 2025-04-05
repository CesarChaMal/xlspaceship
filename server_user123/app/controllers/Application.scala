package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import play.api.libs.json._
import services.{GameService, WSClientService}
import models.{GameRequest, Protocol, Salvo}
import org.slf4j.LoggerFactory
import formatters.JsonFormatters._

@Singleton
class Application @Inject()(
                             val controllerComponents: ControllerComponents,
                             val gameService: GameService,
                             val wsClientService: WSClientService
                           ) extends BaseController {

  private val logger = LoggerFactory.getLogger(this.getClass)

  /* Handles the incoming salvo from opponent */
  def acceptSalvo(gameID: String) = Action { implicit request =>
    request.body.asJson.map { json =>
      val salvo = Salvo((json \ "salvo").as[Seq[String]])
      logger.debug("salvo = " + salvo)
      val status = gameService.acceptSalvo(gameID, salvo)

      if (!status.gameComplete)
        Ok(Json.toJson(status))
      else
        NotFound(Json.toJson(status))
    }.getOrElse {
      BadRequest("Invalid JSON")
    }
  }

  /* Handles the current game status request from the user */
  def showGameStatus(gameID: String) = Action {
    val gameStatus = gameService.getGameStatus(gameID)

    if (gameStatus != null) {
      Ok(Json.toJson(gameStatus)).withHeaders(corsHeaders: _*)
    } else {
      Ok(Json.toJson(Map("game" -> "game not found"))).withHeaders(corsHeaders: _*)
    }
  }

  /* newGame handles the simulation request for a game from user. Response contains player details and game_id. */
  def newGame = Action { implicit request =>
    request.body.asJson.map { json =>
      json.validate[GameRequest] match {
        case JsSuccess(gameRequest, _) =>
          logger.debug("gameRequest = " + gameRequest)
          Ok(createNewGame(gameRequest)).withHeaders(corsHeaders: _*)
        case JsError(errors) =>
          logger.debug("Invalid request: " + errors.toString)
          BadRequest(Json.toJson("Error in request")).withHeaders(corsHeaders: _*)
      }
    }.getOrElse {
      BadRequest("Expected JSON").withHeaders(corsHeaders: _*)
    }
  }

  def preFlight() = Action {
    Ok.withHeaders(corsHeaders: _*)
  }

  def preFlightWithParam(gameID: String) = Action {
    Ok.withHeaders(corsHeaders: _*)
  }

  /* Delegate method that invokes service calls and processes response from service */
  private def createNewGame(gameRequest: GameRequest): JsValue = {
    val game = gameService.createGame(gameRequest)
    Json.toJson(Map(
      "user_id" -> game.self.id,
      "full_name" -> game.self.name,
      "game_id" -> game.id,
      "starting" -> game.nextTurn.id
    ))
  }

  private def corsHeaders: Seq[(String, String)] = Seq(
    "Access-Control-Allow-Origin" -> "*",
    "Access-Control-Allow-Methods" -> "GET, POST, OPTIONS",
    "Access-Control-Allow-Headers" -> "Origin, X-Requested-With, Content-Type, Accept"
  )

  def index = Action {
    Ok("XL Spaceship API is running.")
  }
}
