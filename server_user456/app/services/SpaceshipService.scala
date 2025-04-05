package services

import play._
import play.api.libs.json.{Json, Writes, _}
import scala.jdk.CollectionConverters._
import scala.collection.mutable.ArrayBuffer
import scala.language.postfixOps
import org.slf4j.LoggerFactory
import javax.inject.{Inject, Singleton}
import play.api.Configuration
import models._
import scala.collection.mutable.ListBuffer

@Singleton
class SpaceshipService @Inject()(
                                  config: Configuration,
                                  val iDGeneratorService: IDGeneratorService,
                                  val boardService: BoardService
                                ) {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def getCoordinates(spaceshipType: String): ListBuffer[Coordinates] = {
    val coordinates = config.get[Seq[Configuration]](s"spaceship.$spaceshipType").map { p =>
      Coordinates(p.get[Int]("x"), p.get[Int]("y"))
    }
    coordinates.to(ListBuffer)
  }


  def loadSpaceShipConfiguration(spaceshipType: String): List[Coordinates] = {
    logger.debug(" \n loadSpaceShipConfiguration --> " + spaceshipType)

    val coordinatesList: List[Coordinates] = {
      val list = config.get[Seq[Configuration]](s"spaceship.$spaceshipType")
      list.map(p => Coordinates(p.get[Int]("x"), p.get[Int]("y"))).toList
    }

    coordinatesList.foreach { c =>
      println(c.x + ":" + c.y)
    }

    coordinatesList
  }


  /*this class creates spaceships for player*/
  def createSpaceships(board: Board): Array[XLSpaceship] = {

    //
    var range = loadSpaceShipConfiguration("range")
    //Step 1. Load config of spaceship
    var configCoordinates = loadSpaceShipConfiguration("winger")
    //Step 2. Get unused coordinates
    var allocatedCoordinates = boardService.allocateCoordinates(board, configCoordinates, range)
    //Step 3. updated board with config
    boardService.updateBoard(board, allocatedCoordinates, "*")

    //Step 4. create spaceship
    var nextID = iDGeneratorService.getNext()
    val winger = XLSpaceship("Winger", allocatedCoordinates, true)


    //Step 1. Load config of spaceship
    configCoordinates = loadSpaceShipConfiguration("angle")
    //Step 2. Get unused coordinates
    allocatedCoordinates = boardService.allocateCoordinates(board, configCoordinates, range)
    //Step 3. updated board with config
    boardService.updateBoard(board, allocatedCoordinates, "*")

    //Step 4. create spaceship
    val angle = XLSpaceship("Angle", allocatedCoordinates, true)


    //Step 1. Load config of spaceship
    configCoordinates = loadSpaceShipConfiguration("aClass")
    //Step 2. Get unused coordinates
    allocatedCoordinates = boardService.allocateCoordinates(board, configCoordinates, range)
    //Step 3. updated board with config
    boardService.updateBoard(board, allocatedCoordinates, "*")

    //Step 4. create spaceship
    val aClass = XLSpaceship("A-Class", allocatedCoordinates, true)

    //Step 1. Load config of spaceship
    configCoordinates = loadSpaceShipConfiguration("bClass")
    //Step 2. Get unused coordinates
    allocatedCoordinates = boardService.allocateCoordinates(board, configCoordinates, range)
    //Step 3. updated board with config
    boardService.updateBoard(board, allocatedCoordinates, "*")

    //Step 4. create spaceship
    val bClass = XLSpaceship("B-Class", allocatedCoordinates, true)


    //Step 1. Load config of spaceship
    configCoordinates = loadSpaceShipConfiguration("sClass")
    //Step 2. Get unused coordinates
    allocatedCoordinates = boardService.allocateCoordinates(board, configCoordinates, range)
    //Step 3. updated board with config
    boardService.updateBoard(board, allocatedCoordinates, "*")

    //Step 4. create spaceship
    val sClass = XLSpaceship("S-Class", allocatedCoordinates, true)


    Array(winger, angle, aClass, bClass, sClass)

  }

}
