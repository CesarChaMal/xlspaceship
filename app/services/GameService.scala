// ===================== GameService.scala =====================
package services

import org.slf4j.LoggerFactory
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.util.{Random, Try}
import models._
import javax.inject.{Inject, Singleton}

@Singleton
class GameService @Inject()(
                             val iDGeneratorService: IDGeneratorService,
                             val playerService: PlayerService,
                             val boardService: BoardService
                           ) {

  private val logger = LoggerFactory.getLogger(this.getClass)
  val games = scala.collection.mutable.Map[String, Game]()

  // Wrong beccause create 2 players witht the saame board
  /*
    def createGame(gameRequest: GameRequest): Game = {
      val player = playerService.createPlayerFromRequest(gameRequest)
      val opponent = playerService.createPlayerFromRequest(gameRequest)
      val gameID = "Game-" + iDGeneratorService.getNext()
  //    val game = Game(gameID, player, Array(opponent), false, opponent, opponent)
      val game = Game(gameID, player, Array(opponent), complete = false, winner = null, nextTurn = opponent)

      games += (gameID -> game)
      game
    }
  */

  def createGame(gameRequest: GameRequest): Game = {
    logger.debug("\n\n----- Create Game --------\n\n")

    val player = playerService.createPlayerFromRequest(gameRequest)
    boardService.saveBoard(player.id, player.board)

    val gameID = "Game-" + iDGeneratorService.getNext()
    val emptyOpponent = Player("waiting", "Waiting for opponent", None, boardService.createBoard())
    boardService.saveBoard(emptyOpponent.id, emptyOpponent.board)

    logger.debug(s"[DEBUG] Game ID: $gameID")
    logger.debug(s"[DEBUG] Player ID: ${player.id}, Board Hash: ${System.identityHashCode(player.board)}")
    logBoardSample("Player", player.board)

    logger.debug(s"[DEBUG] Opponent ID: ${emptyOpponent.id}, Board Hash: ${System.identityHashCode(emptyOpponent.board)}")
    logBoardSample("Opponent", emptyOpponent.board)

    val game = Game(gameID, player, Array(emptyOpponent), complete = false, winner = None, nextTurn = player)
    games += (gameID -> game)
    game
  }

  def joinGame(gameRequest: GameRequest): Option[Game] = {
    logger.debug("\n\n----- Join Game --------\n\n")

    games.get(gameRequest.game_id) match {
      case Some(game) =>
        val opponent = playerService.createPlayerFromRequest(gameRequest)
        boardService.saveBoard(opponent.id, opponent.board)

        if (game.opponents.isEmpty || game.opponents(0).id == "waiting") {
          val updatedGame = game.copy(
            opponents = Array(opponent),
            nextTurn = Random.shuffle(Seq(game.self, opponent)).head
          )

          logger.debug(s"[DEBUG] Joining Game: ${gameRequest.game_id}")
          logger.debug(s"[DEBUG] Existing Player ID: ${game.self.id}, Board Hash: ${System.identityHashCode(game.self.board)}")
          logBoardSample("Existing Player", game.self.board)
          logSpaceships("Existing Player", game.self.spaceships)

          logger.debug(s"[DEBUG] New Opponent ID: ${opponent.id}, Board Hash: ${System.identityHashCode(opponent.board)}")
          logBoardSample("New Opponent", opponent.board)
          logSpaceships("New Opponent", opponent.spaceships)

          games(gameRequest.game_id) = updatedGame
          Some(updatedGame)
        } else {
          Some(game)
        }

      case None => None
    }
  }

  /*
    def getGameStatus(gameID: String): GameStatus = {
      games.get(gameID) match {
        case Some(game) => GameStatus(game.self, game.opponents(0), game.nextTurn)
        case None =>
          logger.debug("Game not found")
          null
      }
    }
  */


  // In your GameService, both self and opponent (players) share the same Game object in memory. This object is mutated directly during gameplay (e.g., when updating the board, ships, hits),
  // and no player-specific board view is generated on retrieval.
  /*
    def getGameStatus(gameID: String, requesterId: String): GameStatus = {
      games.get(gameID) match {
        case Some(game) =>
          val (self, opponent) =
            if (game.self.id == requesterId) (game.self, game.opponents(0))
            else (game.opponents(0), game.self)

          GameStatus(self, opponent, game.nextTurn)

        case None =>
          logger.debug("Game not found")
          null
      }
    }
  */

  def getGameStatus(gameID: String, requesterId: String): GameStatus = {
    logger.debug("\n\n----- Game Status  --------\n\n")

    games.get(gameID) match {
      case Some(game) =>
        val isSelf = game.self.id == requesterId
        val (selfPlayer, opponentPlayer) =
          if (isSelf) (game.self, game.opponents(0)) else (game.opponents(0), game.self)

        // 🔍 Print basic player info
        logger.debug(s"[STATUS] self.id: ${selfPlayer.id}")
        logger.debug(s"[STATUS] opponent.id: ${opponentPlayer.id}")

        // 🔍 Print board identity hash to check if boards are shared
        logger.debug(s"[STATUS] self.board hash: ${System.identityHashCode(selfPlayer.board)}")
        logger.debug(s"[STATUS] opponent.board hash: ${System.identityHashCode(opponentPlayer.board)}")

        // 🔍 Print a few key cells from each board to verify if masking/cloning applies
        val selfSample = selfPlayer.board.rows(0).columns.take(5).map(_.status).mkString(",")
        val opponentOriginalSample = opponentPlayer.board.rows(0).columns.take(5).map(_.status).mkString(",")

        logger.debug(s"[STATUS] self.board row[0]: $selfSample")
        logger.debug(s"[STATUS] opponent.board row[0] before masking: $opponentOriginalSample")

        // Mask and clone self board
        val selfCloned = boardService.deepCloneBoard(selfPlayer.board)
        val maskedSelfBoard = boardService.maskSelfBoard(selfCloned)

        // Print self masked sample
        val maskedSelfSample = maskedSelfBoard.rows(0).columns.take(5).map(_.status).mkString(",")
        logger.debug(s"[STATUS] self.board row[0] after masking: $maskedSelfSample")

        val selfCopy = selfPlayer.copy(
          board = boardService.maskSelfBoard(selfPlayer.board)
        )

        // Mask and clone opponent board
        val cloned = boardService.deepCloneBoard(opponentPlayer.board)
        val maskedBoard = boardService.maskOpponentBoard(cloned)

        // Print masked sample
        val maskedSample = maskedBoard.rows(0).columns.take(5).map(_.status).mkString(",")
        logger.debug(s"[STATUS] opponent.board row[0] after masking: $maskedSample")

        val opponentCopy = opponentPlayer.copy(
          board = maskedBoard,
          spaceships = None
        )
        logger.debug(s"[STATUS] opponent copy: $opponentCopy")

        logger.debug(s"[STATUS] Final GameStatus ready for requester ${requesterId}")

//        GameStatus(selfPlayer, opponentCopy, game.nextTurn)
        GameStatus(selfCopy, opponentCopy, game.nextTurn)

      case None =>
        logger.warn(s"[STATUS] Game not found: $gameID")
        null
    }
  }

  /*
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
  */

  def acceptSalvo(gameID: String, salvo: Salvo): SalvoStatus = {
    logger.debug("\n\n----- Accept Salvo --------\n\n")

    games.get(gameID) match {
      case Some(game) =>
        val playerId = salvo.player_id
        val isSelfFiring = game.self.id == playerId

        val (attacker, defender) = if (isSelfFiring) (game.self, game.opponents(0)) else (game.opponents(0), game.self)

        logger.debug(s"Before salvo: attacker=${attacker.id}, defender=${defender.id}")
        logger.debug(s"Attacker board hash: ${System.identityHashCode(attacker.board)}")
        logger.debug(s"Defender board hash: ${System.identityHashCode(defender.board)}")

        val hits = ArrayBuffer[Hit]()
        if (!game.complete) {
          for (position <- salvo.hits) {
            val hit = Hit(position, "miss")
            val coordinates = getCoordinate(position)
            var success = false
            for (spaceship <- defender.spaceships.getOrElse(Array.empty) if !success) {
              success = processSalvo(defender.board, hit, spaceship, coordinates)
            }
            if (!success) {
//              boardService.updateBoard(defender.board, ListBuffer(coordinates), "miss")
              boardService.updateBoard(defender.board, ListBuffer(Coordinates(coordinates(0), coordinates(1))), "miss")
            }
            hits += hit
          }

          val gameOver = !defender.spaceships.getOrElse(Array.empty).exists(_.active)

          if (gameOver) {
            val updatedGame = game.copy(winner = Some(attacker), complete = true)
            games.update(gameID, updatedGame)
            SalvoStatus(hits.toSeq, attacker, gameComplete = true, gameLost = !isSelfFiring)
          } else {
            SalvoStatus(hits.toSeq, attacker, gameComplete = false, gameLost = false)
          }

        } else {
          for (position <- salvo.hits) {
            hits += Hit(position, "miss")
          }
          SalvoStatus(hits.toSeq, attacker, game.complete, game.complete && !isSelfFiring)
        }

      case None =>
        logger.warn(s"Game not found: $gameID")
        SalvoStatus(Seq.empty, null, gameComplete = false, gameLost = false)
    }
  }

  /*
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
  */

  private def processSalvo(board: Board, hit: Hit, spaceship: XLSpaceship, coordinates: Array[Int]): Boolean = {
    logger.debug("----- processSalvo on XLSpaceship --------")

    logger.debug("hit(x:y)" + hit.position)
    for (part <- spaceship.parts) {
      if (part.x == coordinates(0) && part.y == coordinates(1)) {
        logger.debug("spaceship hit = " + spaceship + " X:y = " + hit.position)
        hit.status = "hit"
        spaceship.parts -= Coordinates(coordinates(0), coordinates(1))
        boardService.updateBoard(board, ListBuffer(Coordinates(coordinates(0), coordinates(1))), hit.status)
        if (spaceship.parts.isEmpty) {
          spaceship.active = false
          hit.status = "kill"
        }
        return true
      }
    }
    false
  }

  /*
  def createSalvo(): Salvo = {
    val randomUtil = new Random()
    val hits = (0 until 5).map { _ =>
      val x = randomUtil.nextInt(16)
      val y = randomUtil.nextInt(16)
      s"${x}x${y}"
    }
    Salvo(hits)
  }
  */

  /*
  def createSalvo(playerId: String): Salvo = {
    val randomUtil = new Random()
    val hits = (0 until 5).map { _ =>
      val x = randomUtil.nextInt(16)
      val y = randomUtil.nextInt(16)
      s"${x}x${y}"
    }
    Salvo(hits, playerId)
  }
  */

  def createSalvo(gameId: String, requesterId: String): Option[Salvo] = {
    games.get(gameId).map { game =>
      val playerId = if (game.self.id == requesterId) game.self.id else game.opponents.head.id
      val randomUtil = new Random()
      val hits = (0 until 5).map { _ =>
        val x = randomUtil.nextInt(16)
        val y = randomUtil.nextInt(16)
        s"${x}x${y}"
      }
      Salvo(hits, playerId)
    }
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

  private def logBoardSample(label: String, board: Board): Unit = {
    val row0 = board.rows(0).columns.take(5).map(_.status).mkString(", ")
    val row1 = board.rows(1).columns.take(5).map(_.status).mkString(", ")
    logger.debug(s"[$label] Board Row[0]: $row0")
    logger.debug(s"[$label] Board Row[1]: $row1")
  }

  private def logSpaceships(label: String, spaceshipsOpt: Option[Array[XLSpaceship]]): Unit = {
    spaceshipsOpt match {
      case Some(ships) =>
        ships.foreach { s =>
          val partsStr = s.parts.map(p => s"${p.x}x${p.y}").mkString(", ")
          logger.debug(s"[$label] Spaceship: ${s.name} parts: $partsStr active=${s.active}")
        }
      case None =>
        logger.debug(s"[$label] No spaceships found.")
    }
  }
}