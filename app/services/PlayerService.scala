package services

import models._
import org.slf4j.LoggerFactory
import javax.inject.{Inject, Singleton}

@Singleton
class PlayerService @Inject()(
                               val iDGeneratorService: IDGeneratorService,
                               val boardService: BoardService,
                               val spaceshipService: SpaceshipService
                             ) {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def createPlayer(): Player = {
    val nextID = iDGeneratorService.getNext()
    val playerID = "Player-" + nextID
    val playerName = "Player " + nextID

    logger.debug("ID = " + playerID)

    val board = boardService.createBoard()
    val spaceships = spaceshipService.createSpaceships(board)

    Player(playerID, playerName, Some(spaceships), board)
  }

  def createPlayerFromRequest(request: GameRequest): Player = {
    val board = boardService.createBoard()
    val spaceships = spaceshipService.createSpaceships(board)
    val playerId = request.player_id
    val fullName = if (playerId.startsWith("user")) s"User ${playerId.stripPrefix("user")}" else s"Player ${playerId}"
    logger.debug(s"Created player '${playerId}' with board hash: ${System.identityHashCode(board)}")

    //    Player(playerId, "Default Name", None, board)
    Player(playerId, fullName, Some(spaceships), board)
  }
}

