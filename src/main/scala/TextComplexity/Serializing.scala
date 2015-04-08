package TextComplexity

import java.io.{File, PrintWriter}
import edu.arizona.sista.processors.DocumentSerializer

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

  //TODO build method to retrieve from file


}
