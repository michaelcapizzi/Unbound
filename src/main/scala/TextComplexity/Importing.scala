package TextComplexity

import java.io.{BufferedReader, FileReader}

import edu.arizona.sista.processors.{DocumentSerializer, Document}
import edu.arizona.sista.processors.corenlp.CoreNLPProcessor
import scala.StringBuilder
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

  def getGradeLevel(filePath: String): String = {
    val gradeLevelRegex = """.*\/([0-9]+)[A-Z]+_.*""".r
    gradeLevelRegex.replaceFirstIn(filePath, """$1""")
  }

  def makeDocument(filePath: String, processor: CoreNLPProcessor): TextDocument = {
    val text = importParagraphs(filePath)
    val document = text.map(processor.mkDocument)
    val author = getAuthor(filePath)
    val title = getTitleChapter(filePath)._1
    val chapter = getTitleChapter(filePath)._2
    val gradeLevel = getGradeLevel(filePath)
    new TextDocument(text, processor, document, author, title, chapter, gradeLevel)
  }

  def importSerial(shortAnnotatedFilePath: String): Vector[Document] = {
    val serial = new DocumentSerializer
    val buffer = new BufferedReader(new FileReader("/home/mcapizzi/Github/Unbound/src/main/resources/annotatedText/" + shortAnnotatedFilePath))
    var line = buffer.readLine
    val stringBuffer = new StringBuilder
    val docBuffer = collection.mutable.Buffer[String]()

    while (line != null) {
      if (line == "EOD") {
        //println("END OF DOC")
        stringBuffer.append(line)
        docBuffer += stringBuffer.toString
        //line = buffer.readLine
        stringBuffer.clear
        line = buffer.readLine
      } else {
        //println(line)
        stringBuffer.append(line + "\n")
        line = buffer.readLine
      }
    }
    docBuffer.map(paragraph =>
      serial.load(paragraph)).toVector
  }

  def makeDocumentFromSerial(shortAnnotatedFilePath: String, shortOriginalFilePath: String, processor: CoreNLPProcessor): TextDocument = {
    val document = importSerial(shortAnnotatedFilePath)
    val text = document.map(_.sentences.map(_.words.toVector).flatten.mkString(" "))
    val author = getAuthor("/home/mcapizzi/Github/Unbound/src/main/resources/rawText/" + shortOriginalFilePath)
    val title = getTitleChapter("/home/mcapizzi/Github/Unbound/src/main/resources/rawText/" + shortOriginalFilePath)._1
    val chapter = getTitleChapter("/home/mcapizzi/Github/Unbound/src/main/resources/rawText/" + shortOriginalFilePath)._2
    val gradeLevel = getGradeLevel("/home/mcapizzi/Github/Unbound/src/main/resources/rawText/" + shortOriginalFilePath)
    new TextDocument(text, processor, document, author, title, chapter, gradeLevel)
  }



}
