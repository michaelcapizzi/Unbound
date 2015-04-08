package TextComplexity

import edu.arizona.sista.processors.corenlp.CoreNLPProcessor
import scala.collection.mutable
import scala.io.Source

/**
 * Created by mcapizzi on 4/5/15.
 */
object Importing {

  def importParagraphs(filePath: String): Vector[String] = {
    val finalBuffer = mutable.ListBuffer[String]()
    val insideBuffer = mutable.ListBuffer[String]()
    for (line <- Source.fromFile(filePath).getLines.filterNot(_.startsWith("%"))) {       //skip metadata lines
      if (!line.isEmpty && line != null) insideBuffer += line             //at each blank line, it starts a new "List" to indicate a new paragraph
      else if (line.isEmpty || line == null) {
        finalBuffer += insideBuffer.mkString(" ")                              //add last paragraph
        insideBuffer.clear                                                //return full document
      }
    }
    finalBuffer += insideBuffer.mkString(" ")
    finalBuffer.toVector.filterNot(_.isEmpty)
  }

  def getAuthor(filePath: String): String = {
    val authorRegex = """%(.*)""".r
    val line = Source.fromFile(filePath).getLines.take(1).next
    authorRegex.replaceFirstIn(line, """$1""")
  }

  def getTitleChapter(filePath: String): (String, String) = {
    val titleChapterRegex = """%%(.*)-([0-9]+)""".r
    val titleRegex = """%%(.*)""".r
    val line = Source.fromFile(filePath).getLines.take(2).drop(1).next

    line match {
      case titleChapterRegex(name, value) => (name, value)
      case _ => (titleRegex.replaceFirstIn(line, """$1"""), "00")
    }
  }

  def makeDocument(filePath: String, processor: CoreNLPProcessor): TextDocument = {
    val text = importParagraphs(filePath)
    val document = text.map(processor.mkDocument)
    val author = getAuthor(filePath)
    val title = getTitleChapter(filePath)._1
    val chapter = getTitleChapter(filePath)._2
    new TextDocument(text, processor, document, author, title, chapter)
  }

  //TODO build method to import .csv and generate Map


}
