package TextComplexity

import Importing._
import edu.arizona.sista.processors.corenlp.CoreNLPProcessor
import edu.stanford.nlp.trees.Tree

/**
 * Created by mcapizzi on 4/7/15.
 */
object Testing {

  val processor = new CoreNLPProcessor()

  val text = importParagraphs("/home/mcapizzi/Github/Unbound/src/main/resources/rawText/0001AL_OwlAndMoon.txt")

  val docTest = importParagraphs("/home/mcapizzi/Github/Unbound/src/main/resources/rawText/0001AL_OwlAndMoon.txt").map(processor.mkDocument)

  val doc = makeDocument("/home/mcapizzi/Github/Unbound/src/main/resources/rawText/0001AL_OwlAndMoon.txt", processor)

  //docTest.map(processor.annotate)

  val trees = docTest.map(_.sentences.map(_.syntacticTree.toString).map(Tree.valueOf))

}
