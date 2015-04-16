package TextComplexity

import Importing._
import edu.arizona.sista.processors.corenlp.CoreNLPProcessor
import edu.arizona.sista.discourse.rstparser.DiscourseTree


/**
 * Created by mcapizzi on 4/7/15.
 */
object Testing {

  val processor = new CoreNLPProcessor()
  val processorTest = new CoreNLPProcessor()
  val processorDiscourse = new CoreNLPProcessor(withDiscourse = true)
  val processorDiscourseTest = new CoreNLPProcessor(withDiscourse = true)

  //testing outside of class
  val text = importParagraphs("/home/mcapizzi/Github/Unbound/src/main/resources/rawText/0001AL_OwlAndMoon.txt")

  val docTest = importParagraphs("/home/mcapizzi/Github/Unbound/src/main/resources/rawText/0001AL_OwlAndMoon.txt").map(processorTest.mkDocument)

  val discourseTestDoc = importParagraphs("/home/mcapizzi/Github/Unbound/src/main/resources/rawText/0203ES_CowgirlKate.txt").map(processorDiscourseTest.mkDocument)

  /*

  val discourseTrees = discourseTestDoc.map(_.discourseTree)

  val discourseTreesRaw = discourseTrees.map(_.           //get discourse trees
      get.toString.split("\n").map(_.       //split by line
      trim)).map(paragraph =>               //remove whitespace
      paragraph.filterNot(_.                //remove text, keeping details
        startsWith("TEXT")))

  val relationsRegex = """(.+)( \(\w+\))?""".r

  */

  //testing inside class
  val discourseDoc = makeDocument("/home/mcapizzi/Github/Unbound/src/main/resources/rawText/0203ES_CowgirlKate.txt", processorDiscourse)
  val discourseDoc2 = makeDocument("/home/mcapizzi/Github/Unbound/src/main/resources/rawText/1112JL_Namesake.txt", processorDiscourse)

  val doc = makeDocument("/home/mcapizzi/Github/Unbound/src/main/resources/rawText/0001AL_OwlAndMoon.txt", processor)

  //docTest.map(processor.annotate)


}
