package services

import javax.inject.Singleton

@Singleton
class IDGeneratorService {
  private var nextId = 1
  def getNext(): Int = {
    val id = nextId
    nextId += 1
    id
  }
}
