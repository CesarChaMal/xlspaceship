package controllers

import play.api.mvc._
import connectors.{GameFormatter, GameRequestFormatter, SalvoStatusFormatter}

trait BaseController extends InjectedController
  with GameFormatter
  with GameRequestFormatter
  with SalvoStatusFormatter
