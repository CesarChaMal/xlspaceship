package services

import play.api.libs.ws._
import play.api.libs.json._
import javax.inject.{Inject, Singleton}
import org.slf4j.LoggerFactory
import models._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class WSClientService @Inject()(ws: WSClient)(implicit ec: ExecutionContext) {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def fireSalvo(salvo: Salvo): Future[String] = {
    val url = "http://www.mocky.io/v2/587ac1251100006716d39466"
    val request: WSRequest = ws.url(url)
    val data = Json.obj("salvo" -> salvo.hits)

    logger.debug(s"Sending salvo: $data")

    val futureResponse: Future[WSResponse] = request.put(data)

    futureResponse.map(_.body).recover {
      case t: Throwable =>
        logger.error("Error sending salvo: ", t)
        "error"
    }
  }
}
