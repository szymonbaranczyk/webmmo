package szymonbaranczyk.helper

import play.api.libs.functional.syntax._
import play.api.libs.json._
import szymonbaranczyk.dataLayer.BulletState
import szymonbaranczyk.exitFlow.{GameData, PlayerData}

/**
  * Created by Szymon Barańczyk.
  */
trait OutputJsonParser {

  implicit val playerWrites = new Writes[PlayerData] {
    def writes(playerData: PlayerData) = Json.obj(
      "x" -> playerData.x,
      "y" -> playerData.y,
      "rotation" -> playerData.rotation,
      "turretRotation" -> playerData.turretRotation,
      "id" -> playerData.id,
      "meta" -> playerData.meta
    )
  }
  implicit val bulletWrites = new Writes[BulletState] {
    def writes(bulletState: BulletState) = Json.obj(
      "x" -> bulletState.x,
      "y" -> bulletState.y,
      "id" -> bulletState.id
    )
  }
  implicit val gameWrites = new Writes[GameData] {
    def writes(gameData: GameData) = Json.obj(
      "playersData" -> gameData.playersData,
      "bulletData" -> gameData.bulletData
    )
  }
  implicit val playerReads = (
    (JsPath \ "x").read[Int] and
      (JsPath \ "y").read[Int] and
      (JsPath \ "rotation").read[Int] and
      (JsPath \ "turretRotation").read[Int] and
      (JsPath \ "id").read[String] and
      (JsPath \ "meta").read[String]
    ) (PlayerData.apply _)

  implicit val bulletReads = (
    (JsPath \ "x").read[Int] and
      (JsPath \ "y").read[Int] and
      (JsPath \ "id").read[Int]
    ) (BulletState.apply _)
  implicit val gameReads: Reads[GameData] = (
    (JsPath \ "playersData").read[Seq[PlayerData]] and
      (JsPath \ "bulletData").read[Seq[BulletState]]
    ) (GameData.apply _)

}
