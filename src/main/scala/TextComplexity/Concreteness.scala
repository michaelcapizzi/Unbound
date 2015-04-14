package TextComplexity

import scala.io.Source

/**
 * Created by mcapizzi on 4/8/15.
 */
object Concreteness {

  val concretenessRaw = Source.fromFile("/home/mcapizzi/Github/Unbound/src/main/resources/concretenessData.csv").getLines.toVector.map(_.split(","))

  val concretenessMap = concretenessRaw.map(item =>
      item(0) -> //the word
      item(2) //it's concreteness score
    ).drop(1). //drops the headers
    toMap //make a hashMap

}