package TextComplexity

import Importing._
import edu.arizona.sista.processors.corenlp.CoreNLPProcessor

/**
 * Created by mcapizzi on 4/7/15.
 */
object Testing {

  val processor = new CoreNLPProcessor()

  val docTest = importParagraphs("/home/mcapizzi/Github/Unbound/src/main/resources/rawText/0001AL_OwlAndMoon.txt").map(processor.mkDocumentFromSentences(_))

  val doc = makeDocument("/home/mcapizzi/Github/Unbound/src/main/resources/rawText/0001AL_OwlAndMoon.txt", processor)

}
