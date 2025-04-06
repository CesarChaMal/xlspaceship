package services

import models._
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.util.Random
import org.slf4j.LoggerFactory
import javax.inject.{Inject, Singleton}

@Singleton
class BoardService @Inject()(val iDGeneratorService: IDGeneratorService) {

  private val logger = LoggerFactory.getLogger(this.getClass)
  private val boardsByPlayerId = scala.collection.mutable.Map[String, Board]()

  def saveBoard(playerId: String, board: Board): Unit = {
    boardsByPlayerId(playerId) = board
  }

  def findByPlayerId(playerId: String): Option[Board] = boardsByPlayerId.get(playerId)

  def createBoard(): Board = {
    val occupiedList = ListBuffer[Coordinates]()

    val nextID = iDGeneratorService.getNext()
    val boardID = "Board-" + nextID
    logger.debug(s"[BOARD] Creating board with ID: $boardID")

    val rows = ArrayBuffer[Row]()

    for (i <- 0 to 15) {
      val columns = ArrayBuffer[Cell]()
      for (j <- 0 to 15) {
        columns += Cell(i, j, ".")
      }
      rows += Row(i, columns.toArray)
    }

    val board = Board(boardID, rows.toArray)
    logger.debug(s"[BOARD] Created board hash: ${System.identityHashCode(board)}")
    logBoardSample(board, "createBoard")

    board
  }

  def maskOpponentBoard(board: Board): Board = {
    logger.debug(s"[MASK] Masking opponent board with hash: ${System.identityHashCode(board)}")

    val maskedRows = board.rows.map { row =>
      val maskedCols = row.columns.map { cell =>
        val maskedStatus = cell.status match {
          case "hit" | "kill" => "X"   // official symbol for hit
          case "miss"         => "-"   // official symbol for miss
          case "*"            => "*" // show ship (not hit)
          case _              => "."   // fog of war
        }
        cell.copy(status = maskedStatus)
      }
      Row(row.id, maskedCols)
    }

    val maskedBoard = board.copy(rows = maskedRows.toArray)
    logger.debug(s"[MASK] Finished masking board: ${System.identityHashCode(maskedBoard)}")
    logBoardSample(maskedBoard, "maskOpponentBoard")
    maskedBoard
  }

  def maskSelfBoard(board: Board): Board = {
    logger.debug(s"[MASK] Masking self board with hash: ${System.identityHashCode(board)}")

    val maskedRows = board.rows.map { row =>
      val maskedCols = row.columns.map { cell =>
        val maskedStatus = cell.status match {
          case "hit" | "kill" => "X"
          case "miss"         => "-"
          case "*"            => "*" // show ship (not hit)
          case _              => "." // untouched
        }
        cell.copy(status = maskedStatus)
      }
      Row(row.id, maskedCols)
    }

    val maskedBoard = board.copy(rows = maskedRows.toArray)
    logger.debug(s"[MASK] Finished masking self board: ${System.identityHashCode(maskedBoard)}")
    logBoardSample(maskedBoard, "maskSelfBoard")
    maskedBoard
  }

/*
  def maskOpponentBoard(board: Board): Board = {
    logger.debug(s"[MASK] Masking opponent board with hash: ${System.identityHashCode(board)}")

    val maskedRows = board.rows.map { row =>
      val maskedCols = row.columns.map { cell =>
        val maskedStatus = cell.status match {
          case "*" | "o" => cell.status // known hit or miss
          case _         => "."         // fog
        }
        cell.copy(status = maskedStatus)
      }
      Row(row.id, maskedCols)
    }

    val maskedBoard = board.copy(rows = maskedRows.toArray)
    logger.debug(s"[MASK] Finished masking board: ${System.identityHashCode(maskedBoard)}")
    logBoardSample(maskedBoard, "maskOpponentBoard")
    maskedBoard
  }
*/

  def allocateCoordinates(board: Board, spaceshipConfig: List[Coordinates], range: List[Coordinates]): ListBuffer[Coordinates] = {
    logger.debug(s"[ALLOCATE] Allocating coordinates for spaceship on board hash: ${System.identityHashCode(board)}")

    var allocated = false
    var coordinatesList: ListBuffer[Coordinates] = null

    do {
      val x = Random.nextInt(16)
      val y = Random.nextInt(16)
      logger.debug(s"[ALLOCATE] Trying root (x=$x, y=$y)")

      val rangeCoordinatesList = range.map(c => Coordinates(c.x + x, c.y + y))
      val ok = rangeCoordinatesList.forall { c =>
        c.x <= 15 && c.y <= 15 && board.rows(c.y).columns(c.x).status == "."
      }

      if (ok) {
        coordinatesList = ListBuffer(spaceshipConfig.map(c => Coordinates(c.x + x, c.y + y)): _*)
        allocated = true
        logger.debug(s"[ALLOCATE] Allocated coordinates: ${coordinatesList.mkString(", ")}")
      }

    } while (!allocated)

    coordinatesList
  }

  def updateBoard(board: Board, coordinatesList: ListBuffer[Coordinates], status: String): Unit = {
    logger.debug(s"[UPDATE] Updating board: ${System.identityHashCode(board)} with status '$status' for coordinates: ${coordinatesList.mkString(", ")}")
    for (c <- coordinatesList) {
      board.rows(c.y).columns(c.x).status = status
    }
    logBoardSample(board, "updateBoard")
  }

  def deepCloneBoard(original: Board): Board = {
    val newBoardId = "Board-" + iDGeneratorService.getNext()
    logger.debug(s"[CLONE] Cloning board ID: ${original.id}, hash: ${System.identityHashCode(original)}")

    val clonedRows = original.rows.map { row =>
      val clonedCols = row.columns.map(cell => cell.copy())
      Row(row.id, clonedCols)
    }

    val clonedBoard = Board(newBoardId, clonedRows)
    logger.debug(s"[CLONE] Created clone board ID: $newBoardId, hash: ${System.identityHashCode(clonedBoard)}")
    logBoardSample(clonedBoard, "deepCloneBoard")

    clonedBoard
  }

  def allocateCoordinates(board: Board): Array[Int] = {
    logger.debug(s"[ALLOCATE] Finding free cell on board hash: ${System.identityHashCode(board)}")

    var allocated = false
    val coordinates = ArrayBuffer[Int]()

    while (!allocated) {
      val x = Random.nextInt(16)
      val y = Random.nextInt(16)

      if (board.rows(x).columns(y).status == ".") {
        allocated = true
        logger.debug(s"[ALLOCATE] Found free cell at: ($x, $y)")
        coordinates += x
        coordinates += y
      } else {
        logger.debug(s"[ALLOCATE] Cell ($x, $y) already used. Retrying...")
      }
    }

    coordinates.toArray
  }

  private def logBoardSample(board: Board, context: String): Unit = {
    val sample = board.rows.head.columns.take(5).map(_.status).mkString(", ")
    logger.debug(s"[$context] Board Sample [row 0]: $sample")
  }
}
