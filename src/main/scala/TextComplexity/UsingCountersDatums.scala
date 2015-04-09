package TextComplexity

import edu.arizona.sista.learning.{RVFDatum, Datum}
import edu.arizona.sista.struct.Counter

/**
 * Created by mcapizzi on 4/7/15.
 */
object UsingCountersDatums {

  //make a counter
  val counter = new Counter[String]()

  //make a features list
  val list = List("a","b","C")

  //how to use counter
  list.map(counter.incrementCount(_))

  //make a label

  //make the datum with those things defined in curly brackets
  val datum = new Datum[String, String] {
    val label = "good"
    def features = list
    def featuresCounter = counter
  }


  //TODO implement Naive Bayes with this
  //make RVFDatum with items in ()
  val datum2 = new RVFDatum[String, String]("good",counter)

  datum2.getFeatureCount("a")

}