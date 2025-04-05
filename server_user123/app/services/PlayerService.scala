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

  def createOpponent(gameRequest: GameRequest): Player = {
    val board = boardService.createBoard()
    Player(gameRequest.player_id, "Default Name", None, board)
  }
}

