// ===================== GameService.scala =====================
package services

import org.slf4j.LoggerFactory
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.util.{Random, Try}
import models._
import javax.inject.{Inject, Singleton}

@Singleton
class GameService @Inject() (
                              val iDGeneratorService: IDGeneratorService,
                              val playerService: PlayerService,
                              val boardService: BoardService
                            ) {

  private val logger = LoggerFactory.getLogger(this.getClass)
  val games = scala.collection.mutable.Map[String, Game]()

  def createGame(gameRequest: GameRequest): Game = {
    val player = playerService.createPlayer()
    val opponent = playerService.createOpponent(gameRequest)
    val gameID = "Game-" + iDGeneratorService.getNext()
//    val game = Game(gameID, player, Array(opponent), false, opponent, opponent)
    val game = Game(gameID, player, Array(opponent), complete = false, winner = null, nextTurn = opponent)

    games += (gameID -> game)
    game
  }

  def getGameStatus(gameID: String): GameStatus = {
    games.get(gameID) match {
      case Some(game) => GameStatus(game.self, game.opponents(0), game.nextTurn)
      case None =>
        logger.debug("Game not found")
        null
    }
  }

  def acceptSalvo(gameID: String, salvo: Salvo): SalvoStatus = {
    logger.debug("\n\n----- Accept Salvo --------\n\n")
    games.get(gameID) match {
      case Some(game) =>
        val hits = ArrayBuffer[Hit]()
        if (!game.complete) {
          for (position <- salvo.hits) {
            val hit = Hit(position, "miss")
            val coordinates = getCoordinate(position)
            var success = false
            for (spaceship <- game.self.spaceships.getOrElse(Array.empty) if !success) {
              success = processSalvo(game.self.board, hit, spaceship, coordinates)
            }

            hits += hit
          }
          val gameOver = !game.self.spaceships.getOrElse(Array.empty).exists(_.active)

          game.complete = gameOver
          SalvoStatus(hits.toSeq, game.self, gameOver, gameLost = false)
        } else {
          for (position <- salvo.hits) {
            hits += Hit(position, "miss")
          }
          SalvoStatus(hits.toSeq, game.self, game.complete, game.complete)
        }
      case None =>
        println("game not found")
        SalvoStatus(Seq.empty, null, gameComplete = false, gameLost = false)
    }
  }

  private def processSalvo(board: Board, hit: Hit, spaceship: XLSpaceship, coordinates: Array[Int]): Boolean = {
    logger.debug("----- processSalvo on XLSpaceship --------")
    logger.debug("hit(x:y)" + hit.position)
    for (part <- spaceship.parts) {
      if (part.x == coordinates(0) && part.y == coordinates(1)) {
        logger.debug("spaceship hit = " + spaceship + " X:y = " + hit.position)
        hit.status = "hit"
        spaceship.parts -= Coordinates(coordinates(0), coordinates(1))
        boardService.updateBoard(board, ListBuffer(Coordinates(coordinates(0), coordinates(1))), "X")
        if (spaceship.parts.isEmpty) {
          spaceship.active = false
          hit.status = "kill"
        }
        return true
      }
    }
    false
  }

  def createSalvo(): Salvo = {
    val randomUtil = new Random()
    val hits = (0 until 5).map { _ =>
      val x = randomUtil.nextInt(16)
      val y = randomUtil.nextInt(16)
      s"${x}x${y}"
    }
    Salvo(hits)
  }

  private def getCoordinate(position: String): Array[Int] = {
    logger.debug("----- getCoordinate --------")
    val arr = position.split("x")
    val x = decode(arr(1))
    logger.debug("x:" + x)
    val y = decode(arr(0))
    logger.debug("y:" + y)
    Array(x, y)
  }

  private def decode(str: String): Int = {
    Try(str.toInt).getOrElse(str match {
      case "A" => 10
      case "B" => 11
      case "C" => 12
      case "D" => 13
      case "E" => 14
      case "F" => 15
    })
  }
}