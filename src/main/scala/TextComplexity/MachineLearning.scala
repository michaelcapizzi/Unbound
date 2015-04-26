package TextComplexity

import java.io._
import Importing._
import Serializing._
import edu.arizona.sista.learning._
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
                       val numberOfClasses: Int,                    //required
                       val rawTextFileFolder: String = "",
                       val textToTestFilePath: String = ""
                       ) {

  //TODO reset classifier with only three classes

  //val scores = Source.fromFile("/home/mcapizzi/Github/Unbound/src/main/resources/lexile").getLines.toList.map(_.split(",")).map(_.map(_.trim)).map(each => (each.head, each(1), each(2))).toVector

  val rawFile = new File(rawTextFileFolder)
  val annotatedFile = new File(annotatedTextFileFolder)

  //load raw files from folder and make TextDocument for each
  def importRawMakeDocuments = {
    val textDocuments = rawFile.listFiles.map(item =>                       //get a list of files and for each
      makeDocument(item.getCanonicalPath, processor)).toVector                //make document turn into vector
    textDocuments.map(_.annotate)                                             //annotate all documents
    textDocuments
  }

  //load annotated files from folder and make TextDocument for each
  def importAnnotatedMakeDocuments = {
    val tuple = for (file <- rawFile.listFiles) yield {                                        //get a list of raw files and annotated files
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

      val lexicalFeatures = if (wordSimilarity) lexical.makeLexicalFeatureVector else lexical.makeLexicalFeatureMinusWordSimilarityVector
      //val lexicalFeatures = lexical.makeLexicalFeatureVector
      //val syntacticFeatures = if (wordSimilarity) syntactic.makeSyntacticFeatureVector else syntactic.makeSyntacticMinusSimilarityFeatureVector
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

      val lexicalFeatures = if (wordSimilarity) lexical.makeLexicalFeatureVector else lexical.makeLexicalFeatureMinusWordSimilarityVector
      //val lexicalFeatures = lexical.makeLexicalFeatureVector
      //val syntacticFeatures = if (wordSimilarity) syntactic.makeSyntacticFeatureVector else syntactic.makeSyntacticMinusSimilarityFeatureVector
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

  def makeTestFeatureClasses(wordSimilarity: Boolean) = {
    val doc = this.importTestRawMakeDocument
    val metaData = Vector((doc.title, 0d), (doc.gradeLevel, 0d))
    val lexical = new LexicalFeatures(doc)
    val syntactic = new SyntacticFeatures(doc)
    val paragraph = new ParagraphFeatures(doc)

    val lexicalFeatures = if (wordSimilarity) lexical.makeLexicalFeatureVector else lexical.makeLexicalFeatureMinusWordSimilarityVector
    //val lexicalFeatures = lexical.makeLexicalFeatureVector
    //val syntacticFeatures = if (wordSimilarity) syntactic.makeSyntacticFeatureVector else syntactic.makeSyntacticMinusSimilarityFeatureVector
    val syntacticFeatures = syntactic.makeSyntacticFeatureVector
    val paragraphFeatures = paragraph.makeParagraphFeatureVector

    (
      metaData,
      lexicalFeatures.slice(2, lexicalFeatures.length).asInstanceOf[Vector[(String, Double)]],          //without metadata
      syntacticFeatures.slice(2, syntacticFeatures.length).asInstanceOf[Vector[(String, Double)]],      //without metadata
      paragraphFeatures.slice(2, paragraphFeatures.length).asInstanceOf[Vector[(String, Double)]]       ///without metadata
    )
  }

  def convertLabel(label: String): String = {
    if (numberOfClasses == 5) {
      label match {
        case "0001" => "0"
        case "0203" => "1"
        case "0405" => "2"
        case "0608" => "3"
        case "0910" => "4"
        case "1112" => "5"
      }
    } else {
      label match {
        case ("0001" | "0203" | "0405") => "0"
        case "0608" => "1"
        case ("0910" | "1112") => "2"
      }
    }
  }

  def revertLabel(label: Int): String = {
    if (numberOfClasses == 5) {
      label match {
        case 0 => "0001"
        case 1 => "0203"
        case 2 => "0405"
        case 3 => "0608"
        case 4 => "0910"
        case 5 => "1112"
      }
    } else {
      label match {
        case 0 => "0005"
        case 1 => "0608"
        case 2 => "0912"
      }
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
    val featureVectorFileName = if (wordSimilarity) this.featuresToInclude.mkString("_") + "_similarity.master" else this.featuresToInclude.mkString("_") + ".master"
    val pw = new PrintWriter(new File("/home/mcapizzi/Github/Unbound/src/main/resources/featureVectors/" + numberOfClasses.toString + "/" + featureVectorFileName))
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
    val featureVectorFileName = if (wordSimilarity) this.featuresToInclude.mkString("_") + "_similarity.master" else this.featuresToInclude.mkString("_") + ".master"
    val pw = new PrintWriter(new File("/home/mcapizzi/Github/Unbound/src/main/resources/featureVectors/" + numberOfClasses.toString + "/" + featureVectorFileName))
    svmFile.map(line => pw.println(line))
    pw.close
  }

  def buildLeaveOneOutSVMFiles(wordSimilarity: Boolean) = {
    val featureVectorFileName = if (wordSimilarity) this.featuresToInclude.mkString("_") + "_similarity.master" else this.featuresToInclude.mkString("_") + ".master"   //find .master file based on parameters
    val folderName = featureVectorFileName.dropRight(7)                                                                                 //name folder after parameters
    val outsideFolder = new File("/home/mcapizzi/Github/Unbound/src/main/resources/featureVectors/" + numberOfClasses.toString + "/" + folderName)                           //make new folder
    outsideFolder.mkdir()                                                                                                                   //create directory

    for (i <- 0 to Source.fromFile("/home/mcapizzi/Github/Unbound/src/main/resources/featureVectors/" + numberOfClasses.toString + "/" +featureVectorFileName).getLines.size - 1) {       //set for indexes

      val test = Source.fromFile("/home/mcapizzi/Github/Unbound/src/main/resources/featureVectors/" + numberOfClasses.toString + "/" + featureVectorFileName).getLines.toStream(i)                       //the line for testing
      val trainBeforeTest = Source.fromFile("/home/mcapizzi/Github/Unbound/src/main/resources/featureVectors/" + numberOfClasses.toString + "/" + featureVectorFileName).getLines.toStream.take(i)       //lines for training BEFORE testing line
      val trainAfterTest = Source.fromFile("/home/mcapizzi/Github/Unbound/src/main/resources/featureVectors/" + numberOfClasses.toString + "/" + featureVectorFileName).getLines.toStream.drop(i + 1)    //lines for training AFTER testing line
      val train = (trainBeforeTest ++ trainAfterTest).toVector                                                                                                          //concatenated training lines

      val insideFolder = new File("/home/mcapizzi/Github/Unbound/src/main/resources/featureVectors/" + numberOfClasses.toString + "/" + outsideFolder.getName + "/" + (i + 1).toString)                                                //name of inside folder
      insideFolder.mkdir()

      val pwTrain = new PrintWriter(new File("/home/mcapizzi/Github/Unbound/src/main/resources/featureVectors/" + numberOfClasses.toString + "/" + outsideFolder.getName + "/" + insideFolder.getName + "/" + (i + 1).toString + ".train"))
      //val pwTrain = new PrintWriter(new File("/home/mcapizzi/Github/Unbound/src/main/resources/featureVectors/" + numberOfClasses.toString + "/" + outsideFolder + "/" + (i + 1).toString + ".train"))
      val pwTest = new PrintWriter(new File("/home/mcapizzi/Github/Unbound/src/main/resources/featureVectors/" + numberOfClasses.toString + "/" + outsideFolder.getName + "/" + insideFolder.getName + "/" + (i + 1).toString + ".test"))
      //val pwTest = new PrintWriter(new File("/home/mcapizzi/Github/Unbound/src/main/resources/featureVectors/" + numberOfClasses.toString + "/" + outsideFolder + "/" + (i + 1).toString + ".test"))

      train.map(line => pwTrain.println(line))
      pwTrain.close

      pwTest.println(test)
      pwTest.close

    }
  }

  //TODO test NaiveBayes
  def leaveOneOut(withEnsemble: Boolean = false): Vector[(String, Vector[(String, String, String)])] = {
    val folderName = this.featuresToInclude.mkString("_")
    val outsideFolder = new File("/home/mcapizzi/Github/Unbound/src/main/resources/featureVectors/" + numberOfClasses.toString + "/" + folderName)         //select proper outside folder based on parameters

    val scoreList = for (model <- modelsToUse) yield {

      if (model == "naiveBayes") {                                                                              //if Naive Bayes
        val allDocs = this.importAnnotatedMakeDocuments                       //get all documents
        (
          "naiveBayes",
          (for (document <- allDocs) yield {                                     //for each document...
            val test = Vector(document)                                             //make it test
            val train = allDocs.filterNot(_ == document).toVector                   //and all other docs part of train
            val nb = new NaiveBayes(train, test, Vector(), 0, 0, 0)
            (
              document.title,       //title
              nb.argMax,            //mlScore
              document.gradeLevel   //actualScore
            )
          }).toVector
        )

      } else {
        val classifier = model match {                                                                          //else build the classifier
          case "logisticRegression" => new LogisticRegressionClassifier[Int, String](bias = false)
          case "perceptron" => new PerceptronClassifier[Int, String](
            epochs = 20,
            marginRatio = 1d
          )
          case "randomForest" => new RandomForestClassifier[Int, String](
            numTrees = 5000,
            featureSampleRatio = -.20,
            maxTreeDepth = 4
          )
        }

        (
          model,                                                                            //classifier name
          (for (insideFolder <- outsideFolder.listFiles) yield {
            //for each subfolder
            val train = insideFolder.listFiles.find(fileName => fileName.getName.contains("train")).get             //get train file
            val test = insideFolder.listFiles.find(fileName => fileName.getName.contains("test")).get               //get test file

            val trainDataSet = RVFDataset.mkDatasetFromSvmLightFormat(train.getCanonicalPath)                       //build training dataset
            val testDataSet = RVFDataset.mkDatumsFromSvmLightFormat(test.getCanonicalPath)                          //build test dataset

            classifier.train(trainDataSet)

            val titleRegex = """.+#(.*)""".r
            val line = Source.fromFile(test).getLines.toVector.head
            val title = titleRegex.replaceFirstIn(line, """$1""")

            (
              title,                                                                        //title
              revertLabel(classifier.classOf(testDataSet.head)),                            //mlScore
              revertLabel(testDataSet.head.label)                                           //actualScore
              )
          }).toVector
        )
      }
    }
    scoreList.asInstanceOf[Vector[(String, Vector[(String, String, String)])]]
  }

  def getWeights = {
    //what does Perceptron give?
    //what does Random Forest give?
    //what to use for NaiveBayes? anything?
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


  /*
  val classifierTest = new RandomForestClassifier[Int, String](numTrees = 5000, featureSampleRatio = -.20, maxTreeDepth = 4)

  val norm = Datasets.svmScaleRVFDataset(trainDataSet, 0, 1)

  val trainDataSet = RVFDataset.mkDatasetFromSvmLightFormat("/home/mcapizzi/Github/Unbound/src/main/resources/featureVectors/lexical/1/1.train")
  val testDataSet = RVFDataset.mkDatumsFromSvmLightFormat("/home/mcapizzi/Github/Unbound/src/main/resources/featureVectors/lexical/1/1.test")

  norm.maxs


    //normalize data

    //make scale range
    val normalizedTrainDataScaleRange = Datasets.svmScaleRVFDataset(trainDataSet, -1, 1)
      //why not (0, 1)?

    //apply to every datum
    def normalizeData(dataSet: RVFDataset[Int, String]): RVFDataset[Int, String] = {
      val normalizedScaleRange = Datasets.svmScaleRVFDataset(dataSet, -1, 1)
      for (i <- 0 to dataSet.size - 1) yield
        Datasets.svmScaleDatum(dataSet.mkDatum(i).featuresCounter, normalizedScaleRange, -1, 1)
    }


    trainDataSet.featuresCounter()


    Datasets.svmScaleDatum(trainDataSet.mkDatum(0).featuresCounter, normalizedTrainDataScaleRange, -1, 1)

    */

}
