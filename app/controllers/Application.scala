package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import play.api.libs.json._
import services.{GameService, WSClientService}
import models.{GameRequest, Protocol, Salvo}
import org.slf4j.LoggerFactory
import formatters.JsonFormatters._
import services.BoardService
import models._

@Singleton
class Application @Inject()(
                             val controllerComponents: ControllerComponents,
                             val gameService: GameService,
                             val boardService: BoardService,
                             val wsClientService: WSClientService
                           ) extends BaseController {

  private val logger = LoggerFactory.getLogger(this.getClass)

  /* Handles the incoming salvo from opponent */
  def acceptSalvo(gameID: String) = Action { implicit request =>
    request.body.asJson.map { json =>
      //      val salvo = Salvo((json \ "salvo").as[Seq[String]])
      val salvo = json.as[Salvo]
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
  /*
    def showGameStatus(gameID: String) = Action {
      val gameStatus = gameService.getGameStatus(gameID)

      if (gameStatus != null) {
        Ok(Json.toJson(gameStatus)).withHeaders(corsHeaders: _*)
      } else {
        Ok(Json.toJson(Map("game" -> "game not found"))).withHeaders(corsHeaders: _*)
      }
    }
  */

  def showGameStatus(gameID: String) = Action { request =>
    val playerID = request.getQueryString("player_id").getOrElse("unknown")
    logger.debug(s"[STATUS] Request from player: $playerID for game: $gameID")

    val gameStatus = gameService.getGameStatus(gameID, playerID)

    if (gameStatus == null) {
      logger.warn(s"[STATUS] Game not found: $gameID")
      Ok(Json.obj("error" -> s"Game '$gameID' not found")).withHeaders(corsHeaders: _*)
    } else {
      logger.debug(s"[STATUS] Self: ${gameStatus.self.id}, Opponent: ${gameStatus.opponent.id}")
      logger.debug(s"[STATUS] Self Board Hash: ${System.identityHashCode(gameStatus.self.board)}")
      logger.debug(s"[STATUS] Opponent Board Hash: ${System.identityHashCode(gameStatus.opponent.board)}")
      logBoardSample("Self", gameStatus.self.board)
      logBoardSample("Opponent (masked)", gameStatus.opponent.board)

      Ok(Json.toJson(gameStatus)).withHeaders(corsHeaders: _*)
    }
  }

  /* newGame handles the simulation request for a game from user. Response contains player details and game_id. */
  def newGame = Action { implicit request =>
    request.body.asJson.map { json =>
      json.validate[GameRequest] match {
        case JsSuccess(gameRequest, _) =>
          logger.debug(s"[NEW GAME] GameRequest: $gameRequest")
          val gameJson = createNewGame(gameRequest)
          logger.debug(s"[NEW GAME] Response JSON: ${Json.prettyPrint(gameJson)}")
          Ok(gameJson).withHeaders(corsHeaders: _*)
        case JsError(errors) =>
          logger.warn(s"[NEW GAME] Invalid request: $errors")
          BadRequest(Json.toJson("Error in request")).withHeaders(corsHeaders: _*)
      }
    }.getOrElse {
      logger.warn("[NEW GAME] Expected JSON")
      BadRequest("Expected JSON").withHeaders(corsHeaders: _*)
    }
  }

  def preFlight() = Action {
    Ok.withHeaders(corsHeaders: _*)
  }

  def preFlightWithParam(gameID: String) = Action {
    Ok.withHeaders(corsHeaders: _*)
  }

  private def createNewGame(gameRequest: GameRequest): JsValue = {
    val game = gameService.createGame(gameRequest)

    logger.debug(s"[CONTROLLER] New Game created -> ID: ${game.id}")
    logger.debug(s"[CONTROLLER] Self ID: ${game.self.id}, Board Hash: ${System.identityHashCode(game.self.board)}")
    logBoardSample("Self", game.self.board)
    logSpaceships("Self", game.self.spaceships)

    logger.debug(s"[CONTROLLER] Opponent ID: ${game.opponents.head.id}, Board Hash: ${System.identityHashCode(game.opponents.head.board)}")
    logBoardSample("Opponent", game.opponents.head.board)
    logSpaceships("Opponent", game.opponents.head.spaceships)

    Json.toJson(Map(
      "user_id" -> game.self.id,
      "full_name" -> game.self.name,
      "game_id" -> game.id,
      "starting" -> game.nextTurn.id
    ))
  }

  def joinGame: Action[JsValue] = Action(parse.json) { request =>
    val json = request.body
    json.validate[GameRequest] match {
      case JsSuccess(gameRequest, _) =>
        gameService.joinGame(gameRequest) match {
          case Some(game) =>
            val opponent = game.opponents(0)

            logger.debug(s"[CONTROLLER] Player joined -> Opponent ID: ${opponent.id}, Board Hash: ${System.identityHashCode(opponent.board)}")
            logBoardSample("Opponent", opponent.board)
            logSpaceships("Opponent", opponent.spaceships)

            logger.debug(s"[CONTROLLER] Existing Self ID: ${game.self.id}, Board Hash: ${System.identityHashCode(game.self.board)}")
            logBoardSample("Self", game.self.board)
            logSpaceships("Self", game.self.spaceships)

            val response = Json.obj(
              "user_id" -> opponent.id,
              "full_name" -> opponent.name,
              "game_id" -> game.id,
              "starting" -> game.nextTurn.id
            )
            Ok(response).withHeaders(corsHeaders: _*)
          case None =>
            NotFound(Json.obj("error" -> s"Game ID '${gameRequest.game_id}' not found")).withHeaders(corsHeaders: _*)
        }

      case JsError(errors) =>
        BadRequest(Json.obj("error" -> "Invalid request")).withHeaders(corsHeaders: _*)
    }
  }

  private def corsHeaders: Seq[(String, String)] = Seq(
    "Access-Control-Allow-Origin" -> "*",
    "Access-Control-Allow-Methods" -> "GET, POST, OPTIONS",
    "Access-Control-Allow-Headers" -> "Origin, X-Requested-With, Content-Type, Accept"
  )

  def index = Action {
    Ok("XL Spaceship API is running.")
  }

  private def logBoardSample(label: String, board: Board): Unit = {
    val sample = board.rows(0).columns.take(5).map(_.status).mkString(", ")
    logger.debug(s"[$label] Board Sample Row[0]: $sample")
  }

  private def logSpaceships(label: String, shipsOpt: Option[Array[XLSpaceship]]): Unit = {
    shipsOpt match {
      case Some(ships) =>
        ships.foreach { ship =>
          val partsStr = ship.parts.map(p => s"${p.x}x${p.y}").mkString(", ")
          logger.debug(s"[$label] Ship: ${ship.name}, Parts: $partsStr, Active: ${ship.active}")
        }
      case None =>
        logger.debug(s"[$label] No spaceships.")
    }
  }
}
