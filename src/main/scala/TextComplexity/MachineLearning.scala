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
      (
        metaData,
        lexical.makeLexicalFeatureVector.slice(2, lexical.makeLexicalFeatureVector.length - 1),             //without metadata
        syntactic.makeSyntacticFeatureVector.slice(2, syntactic.makeSyntacticFeatureVector.length - 1),     //without metadata
        paragraph.makeParagraphFeatureVector.slice(2, paragraph.makeParagraphFeatureVector.length - 1)      //without metadata
      )
    }
  }

  //make features from annotated import
  def makeAnnotatedFeatureVectors = {
    for (item <- this.importAnnotatedMakeDocuments) yield {
      val metaData = Vector((item.title, 0d), (item.gradeLevel, 0d))
      val lexical = new LexicalFeatures(item)
      val syntactic = new SyntacticFeatures(item)
      val paragraph = new ParagraphFeatures(item)
      (
        metaData,
        lexical.makeLexicalFeatureVector.slice(2, lexical.makeLexicalFeatureVector.length - 1).asInstanceOf[Vector[(String, Double)]],             //without metadata
        syntactic.makeSyntacticFeatureVector.slice(2, syntactic.makeSyntacticFeatureVector.length - 1).asInstanceOf[Vector[(String, Double)]],     //without metadata
        paragraph.makeParagraphFeatureVector.slice(2, paragraph.makeParagraphFeatureVector.length - 1).asInstanceOf[Vector[(String, Double)]]      //without metadata
      )
    }
  }

  def makeTestFeatureClasses = {
    //
  }

    //concatenate features from class parameter
    //build into SVM light format
  def buildFinalFeatureVector = {
    val allFeatureVectors = this.makeAnnotatedFeatureVectors
    val featureBuffer = collection.mutable.Buffer[Vector[(String, Double)]]()

    for (item <- allFeatureVectors) yield {
      if (featuresToInclude == Vector("lexical")) {
        featureBuffer += item._1 ++ item._2
      } else if (featuresToInclude == Vector("syntactic")) {
        featureBuffer += item._1 ++ item._3
      } else if (featuresToInclude == Vector("paragraph")) {
        featureBuffer += item._1 ++ item._4
      } else if (featuresToInclude == Vector("lexical", "syntactic")) {
        featureBuffer += item._1 ++ item._2 ++ item._3
      } else if (featuresToInclude == Vector("syntactic", "paragraph")) {
        featureBuffer += item._1 ++ item._3 ++ item._4
      } else if (featuresToInclude == Vector("lexical", "paragraph")) {
        featureBuffer += item._1 ++ item._2 ++ item._4
      } else if (featuresToInclude == Vector("lexical", "syntactic", "paragraph")) {
        featureBuffer += item._1 ++ item._2 ++ item._3 ++ item._4
      }
    }
    val svmFile = toSVM(featureBuffer)
    //build into one SVM light file with all files
      val pw = new PrintWriter(new File("/home/mcapizzi/Github/Unbound/src/main/resources/featureVectors/" + featureVectorFileFolder))
      svmFile.map(line => pw.println(line))
      pw.close

      def toSVM(buffer: collection.mutable.Buffer[Vector[(String, Double)]]) = {
        for (row <- buffer) yield {
          row(1)._1 + " " + {                                         //the grade level
            for (i <- 2 to row.length - 1) yield {
              " " + (i-1).toString +                                  //the feature index
              ":" +
              row(i).toString                                         //the feature value
            }
          } + "#" + row.head._1                                       //the title
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
