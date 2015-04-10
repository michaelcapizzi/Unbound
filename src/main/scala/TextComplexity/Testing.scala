package TextComplexity

import Importing._
import edu.arizona.sista.processors.corenlp.CoreNLPProcessor
import edu.stanford.nlp.trees.Tree
import edu.arizona.sista.discourse.rstparser.DiscourseTree

/**
 * Created by mcapizzi on 4/7/15.
 */
object Testing {

  val processorTest = new CoreNLPProcessor()
  val processorDiscourse = new CoreNLPProcessor(withDiscourse = true)
  val processorDiscourseTest = new CoreNLPProcessor(withDiscourse = true)

  //testing outside of class
  val text = importParagraphs("/home/mcapizzi/Github/Unbound/src/main/resources/rawText/0001AL_OwlAndMoon.txt")

  val docTest = importParagraphs("/home/mcapizzi/Github/Unbound/src/main/resources/rawText/0001AL_OwlAndMoon.txt").map(processorTest.mkDocument)

  val discourseTestDoc = importParagraphs("/home/mcapizzi/Github/Unbound/src/main/resources/rawText/0203ES_CowgirlKate.txt").map(processorDiscourseTest.mkDocument)


  //testing inside class
  val discourseDoc = makeDocument("/home/mcapizzi/Github/Unbound/src/main/resources/rawText/0203ES_CowgirlKate.txt", processorDiscourse)

  val doc = makeDocument("/home/mcapizzi/Github/Unbound/src/main/resources/rawText/0001AL_OwlAndMoon.txt", processorTest)

  //docTest.map(processor.annotate)




  //discourseTestDoc(0).discourseTree


}
