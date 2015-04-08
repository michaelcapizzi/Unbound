package TextComplexity

import scala.io.Source

/**
 * Created by mcapizzi on 4/8/15.
 */
object Concreteness {

  //TODO build method to import .csv and generate Map
  val concretenessRaw = Source.fromFile("/home/mcapizzi/Github/Unbound/src/main/resources/concreteness_data.csv").getLines.toVector.map(_.split(","))

  val concretenessMap = concretenessRaw.map(item =>
    item(0) ->      //the word
    item(2)         //it's concreteness score
    )
  .drop(1).         //drops the headers
  toMap             //make a hashMap
}
