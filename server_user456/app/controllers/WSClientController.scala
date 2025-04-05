package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import play.api.libs.json._
import services.GameService
import models.Salvo
import formatters.JsonFormatters._
import scala.concurrent.{ExecutionContext, Future}
import org.slf4j.LoggerFactory

@Singleton
class WSClientController @Inject() (
                                     val controllerComponents: ControllerComponents,
                                     val gameService: GameService
                                   )(implicit ec: ExecutionContext) extends BaseController {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def fireSalvo(gameID: String) = Action(parse.json).async { request =>
    request.body.validate[Salvo] match {
      case JsSuccess(salvo, _) =>
        logger.debug(s"Firing salvo for gameID=$gameID: $salvo")
        Future {
          val status = gameService.acceptSalvo(gameID, salvo)

          if (status == null) {
            logger.warn(s"Game not found or returned null for gameID=$gameID")
            NotFound(Json.obj("error" -> "Game not found"))
          } else {
            Ok(Json.toJson(status))
          }
        }
      case JsError(errors) =>
        logger.warn(s"Invalid JSON for salvo: $errors")
        Future.successful(BadRequest(Json.obj("error" -> "Invalid JSON")))
    }
  }
}
