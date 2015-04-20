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
                       val featureVectorFileFolder: String = "",
                       val textToTestFilePath: String = ""
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

  def importTestRawMakeDocuments = {
    //
  }

  //make features from Raw import
  def makeRawFeatureClasses = {
    for (item <- this.importRawMakeDocuments) yield {
      val metaData = (item.title, item.gradeLevel)
      val lexical = new LexicalFeatures(item)
      val syntactic = new SyntacticFeatures(item)
      val paragraph = new ParagraphFeatures(item)
      (metaData, lexical.makeLexicalFeatureVector, syntactic.makeSyntacticFeatureVector, paragraph.makeParagraphFeatureVector)
    }
  }

  //make features from annotated import
  def makeAnnotatedFeatureVectors = {
    for (item <- this.importAnnotatedMakeDocuments) yield {
      val metaData = (item.title, item.gradeLevel)
      val lexical = new LexicalFeatures(item)
      val syntactic = new SyntacticFeatures(item)
      val paragraph = new ParagraphFeatures(item)
      (metaData, lexical.makeLexicalFeatureVector, syntactic.makeSyntacticFeatureVector, paragraph.makeParagraphFeatureVector)
    }
  }

  def makeTestFeatureClasses = {
    //
  }

    //concatenate features from class parameter
    //build into SVM light format
  def buildFinalFeatureVector = {
    val allFeatureVectors = this.makeAnnotatedFeatureVectors
    val featureBuffer = collection.mutable.Buffer[Vector[(String, Any)]]()

    for (item <- allFeatureVectors) yield {
      if (featuresToInclude == Vector("lexical")) {
        featureBuffer += item._2
      } else if (featuresToInclude == Vector("syntactic")) {
        featureBuffer += item._3
      } else if (featuresToInclude == Vector("paragraph")) {
        featureBuffer += item._4
      } else if (featuresToInclude == Vector("lexical", "syntactic")) {
        featureBuffer += item._2
        featureBuffer += item._3.slice(2, item._2.length - 1)
      } else if (featuresToInclude == Vector("syntactic", "paragraph")) {
        featureBuffer += item._3
        featureBuffer += item._4.slice(2, item._2.length - 1)
      } else if (featuresToInclude == Vector("lexical", "paragraph")) {
        featureBuffer += item._2
        featureBuffer += item._4.slice(2, item._2.length - 1)
      } else if (featuresToInclude == Vector("lexical", "syntactic", "paragraph")) {
        featureBuffer += item._2
        featureBuffer += item._3.slice(2, item._2.length - 1)
        featureBuffer += item._4.slice(2, item._2.length - 1)
      }
    }
    val svmFile = toSVM(featureBuffer.flatten)
    //build into one SVM light file with all files
      val pw = new PrintWriter(new File("/home/mcapizzi/Github/Unbound/src/main/resources/featureVectors/" + featureVectorFileFolder))
      svmFile.map(line => pw.println(line))
      pw.close

      def toSVM(buffer: collection.mutable.Buffer[(String, Any)]) = {
        for (row <- buffer) yield {
          buffer.head._2 + " " + {
            (for (i <- 1 to buffer.length - 3) yield {
              i.toString + ":" + buffer(i).toString).mkString(" ")
            })
          } + "#" + buffer.head._1
        }
      }
  }

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
    //HOW TO HANDLE parameter selections?
  }

  def testOne = {
    //
  }

}
