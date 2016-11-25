package szymonbaranczyk

import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import play.api.libs.json.Json
import szymonbaranczyk.enterFlow.PlayerInput
import szymonbaranczyk.exitFlow.{GameData, PlayerData}
import szymonbaranczyk.helper.{InputJsonParser, OutputJsonParser}

/**
  * Created by SBARANCZ on 2016-11-22.
  */
class HelperSpec extends FlatSpec with BeforeAndAfterAll {
  "InputJsonParser" should "parse correctly" in {
    class TestClass extends InputJsonParser {
      def parseJSON(string: String) = Json.parse(string).as[PlayerInput]
    }
    val test = new TestClass().parseJSON(
      "{\n" +
        "\"acceleration\": 1,\n" +
        "\"rotation\": 1,\n" +
        "\"turretRotation\": 1,\n" +
        "\"shot\": false\n}"
    )
  }
  "OutputJsonParser" should "parse correctly" in {
    class TestClass extends OutputJsonParser {
      def parseGameData(gameData: GameData) = Json.toJson(gameData)
    }
    val test = new TestClass().parseGameData(GameData(Seq(PlayerData(0, 0, 0, 0, "1", "")), Seq()))
  }
}
