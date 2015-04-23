package TextComplexity

import java.io._
import Importing._
import Serializing._
import edu.arizona.sista.learning.{RandomForestClassifier, PerceptronClassifier, LogisticRegressionClassifier, RVFDataset}
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
                       //val featureVectorFileFolder: String = "",
                       val textToTestFilePath: String = ""
                       ) {

  val rawFile = new File(rawTextFileFolder)
  val annotatedFile = new File(annotatedTextFileFolder)
  //val featureVectorFile = new File(featureVectorFileFolder)

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

  //make features from raw text
  def makeRawFeatureClasses(wordSimilarity: Boolean) = {
    for (item <- this.importRawMakeDocuments) yield {
      val metaData = Vector((item.title, 0d), (item.gradeLevel, 0d))
      val lexical = new LexicalFeatures(item)
      val syntactic = new SyntacticFeatures(item)
      val paragraph = new ParagraphFeatures(item)

      val lexicalFeatures = lexical.makeLexicalFeatureVector
      val syntacticFeatures = if (wordSimilarity) syntactic.makeSyntacticFeatureVector else syntactic.makeSyntacticMinusSimilarityFeatureVector
      val paragraphFeatures = paragraph.makeParagraphFeatureVector

      (
        metaData,
        lexicalFeatures.slice(2, lexicalFeatures.length).asInstanceOf[Vector[(String, Double)]],          //without metadata
        syntacticFeatures.slice(2, syntacticFeatures.length).asInstanceOf[Vector[(String, Double)]],      //without metadata
        paragraphFeatures.slice(2, paragraphFeatures.length).asInstanceOf[Vector[(String, Double)]]       ///without metadata
        )
    }
  }

  /*
  attempt to only run features required but doesn't work
  //make features from Raw import
  def makeRawFeatureClasses2 = {
    for (item <- this.importRawMakeDocuments) yield {                                                   //for each document
      val metadata = Vector((item.title, ""), (item.gradeLevel, ""))

      val features = for (feature <- this.featuresToInclude) yield {                                      //for each feature in parameters
        if (feature == "lexical") {
          val lexical = new LexicalFeatures(item)
          val lexicalFeatures = lexical.makeLexicalFeatureVector
          lexicalFeatures.slice(2, lexicalFeatures.length).map(_._2.toString)          //without metadata
        } else if (feature == "syntactic") {
          val syntactic = new SyntacticFeatures(item)
          val syntacticFeatures = syntactic.makeSyntacticFeatureVector
          syntacticFeatures.slice(2, syntacticFeatures.length).map(_._2.toString)      //without metadata
        } else if (feature == "paragraph") {
          val paragraph = new ParagraphFeatures(item)
          val paragraphFeatures = paragraph.makeParagraphFeatureVector
          paragraphFeatures.slice(2, paragraphFeatures.length).map(_._2.toString)       ///without metadata
        }
      }
      features match {
        case Vector(a) => (metadata, a.asInstanceOf[Vector[(String, String)]], Vector(("","")), Vector(("", "")).asInstanceOf[Vector[(String, String)]])
        case Vector(a, b) => (metadata, a.asInstanceOf[Vector[(String, String)]], b.asInstanceOf[Vector[(String, String)]], Vector("",0d).asInstanceOf[Vector[(String, String)]])
        case Vector(a,b,c) => (metadata, a.asInstanceOf[Vector[(String, String)]], b.asInstanceOf[Vector[(String, String)]], c.asInstanceOf[Vector[(String, String)]])
      }
    }
  }
  */

  //make features from annotated import
  def makeAnnotatedFeatureClasses(wordSimilarity: Boolean) = {
    for (item <- this.importAnnotatedMakeDocuments) yield {
      val metaData = Vector((item.title, 0d), (item.gradeLevel, 0d))
      val lexical = new LexicalFeatures(item)
      val syntactic = new SyntacticFeatures(item)
      val paragraph = new ParagraphFeatures(item)

      val lexicalFeatures = lexical.makeLexicalFeatureVector
      val syntacticFeatures = if (wordSimilarity) syntactic.makeSyntacticFeatureVector else syntactic.makeSyntacticMinusSimilarityFeatureVector
      val paragraphFeatures = paragraph.makeParagraphFeatureVector

      (
        metaData,
        lexicalFeatures.slice(2, lexicalFeatures.length).asInstanceOf[Vector[(String, Double)]],          //without metadata
        syntacticFeatures.slice(2, syntacticFeatures.length).asInstanceOf[Vector[(String, Double)]],      //without metadata
        paragraphFeatures.slice(2, paragraphFeatures.length).asInstanceOf[Vector[(String, Double)]]       ///without metadata
      )
    }
  }

  def makeTestFeatureClasses(wordSimilarity: Boolean) = {
    val doc = this.importTestRawMakeDocument
    val metaData = Vector((doc.title, 0d), (doc.gradeLevel, 0d))
    val lexical = new LexicalFeatures(doc)
    val syntactic = new SyntacticFeatures(doc)
    val paragraph = new ParagraphFeatures(doc)

    val lexicalFeatures = lexical.makeLexicalFeatureVector
    val syntacticFeatures = if (wordSimilarity) syntactic.makeSyntacticFeatureVector else syntactic.makeSyntacticMinusSimilarityFeatureVector
    val paragraphFeatures = paragraph.makeParagraphFeatureVector

    (
      metaData,
      lexicalFeatures.slice(2, lexicalFeatures.length).asInstanceOf[Vector[(String, Double)]],          //without metadata
      syntacticFeatures.slice(2, syntacticFeatures.length).asInstanceOf[Vector[(String, Double)]],      //without metadata
      paragraphFeatures.slice(2, paragraphFeatures.length).asInstanceOf[Vector[(String, Double)]]       ///without metadata
    )
  }


  def convertLabel(label: String): String = {
    label match {
      case "0001" => "0"
      case "0203" => "1"
      case "0405" => "2"
      case "0608" => "3"
      case "0910" => "4"
      case "1112" => "5"
    }
  }

  def revertLabel(label: Int): String = {
    label match {
      case 0 => "0001"
      case 1 => "0203"
      case 2 => "0405"
      case 3 => "0608"
      case 4 => "0910"
      case 5 => "1112"
    }
  }


  //builds svmLight feature vector
  def buildRawFinalFeatureVector(wordSimilarity: Boolean) = {
    val allFeatureVectors = if (wordSimilarity) this.makeRawFeatureClasses(wordSimilarity = true) else this.makeRawFeatureClasses(wordSimilarity = false)
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
        convertLabel(row(1)._1) + " " + {                                         //the grade level
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

  /*
  //builds svmLight feature vector
  def buildRawFinalFeatureVector = {
    val allFeatureVectors = this.makeRawFeatureClasses
    val featureBuffer = collection.mutable.Buffer[Vector[(String, String)]]()

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

    def toSVM(buffer: collection.mutable.Buffer[Vector[(String, String)]]) = {
      for (row <- buffer) yield {
        convertLabel(row(1)._1) + " " + {                                         //the grade level
          {for (i <- 2 to row.length - 1) yield {
            (i-1).toString +                                        //the feature index
              ":" +
              row(i)._2                                             //the feature value
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
  */

  //TODO test convertLabel on just lexical and test
  //builds svmLight feature vector
  def buildAnnotatedFinalFeatureVector(wordSimilarity: Boolean) = {
    val allFeatureVectors = if (wordSimilarity) this.makeAnnotatedFeatureClasses(wordSimilarity = true) else this.makeAnnotatedFeatureClasses(wordSimilarity = false)
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
        convertLabel(row(1)._1) + " " + {                                         //the grade level
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
      //val pwTrain = new PrintWriter(new File("/home/mcapizzi/Github/Unbound/src/main/resources/featureVectors/" + outsideFolder + "/" + (i + 1).toString + ".train"))
      val pwTest = new PrintWriter(new File("/home/mcapizzi/Github/Unbound/src/main/resources/featureVectors/" + outsideFolder + "/" + insideFolder + "/" + (i + 1).toString + "/" + (i + 1).toString + ".test"))
      //val pwTest = new PrintWriter(new File("/home/mcapizzi/Github/Unbound/src/main/resources/featureVectors/" + outsideFolder + "/" + (i + 1).toString + ".test"))

      train.map(line => pwTrain.println(line))
      pwTrain.close

      test.map(line => pwTest.println(line))
      pwTest.close

    }
  }

  //TODO test
  //TODO add ensemble capability
  //TODO finish adding NaiveBayes capability
  def leaveOneOut(withEnsemble: Boolean = false) = {
    val folderName = this.featuresToInclude.mkString("_")
    val outsideFolder = new File("/home/mcapizzi/Github/Unbound/src/main/resources/featureVectors/" + folderName)         //select proper outside folder based on parameters

    val scoreList = for (model <- modelsToUse) yield {

      if (model == "naiveBayes") {                                                                              //if Naive Bayes
        val allDocs = this.importAnnotatedMakeDocuments                       //get all documents
        (
          "naiveBayes",
          for (document <- allDocs) yield {                                     //for each document...
            val test = Vector(document)                                             //make it test
            val train = allDocs.filterNot(_ == document).toVector                   //and all other docs part of train
            val nb = new NaiveBayes(train, test, Vector(), 0, 0, 0)

            /*(
              //title,
              nb.argMax,      //mlScore
              document.label  //actualScore
            )*/

          }
        )

      } else {
        val classifier = model match {                                                                          //else build the classifier
          case "logisticRegression" => new LogisticRegressionClassifier[Int, String](bias = false)
          case "perceptron" => new PerceptronClassifier[Int, String](epochs = 20, marginRatio = 1d)
          case "randomForest" => new RandomForestClassifier[Int, String](
            numTrees = 1000,
            featureSampleRatio = -0.20,
            maxTreeDepth = 4
          )
        }

        (
          model,                                                                            //classifier name
          for (insideFolder <- outsideFolder.listFiles) yield {
            //for each subfolder
            val train = insideFolder.listFiles.find(fileName => fileName.getName.contains("train")).get             //get train file
            val test = insideFolder.listFiles.find(fileName => fileName.getName.contains("test")).get               //get test file

            val trainDataSet = RVFDataset.mkDatasetFromSvmLightFormat(train.getCanonicalPath)                       //build training dataset
            val testDataSet = RVFDataset.mkDatumsFromSvmLightFormat(test.getCanonicalPath)                          //build test dataset

            classifier.train(trainDataSet)

            val titleRegex = """#(.*)""".r
            val line = Source.fromFile(test).getLines.toVector.head
            val title = titleRegex.replaceFirstIn(line, """$1""")

            (
              title,                                                                        //title
              revertLabel(classifier.classOf(testDataSet.head)),                            //mlScore
              revertLabel(testDataSet.head.label)                                           //actualScore
              )
          }
        )
      }
    }
    if (withEnsemble) {
      /*
      ////////////////ensemble - voting///////////////////////

  //put all scores in a list:
    //List(lr, simple, perceptron)

  //make collected score list
  def makeCollectedScoreList(scoreList: List[List[(String, String, String)]]): List[(String, List[String])] = {
    val titles = scoreList.map(_.map(_._1))
    for (title <- titles(0)) yield
      title ->
        scoreList.map(scores => scores.find(item => item._1 == title).get).map(_._2)
  }

  //takes a List[title, List[Scores]
 def ensembleVoting(scores: List[(String, List[String])], tieBreakerIndex: Int): List[(String, String)] = {
    for (score <- scores) yield (score._1, {
      val record = score._2.groupBy(item => item).values.toList.map(thing => thing(0) -> thing.length).toMap
      if (record.values.toList.length == scores.map(_._2).length) score._2(tieBreakerIndex)
      else record.maxBy(_._2)._1
      }
      )
  }
       */
    } else {
      scoreList
    }
  }

  /*//TODO build using Counters (see UsingCountersDatums)
  def fullTrainAndTest = {
    val featureVectorFileName = this.featuresToInclude.mkString("_") + ".master"
    val trainFile = RVFDataset.mkDatasetFromSvmLightFormat("/home/mcapizzi/Github/Unbound/src/main/resources/featureVectors/" + featureVectorFileName)

    for (model <- modelsToUse) yield {
      val classifier = model match {
        //build the classifier
        case "logisticRegression" => new LogisticRegressionClassifier[Int, String](bias = false)
        case "perceptron" => new PerceptronClassifier[Int, String](epochs = 20, marginRatio = 1d)
        case "randomForest" => new RandomForestClassifier[Int, String](
          numTrees = 1000,
          featureSampleRatio = -0.20,
          maxTreeDepth = 4
        )
      }
      classifier.train(trainFile)

      val testFilesRaw = new File(textToTestFilePath)
      val testDocs = for (doc <- testFilesRaw.listFiles) yield {
        //make test document
      }
      //build SVM light file
      /*

      (

       */
    }
  }*/




}
