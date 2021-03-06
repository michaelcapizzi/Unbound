package TextComplexity

import java.io.File
import breeze.linalg.{sum, SparseVector, functions}
import breeze.numerics.sqrt
import edu.arizona.sista.processors.fastnlp.FastNLPProcessor
import org.apache.commons.math3.stat.Frequency

import scala.io.Source

/**
 * Created by mcapizzi on 4/9/15.
 */
object Similarity {



  /*def wordSimilarityTextLookUp(wordOne: String, wordTwo: String, similarityFileName: String): Double = {
    val wordOneVector = SparseVector(Source.fromFile("/home/mcapizzi/Github/Unbound/src/main/resources/" + similarityFileName).getLines.
      find(line => line.startsWith(wordOne)).map(_.           //find the vector in the text file
      split(" ").drop(1)).                                      //split and drop word (leaving just numbers)
      toArray.flatten.map(_.toDouble))                          //flatten and turn into double

    val wordTwoVector = SparseVector(Source.fromFile("/home/mcapizzi/Github/Unbound/src/main/resources/" + similarityFileName).getLines.
        find(line => line.startsWith(wordTwo)).map(_.           //find the vector in the text file
        split(" ").drop(1)).                                      //split and drop word (leaving just numbers)
        toArray.flatten.map(_.toDouble))                          //flatten and turn into double

    val normalized = sqrt(wordOneVector dot wordOneVector) * sqrt(wordTwoVector dot wordTwoVector)
    val dotProduct = if (wordOneVector.length == 0 || wordTwoVector.length == 0) 0 else wordOneVector dot wordTwoVector

    dotProduct / normalized
  }

  def wordSimilarityHashLookUp(wordOne: String, wordTwo: String, similarityHash: Map[String, Array[Double]]): Double = {
    val wordOneVector = SparseVector(similarityHash(wordOne))
    val wordTwoVector = SparseVector(similarityHash(wordTwo))

    val normalized = sqrt(wordOneVector dot wordOneVector) * sqrt(wordTwoVector dot wordTwoVector)
    val dotProduct = if (wordOneVector.length == 0 || wordTwoVector.length == 0) 0 else wordOneVector dot wordTwoVector

    dotProduct / normalized
  }*/

  def foldElementwiseSum(vectorList: Vector[SparseVector[Double]]): SparseVector[Double] = {

    def loop(vectorList: Vector[SparseVector[Double]], accum: SparseVector[Double]): SparseVector[Double] = {
      if (vectorList.tail.isEmpty) {
        accum
      }
      else {
        loop(vectorList.tail, accum + vectorList.tail.head)
      }
    }
    loop(vectorList, vectorList.head)
  }

  def wordSimilarityVector(wordOneVector: SparseVector[Double], wordTwoVector: SparseVector[Double]): Double = {
    val normalized = sqrt(wordOneVector dot wordOneVector) * sqrt(wordTwoVector dot wordTwoVector)
    val dotProduct = if (wordOneVector.length == 0 || wordTwoVector.length == 0) 0 else wordOneVector dot wordTwoVector

    if (dotProduct == 0) 0 else dotProduct / normalized
  }

/*

//calculate similarity for every word in a sentence in relation to its neighbors

  //extract all distinct words from all texts -- ROWS in similarity matrix
    //do it dynamically so it can update as the test data increases
    //only capture common nouns, verbs, and adjectives

  val rawTextFile = new File("/home/mcapizzi/Github/Unbound/src/main/resources/rawText")

  //concatenate all sentences/paragraphs from all texts into one list
  def extractAllSentencesAllTexts(directory: File): Vector[Vector[String]] = {
    val metadataRegex = """%%?.+""".r
    val fullTexts = directory.listFiles.map(item => Source.fromFile(item.getCanonicalPath).mkString).   //import each text
      map(text => metadataRegex.replaceAllIn(text, ""))                                                 //remove metadata
    val processor = new FastNLPProcessor()
    val fullDocs = fullTexts.map(each => processor.mkDocument(each))                                    //make processors documents
    fullDocs.map(each => processor.annotate(each))                                                      //annotate all documents
    fullDocs.map(_.sentences.map(_.words.toVector)).flatten.toVector                                    //flat list of sentences
  }

  //get all words from all texts
      //keepProper = true --> keeps ALL tokens that are not punctuation
      //keepProper = false --> removes punctuation and proper nouns
  def extractAllWordsAllTexts(directory: File, keepProper: Boolean): Vector[(String, (String, String, String))] = {
    val metadataRegex = """%%?.+""".r
    val fullTexts = directory.listFiles.map(item => Source.fromFile(item.getCanonicalPath).mkString).       //import each text
      map(text => metadataRegex.replaceAllIn(text, ""))                                                     //remove metadata
    val processor = new FastNLPProcessor()
    val fullDocs = fullTexts.map(each => processor.mkDocument(each))                                        //make processors documents
    //fullDocs.map(each => processor.annotate(each))                                                          //annotate all documents -- takes too long because of dependency parser
    fullDocs.map(each => processor.tagPartsOfSpeech(each))
    fullDocs.map(each => processor.lemmatize(each))
    fullDocs.map(each => processor.recognizeNamedEntities(each))
    val allWordsTuple = fullDocs.map(_.sentences.map(_.words.toVector)).flatten.flatten.toVector zip        //build tuple (word, (lemma, POS, NER))
        (
          fullDocs.map(_.sentences.map(_.lemmas.get.toVector)).flatten.flatten,                             //lemmas
          fullDocs.map(_.sentences.map(_.tags.get.toVector)).flatten.flatten,                               //tags
          fullDocs.map(_.sentences.map(_.entities.get.toVector)).flatten.flatten                            //entities
          ).zipped.toVector
    if (keepProper) {                                                                                       //if keeping proper nouns in...
      allWordsTuple.filter(word => word._1.matches("[A-Za-z]+")).toVector                                     //remove punctuation
    } else {                                                                                                //if removing proper nouns
      allWordsTuple.filter(word => word._1.matches("[A-Za-z]+")).                                             //remove punctuation
        filterNot(tuple => (tuple._2._3 == "PERSON" || tuple._2._3 == "LOCATION")                             //remove proper nouns: is either PERSON or LOCATION
        && tuple._1.matches("[A-Z].*")).toVector                                                              //and capitalized
    }
  }

  //vectors for countMatrix
  def makeCountMatrixRows(directory: File): Vector[String] = {
    extractAllWordsAllTexts(directory, keepProper = false).
      filter(word => word._2._2.matches("NN.") || word._2._2.matches("VB.") || word._2._2.matches("JJ.") || word._2._2.matches("RB.")).      //only keep "important" POS
      map(_._1).
      map(_.toLowerCase).distinct                                                                                                                     //make all lowercase
  }

  def makeCountMatrixColumns(directory: File): Vector[String] = {
    extractAllWordsAllTexts(directory, keepProper = true).map(_._1).map(_.toLowerCase).distinct
  }

  //map representing countMatrix
  def makeWordMap(allSentencesAllTexts: Vector[Vector[String]], rowMatrix: Vector[String]/*, columnMatrix: Vector[String]*/): Vector[(String, Map[String, Double])] = {
    for (wordToCalculate <- rowMatrix) yield {                                                      //for every word in wordMatrix rows
    val frequency = new Frequency()                                                                       //build frequency to capture counts
    val containsWord = allSentencesAllTexts.
        map(_.map(_.toLowerCase)).                                                                          //make all words lowercase
        filter(sentence => sentence.contains(wordToCalculate))                                              //capture the sentences containing that word
      for (sentence <- containsWord) {                                                                      //for each sentence
        for (word <- sentence.filterNot(_ == wordToCalculate)) {                                                //for each word that is not the wordToCalculate
          frequency.addValue(word)                                                                                //increment the count ==> # of times it exists in same sentence as wordToCalculate
        }
      }                                                                                                     //build the output tuple
      (wordToCalculate,                                                                                       //(wordToCalculate,
        /*containsWord*/allSentencesAllTexts.map(_.map(_.toLowerCase)).flatten.map(word =>                    //commented out "containsWord" to include 0 counts for all words (not just sparse matrix)
          word -> frequency.getCount(word).toDouble                                                           //(word -> count))
        ).toMap
      )
    }
  }


  //calculate cosine similarity of two words
    //compiles despite error
  def calculateWordSimilarity(wordOne: String, wordTwo: String, wordCountMap: Vector[(String, Map[String, Double])]): Double = {
    val wordOneVector = SparseVector(wordCountMap.filter(_._1 == wordOne).map(_._2.values).flatten.toArray)
    val wordTwoVector = SparseVector(wordCountMap.filter(_._1 == wordTwo).map(_._2.values).flatten.toArray)
    val dotProduct = if (wordOneVector.length != wordTwoVector.length) 0 else wordOneVector dot wordTwoVector         //if words both don't appear in wordMap, then 0
    val normalized = sqrt(wordOneVector dot wordOneVector) * sqrt(wordTwoVector dot wordTwoVector)                    //square root of the dot product of itself?
    dotProduct / normalized
  }



  /////////////////////test:
  //calculateWordSimilarity("mother", "bear", makeWordMap(extractAllSentencesAllTexts(f), makeCountMatrixRows(f)))


  /*//all sentences in all texts
  val allSentencesFromAllTexts = extractAllSentencesAllTexts(rawTextFile)

  //count matrix rows
  val countMatrixRows = makeCountMatrixRows(rawTextFile)

  //count matrix columns
  val countMatrixColumns = makeCountMatrixColumns(rawTextFile)

  //count matrix
  val wordMap = makeWordMap(allSentencesFromAllTexts, countMatrixRows)*/

*/


}
