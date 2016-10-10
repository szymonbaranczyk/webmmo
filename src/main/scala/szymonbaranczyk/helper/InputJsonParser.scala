package szymonbaranczyk.helper

import play.api.libs.functional.syntax._
import play.api.libs.json._
import szymonbaranczyk.enterFlow.PlayerInput

/**
  * Created by Szymon Barańczyk.
  */
trait InputJsonParser {
  implicit val playerInputWrites = new Writes[PlayerInput] {
    def writes(playerInput: PlayerInput) = Json.obj(
      "acceleration" -> playerInput.acceleration,
      "rotation" -> playerInput.rotation,
      "shot" -> playerInput.shot
    )
  }
  implicit val playerReads = (
    (JsPath \ "acceleration").read[Int] and
      (JsPath \ "rotation").read[Int] and
      (JsPath \ "shot").read[Boolean]
    ) (PlayerInput.apply _)

}
