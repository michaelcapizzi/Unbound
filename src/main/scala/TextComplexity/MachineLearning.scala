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


  val rawFile = new File(rawTextFileFolder)
  val annotatedFile = new File(annotatedTextFileFolder)
  val featureVectorFile = new File(featureVectorFileFolder)

  //load raw files from folder and make TextDocument for each
  def importRawMakeDocuments = {
    val textDocuments = rawFile.listFiles.map(item =>                       //get a list of files and for each
      makeDocument(item.getCanonicalPath, processor)).toVector                //make document turn into vector
    textDocuments.map(_.annotate)                                             //annotate all documents
    textDocuments
  }

  //TODO rewrite using FIND and REGEX instead of ZIP
  //load annotated files from folder and make TextDocument for each
  def importAnnotatedMakeDocuments = {
    val tuple = rawFile.listFiles zip annotatedFile.listFiles                                           //get a list of raw files and annotated files
    tuple.map(item =>                                                                                     //for each
      makeDocumentFromSerial(item._1.getCanonicalPath, item._2.getCanonicalPath, processor)).toVector       //import annotation
  }

  //TODO build and test
  def importTestRawMakeDocuments = {
    //
  }

  //make features from Raw import
  def makeRawFeatureClasses = {
    for (item <- this.importRawMakeDocuments) yield {
      val metaData = Vector((item.title, 0d), (item.gradeLevel, 0d))
      val lexical = new LexicalFeatures(item)
      val syntactic = new SyntacticFeatures(item)
      val paragraph = new ParagraphFeatures(item)

      val lexicalFeatures = lexical.makeLexicalFeatureVector
      val syntacticFeatures = syntactic.makeSyntacticFeatureVector
      val paragraphFeatures = paragraph.makeParagraphFeatureVector

      (
        metaData,
        lexicalFeatures.slice(2, lexicalFeatures.length - 1).asInstanceOf[Vector[(String, Double)]],          //without metadata
        syntacticFeatures.slice(2, syntacticFeatures.length - 1).asInstanceOf[Vector[(String, Double)]],      //without metadata
        paragraphFeatures.slice(2, paragraphFeatures.length - 1).asInstanceOf[Vector[(String, Double)]]       ///without metadata
      )
    }
  }

  //TODO test
  //make features from annotated import
  def makeAnnotatedFeatureClasses = {
    for (item <- this.importAnnotatedMakeDocuments) yield {
      val metaData = Vector((item.title, 0d), (item.gradeLevel, 0d))
      val lexical = new LexicalFeatures(item)
      val syntactic = new SyntacticFeatures(item)
      val paragraph = new ParagraphFeatures(item)

      val lexicalFeatures = lexical.makeLexicalFeatureVector
      val syntacticFeatures = syntactic.makeSyntacticFeatureVector
      val paragraphFeatures = paragraph.makeParagraphFeatureVector

      (
        metaData,
        lexicalFeatures.slice(2, lexicalFeatures.length - 1).asInstanceOf[Vector[(String, Double)]],          //without metadata
        syntacticFeatures.slice(2, syntacticFeatures.length - 1).asInstanceOf[Vector[(String, Double)]],      //without metadata
        paragraphFeatures.slice(2, paragraphFeatures.length - 1).asInstanceOf[Vector[(String, Double)]]       ///without metadata
      )
    }
  }

  //TODO build and test
  def makeTestFeatureClasses = {
    //
  }


  //builds svmLight feature vector
  def buildRawFinalFeatureVector = {
    val allFeatureVectors = this.makeRawFeatureClasses
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

    val svmFile = toSVM(featureBuffer)
    val pw = new PrintWriter(new File("/home/mcapizzi/Github/Unbound/src/main/resources/featureVectors/" + featureVectorFileFolder))
    svmFile.map(line => pw.println(line))
    pw.close
  }

  //TODO figure out how to do file folder and file naming for svm light file
  //builds svmLight feature vector
  def buildAnnotatedFinalFeatureVector = {
    val allFeatureVectors = this.makeAnnotatedFeatureClasses
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

    val svmFile = toSVM(featureBuffer)
    val pw = new PrintWriter(new File("/home/mcapizzi/Github/Unbound/src/main/resources/featureVectors/" + featureVectorFileFolder))
    svmFile.map(line => pw.println(line))
    pw.close
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
