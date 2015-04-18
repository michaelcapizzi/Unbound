package TextComplexity

import java.io._
import Importing._
import Serializing._
import edu.arizona.sista.processors.corenlp.CoreNLPProcessor
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
        (lexical.makeLexicalFeatureVector, syntactic.makeSyntacticFeatureVector, paragraph.makeParagraphFeatureVector)
      }
    }
  }

  //concatenate features from class parameter
    //build into SVM light format
  def buildFinalFeatureVector(export: Boolean) {
    //concatenate
    //build into one SVM light file with all files
    if (export) {
      //printwriter to file folder labeled in Vector parameter
        //make a folder named parameters
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
    //HOW TO HANDLE paramter selections?
  }


}
