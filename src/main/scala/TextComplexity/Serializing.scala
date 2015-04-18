package TextComplexity

import java.io.{FileReader, BufferedReader, File, PrintWriter}
import edu.arizona.sista.processors.corenlp.CoreNLPProcessor
import edu.arizona.sista.processors.{Document, DocumentSerializer}
import Importing._

/**
 * Created by mcapizzi on 4/7/15.
 */
object Serializing {

  //serializes annotation output
  def serializeAnnotation(document: TextDocument, outputFileName: String): Unit = {
    val writeToFile = new PrintWriter(new File("/home/mcapizzi/Github/Unbound/src/main/resources/annotatedText/" + outputFileName))
    val serializer = new DocumentSerializer
    val annotatedDocument = document.annotate
    annotatedDocument.map(serializer.save(_, writeToFile))
    writeToFile.close
  }


  def importSerial(fullAnnotatedFilePath: String): Vector[Document] = {
    val serial = new DocumentSerializer
    val buffer = new BufferedReader(new FileReader(fullAnnotatedFilePath))
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

  def makeDocumentFromSerial(fullAnnotatedFilePath: String, fullOriginalFilePath: String, processor: CoreNLPProcessor): TextDocument = {
    val document = importSerial(fullAnnotatedFilePath)
    val text = document.map(_.sentences.map(_.words.toVector).flatten.mkString(" "))
    val author = getAuthor(fullOriginalFilePath)
    val title = getTitleChapter(fullOriginalFilePath)._1
    val chapter = getTitleChapter(fullOriginalFilePath)._2
    val gradeLevel = getGradeLevel(fullOriginalFilePath)
    new TextDocument(text, processor, document, author, title, chapter, gradeLevel)
  }



}
