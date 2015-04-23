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

  //load annotated files from folder and make TextDocument for each
  def importAnnotatedMakeDocuments = {
    val tuple = for (file <- rawFile.listFiles) yield {                                                                                           //get a list of raw files and annotated files
      val fileName = file.getName.dropRight(file.getName.length - file.getName.indexOf("."))
      (                                                                                                                                             //make tuple of
        file,                                                                                                                                         //rawFile
        annotatedFile.listFiles.find(item => item.getName.contains(fileName)).get                                                                     //annotatedFile
      )
    }
    tuple.map(item =>                                                                                                                                   //for each
      makeDocumentFromSerial(item._2.getCanonicalPath, item._1.getCanonicalPath, processor)).toVector                                                     //import annotationFile
  }

  def importTestRawMakeDocument = {
    val doc = makeDocument(textToTestFilePath, processor)
    doc.annotate
    doc
  }

  //TODO make if/then to match parameters
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
        lexicalFeatures.slice(2, lexicalFeatures.length).asInstanceOf[Vector[(String, Double)]],          //without metadata
        syntacticFeatures.slice(2, syntacticFeatures.length).asInstanceOf[Vector[(String, Double)]],      //without metadata
        paragraphFeatures.slice(2, paragraphFeatures.length).asInstanceOf[Vector[(String, Double)]]       ///without metadata
      )
    }
  }

  //TODO make if/then to match parameters
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
        lexicalFeatures.slice(2, lexicalFeatures.length).asInstanceOf[Vector[(String, Double)]],          //without metadata
        syntacticFeatures.slice(2, syntacticFeatures.length).asInstanceOf[Vector[(String, Double)]],      //without metadata
        paragraphFeatures.slice(2, paragraphFeatures.length).asInstanceOf[Vector[(String, Double)]]       ///without metadata
      )
    }
  }

  //TODO make if/then to match parameters
  def makeTestFeatureClasses = {
    val doc = this.importTestRawMakeDocument
    val metaData = Vector((doc.title, 0d), (doc.gradeLevel, 0d))
    val lexical = new LexicalFeatures(doc)
    val syntactic = new SyntacticFeatures(doc)
    val paragraph = new ParagraphFeatures(doc)

    val lexicalFeatures = lexical.makeLexicalFeatureVector
    val syntacticFeatures = syntactic.makeSyntacticFeatureVector
    val paragraphFeatures = paragraph.makeParagraphFeatureVector

    (
      metaData,
      lexicalFeatures.slice(2, lexicalFeatures.length).asInstanceOf[Vector[(String, Double)]],          //without metadata
      syntacticFeatures.slice(2, syntacticFeatures.length).asInstanceOf[Vector[(String, Double)]],      //without metadata
      paragraphFeatures.slice(2, paragraphFeatures.length).asInstanceOf[Vector[(String, Double)]]       ///without metadata
    )
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
          {for (i <- 2 to row.length - 1) yield {
            (i-1).toString +                                        //the feature index
              ":" +
              row(i)._2.toString                                    //the feature value
          }}.mkString(" ")
        } + " #" + row.head._1                                      //the title
      }
    }

    val svmFile = toSVM(featureBuffer)
    val featureVectorFileName = this.featuresToInclude.mkString("_") + ".master"
    val pw = new PrintWriter(new File("/home/mcapizzi/Github/Unbound/src/main/resources/featureVectors/" + featureVectorFileName))
    svmFile.map(line => pw.println(line))
    pw.close
  }

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
          {for (i <- 2 to row.length - 1) yield {
            (i-1).toString +                                        //the feature index
              ":" +
              row(i)._2.toString                                    //the feature value
          }}.mkString(" ")
        } + " #" + row.head._1                                      //the title
      }
    }

    val svmFile = toSVM(featureBuffer)
    val featureVectorFileName = this.featuresToInclude.mkString("_") + ".master"
    val pw = new PrintWriter(new File("/home/mcapizzi/Github/Unbound/src/main/resources/featureVectors/" + featureVectorFileName))
    svmFile.map(line => pw.println(line))
    pw.close
  }

  //TODO test
  def buildLeaveOneOutSVMFiles = {
    val featureVectorFileName = this.featuresToInclude.mkString("_") + ".master"                                                        //find .master file based on parameters
    val folderName = featureVectorFileName.dropRight(7)                                                                                 //name folder after parameters
    val outsideFolder = new File("/home/mcapizzi/Github/Unbound/src/main/resources/featureVectors/" + folderName)                           //make new folder
    outsideFolder.mkdir()                                                                                                                   //create directory

    for (i <- 0 to Source.fromFile("/home/mcapizzi/Github/Unbound/src/main/resources/featureVectors/" + featureVectorFileName).getLines.size - 1) {       //set for indexes

      val test = Source.fromFile("/home/mcapizzi/Github/Unbound/src/main/resources/featureVectors/" + featureVectorFileName).getLines.toStream(i)                       //the line for testing
      val trainBeforeTest = Source.fromFile("/home/mcapizzi/Github/Unbound/src/main/resources/featureVectors/" + featureVectorFileName).getLines.toStream.take(i)       //lines for training BEFORE testing line
      val trainAfterTest = Source.fromFile("/home/mcapizzi/Github/Unbound/src/main/resources/featureVectors/" + featureVectorFileName).getLines.toStream.drop(i + 1)    //lines for training AFTER testing line
      val train = (trainBeforeTest ++ trainAfterTest).toVector                                                                                                          //concatenated training lines

      val insideFolder = new File("/home/mcapizzi/Github/Unbound/src/main/resources/featureVectors/" + outsideFolder + "/" + (i + 1).toString)                                                //name of inside folder
      insideFolder.mkdir()

      val pwTrain = new PrintWriter(new File("/home/mcapizzi/Github/Unbound/src/main/resources/featureVectors/" + outsideFolder + "/" + insideFolder + "/" + (i + 1).toString + "/" + (i + 1).toString + ".train"))
      val pwTest = new PrintWriter(new File("/home/mcapizzi/Github/Unbound/src/main/resources/featureVectors/" + outsideFolder + "/" + insideFolder + "/" + (i + 1).toString + "/" + (i + 1).toString + ".test"))

      train.map(line => pwTrain.println(line))
      pwTrain.close

      test.map(line => pwTest.println(line))
      pwTest.close

    }
  }

  def leaveOneOut = {
    //import files from featureVectorFileFolder
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
