package szymonbaranczyk.helper

import play.api.libs.functional.syntax._
import play.api.libs.json._
import szymonbaranczyk.exitFlow.{GameData, PlayerData}

/**
  * Created by Szymon Barańczyk on 28/08/2016.
  */
trait JsonParser {

  implicit val playerWrites = new Writes[PlayerData] {
    def writes(playerData: PlayerData) = Json.obj(
      "x" -> playerData.x,
      "y" -> playerData.y,
      "rotation" -> playerData.rotation,
      "turretRotation" -> playerData.turretRotation
    )
  }
  implicit val gameWrites = new Writes[GameData] {
    def writes(gameData: GameData) = Json.obj(
      "playersData" -> gameData.playersData,
      "gameId" -> gameData.gameId
    )
  }
  implicit val playerReads = (
    (JsPath \ "x").read[Int] and
      (JsPath \ "y").read[Int] and
      (JsPath \ "rotation").read[Int] and
      (JsPath \ "turretRotation").read[Int]
    ) (PlayerData.apply _)
  implicit val gameReads = (
    (JsPath \ "playersData").read[Seq[PlayerData]] and
      (JsPath \ "gameId").read[Int]
    ) (GameData.apply _)


}
