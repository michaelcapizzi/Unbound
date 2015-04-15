package TextComplexity

import scala.collection.mutable

/**
 * Created by mcapizzi on 4/7/15.
 */
class DocumentFeatures(textDocument: TextDocument) {

  //amount of dialogue
  //if first item in *sentence* array is "``" OR last item in sentence array is "''"

  def getNamedEntities = {
    val nerTuples = textDocument.lexicalTuple.map(item => (item._1, item._2._3))
    val newTuples = mutable.ListBuffer[(String, String)]()

    def loop(tuples: Vector[(String, String)]): Vector[(String, String)] = {
      if (tuples.isEmpty || tuples.tail.isEmpty) {
        newTuples.toVector
      }
      else if (tuples.head._2 != "O") {
        val ner = tuples.takeWhile(item => item._2 != "O")
        newTuples += (((ner.map(_._1).head /: ner.map(_._1).tail)(_ + " " + _), tuples.head._2))
        loop(tuples.drop(ner.length - 1).tail)
      }
      else {
        loop(tuples.tail)
      }
    }

    loop(nerTuples).distinct
  }

  def getCharacters = {
    val characterList = this.getNamedEntities.filter(_._2 == "PERSON").map(_._1)
    val buffer = mutable.Buffer[String]()                                             //to hold characters kept

    def loop(chars: Vector[String]): Vector[String] = {
      if (chars.isEmpty) buffer.toVector
      else if (chars.tail.map(_.contains(chars.head)).contains(true)) {                 //if a later item in the list contains the head
        loop(chars.tail)                                                                  //skip it
      }
      else {
        buffer += chars.head                                                            //otherwise add it to buffer
        loop(chars.tail)                                                                //continue
      }
    }

    loop(characterList.sortBy(_.length))
  }

  def numberOfCharacters = {
    this.getCharacters.length.toDouble
  }
}
