package TextComplexity

import java.io._
import Importing._
import Serializing._
import edu.arizona.sista.processors.corenlp.CoreNLPProcessor
import scala.collection.mutable.Buffer
import scala.collection.parallel.mutable
import scala.io.Source

/**
 * Created by mcapizzi on 4/15/15.
 */
class MachineLearning(
                       val annotatedTextFileFolder: String,         //required
                       val processor: CoreNLPProcessor,             //required
                       val featuresToInclude: Vector[String],       //required
                       val modelsToUse: Vector[String],             //required
                       val rawTextFileFolder: String = "",
                       val featureVectorFileFolder: String = ""
                       ) {

  //TODO test all methods

  val rawFile = new File(rawTextFileFolder)
  val annotatedFile = new File(annotatedTextFileFolder)
  val featureVectorFile = new File(featureVectorFileFolder)

  //load raw files from folder and make TextDocument for each
  def importRawMakeDocuments = {
    val textDocuments = rawFile.listFiles.map(item =>
      makeDocument(item.getCanonicalPath, processor)).toVector
    textDocuments.map(_.annotate)
    textDocuments
  }

  //load annotated files from folder and make TextDocument for each
  def importAnnotatedMakeDocuments = {
    val tuple = rawFile.listFiles zip annotatedFile.listFiles
    tuple.map(item =>
      makeDocumentFromSerial(item._1.getCanonicalPath, item._2.getCanonicalPath, processor)).toVector
  }

  //make features from Raw import
  def makeRawFeatureClasses = {
    for (item <- this.importRawMakeDocuments) yield {
      for (featureClass <- featuresToInclude) yield {
        val lexical = new LexicalFeatures(item)
        val syntactic = new SyntacticFeatures(item)
        val paragraph = new ParagraphFeatures(item)
        (lexical.makeLexicalFeatureVector, syntactic.makeSyntacticFeatureVector, paragraph.makeParagraphFeatureVector)
      }
    }
  }

  //make features from annotated import
  def makeAnnotatedFeatureVectors = {
    for (item <- this.importAnnotatedMakeDocuments) yield {
      for (featureClass <- featuresToInclude) yield {
        val lexical = new LexicalFeatures(item)
        val syntactic = new SyntacticFeatures(item)
        val paragraph = new ParagraphFeatures(item)
      }
      Map("lexical" -> lexical.makeLexicalFeatureVector, "syntactic" -> syntactic.makeSyntacticFeatureVector, "paragraph" -> paragraph.makeParagraphFeatureVector)
    }
  }

  /*//concatenate features from class parameter
    //build into SVM light format
  def buildFinalFeatureVector {
    val allFeatureVectors = this.makeAnnotatedFeatureVectors
    val featureBuffer = collection.mutable.Buffer[(String, Double)]()

    for (item <- allFeatureVectors) yield {
      if (featuresToInclude.contains("lexical")) {
        featureBuffer += item("lexical")
      } else if (featuresToInclude.contains("lexical") && featuresToInclude.contains("syntactic")) {
        featureBuffer += item("lexical")
        featureBuffer += item("syntactic")
      } else if (featuresToInclude.contains("lexical") && featuresToInclude.contains("syntactic") && featuresToInclude.contains("paragraph"))
        featureBuffer += item("lexical")
        featureBuffer += item("syntactic")
        featureBuffer += item("paragraph")
    }
    //concatenate based on parameter
    //build into one SVM light file with all files
      val pw = new PrintWriter(new File("/home/mcapizzi/Github/Unbound/src/main/resources/featureVectors/" + featureVectorFileFolder))
      featureVector.map(line <- pw.println(line))
      pw.close
      featureVector
  }*/

  def importFinalFeatureVector = {
    //featureVectorFile.listFiles.map(item => )
  }

  def leaveOneOut = {
    //take SVM light file
      //generate all possible combinations
    //HOW TO HANDLE parameter selections?
  }

  def fullTrain = {
    //take SVM light file as is
    //HOW TO HANDLE paramter selections?
  }


}
