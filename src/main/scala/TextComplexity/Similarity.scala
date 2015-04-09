package TextComplexity

import java.io.File
import edu.arizona.sista.processors.fastnlp.FastNLPProcessor

import scala.io.Source

/**
 * Created by mcapizzi on 4/9/15.
 */
object Similarity {

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
    fullDocs.map(each => processor.annotate(each))                                                          //annotate all documents
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
  val countMatrixColumns = extractAllWordsAllTexts(rawTextFile, keepProper = true).map(_._1)
  val countMatrixRows = extractAllWordsAllTexts(rawTextFile, keepProper = false).
    filter(word => word._2._2.matches("NN.") || word._2._2.matches("VB.") || word._2._2.matches("JJ.") || word._2._2.matches("RB.")).      //only keep "important" POS
    map(_._1)

  //build the count matrix
  val wordMap = countMatrixRows.map(row => (row, countMatrixColumns.map(column => column -> 0).toMap))



    //for each sentence
      //for each word in distinct common noun, verb, adjective list
        //count how many times each OTHER word appears  -- COLUMNS in similarity matrix

  //generate word similarity matrix
    //rows ==> words
    //columns ==> words
    //cells ==> cosine similarity

  //generating metric
    //sum of all in sentence/paragraph?
    //average of all in sentence/paragraph?

}
