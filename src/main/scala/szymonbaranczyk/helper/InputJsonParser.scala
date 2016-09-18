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
      "move" -> playerInput.move,
      "shot" -> playerInput.shot
    )
  }
  implicit val playerReads = (
    (JsPath \ "move").read[String] and
      (JsPath \ "shot").read[Boolean]
    ) (PlayerInput.apply _)

}
