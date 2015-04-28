package TextComplexity

import edu.arizona.sista.learning.{RVFDatum, Datum}
import edu.arizona.sista.struct.{Lexicon, Counter}

/**
 * Created by mcapizzi on 4/7/15.
 */
object UsingCountersDatums {

  //make a counter
  val counter = new Counter[String]()

  //make a features list
  val list = List("a","b","C")

  val it = new Counter
  //how to use counter
  list.map(counter.incrementCount(_))


  //can manually set counter value
  counter.setCount("blah", 55d)

  val tuple = List(("z", 5d), ("y", 4d), ("x", 3d))
  tuple.map(each => counter.setCount(each._1, each._2))

  //make a label

  //use Lexicon
  val lex = new Lexicon[String]

  list.map(lex.add)



  //make the datum with those things defined in curly brackets
  val datum = new Datum[String, String] {
    val label = "good"
    def features = list
    def featuresCounter = counter
  }


  //implement Naive Bayes with this
  //make RVFDatum with items in ()
  val datum2 = new RVFDatum[String, String]("good",counter)

  datum2.getFeatureCount("a")

}
