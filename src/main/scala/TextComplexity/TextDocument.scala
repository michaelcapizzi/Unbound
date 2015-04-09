package TextComplexity

import edu.arizona.sista.learning.Datum
import edu.arizona.sista.processors.corenlp.CoreNLPProcessor
import edu.arizona.sista.struct.Counter
import edu.stanford.nlp.trees.Tree
import org.apache.commons.math3.stat.Frequency
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import scala.collection.JavaConverters._
import Concreteness._


/**
 * Created by mcapizzi on 4/5/15.
 */

//each item in vector is a full paragraph
class TextDocument(text: Vector[String], processor: CoreNLPProcessor, document: Vector[edu.arizona.sista.processors.Document], author: String, title: String, chapter: String, gradeLevel: String) {

  val getAuthor = author

  val getTitle = title

  val getChapter = chapter

  val getGradeLevel = gradeLevel

  def fullText = {
    text.mkString(" ")
  }

  ////////////////////////// for normalizing //////////////////////////

  def getWords = {
    this.lexicalTuple.toVector.
      map(_._1).                                  //get the tokens
      filter(_.matches("[A-Za-z]+"))              //only keep words (not punctuation)
  }

  def getProperNouns = {
    this.lexicalTuple.toVector.
      filter(tuple => (tuple._2._3 == "PERSON" || tuple._2._3 == "LOCATION")        //is either PERSON or LOCATION
      && tuple._1.matches("[A-Z].*")).                                                //and capitalized
      map(_._1).distinct                                                            //take just the word and get a distinct list
  }

  def getWordsMinusProperNouns = {
    this.lexicalTuple.toVector.
      filter(_._1.matches("[A-Za-z]+")).                                                    //only keep words (not punctuation)
      filterNot(tuple => (tuple._2._3 == "PERSON" || tuple._2._3 == "LOCATION")         //remove words that are either PERSON or LOCATION
      && tuple._1.matches("[A-Z].*")).                                                    //and capitalized
      map(_._1)                                                                         //take just the word
  }

  //# of total words
  def wordCount = {
    this.getWords.length.toDouble
  }

  def properNounCount = {
    this.getProperNouns.length.toDouble
  }

  def wordCountMinusProperNouns = {
    this.getWordsMinusProperNouns.length.toDouble
  }

  def getLemmas = {
    this.lexicalTuple.toVector.
      map(_._2._1).                                 //get the lemmas
      filter(_.matches("[A-Za-z]+"))               //only keep words (not punctuation)
  }

  //# of total lemmas
  def lemmaCount = {
    this.lexicalTuple.toVector.
      map(_._2._1).                               //get the lemmas
      count(_.matches("[A-Za-z]+"))               //only count words (not punctuation)
  }

  //# of sentences
  def sentenceSize = {
    document.map(_.sentences.length).sum
  }

  //# of paragraphs
  def paragraphSize = {
    text.length
  }

  ////////////////////////// statistics //////////////////////////

  /*def statCount(stat: DescriptiveStatistics, listOfCounts: Vector[Int], statCommand: String) = {
    listOfCounts.map(stat.addValue(_))
    statCommand
  }*/

  ////////////////////////// access Processors  //////////////////////////

  def annotate = {
    document.map(processor.annotate)
  }

  //TODO build method to retrieve from file
  def retrieveAnnotation = {
    //
  }
  ////////////////////////// lexical //////////////////////////

  //compare to NaiveBayes piped as features

  def lexicalTuple = {
    document.map(_.sentences.map(_.words.toVector)).flatten.flatten zip             //the word
    (
      document.map(_.sentences.map(_.lemmas.get.toVector)).flatten.flatten,         //the lemma
      document.map(_.sentences.map(_.tags.get.toVector)).flatten.flatten,           //the POS tag
      document.map(_.sentences.map(_.entities.get.toVector)).flatten.flatten        //the NER label
    ).zipped.toVector
  }

  //most frequently used words by POS
  def mostFrequentWords = {
    val frequencyNouns = new Frequency()
    val frequencyAdjectives = new Frequency()
    val frequencyVerbs = new Frequency()
    val nouns = this.lexicalTuple.filter(tuple => tuple._2._2.matches("NNS?"))          //filter just nouns
    val adjectives = this.lexicalTuple.filter(tuple => tuple._2._2.matches("JJ.?"))     //filter just adjectives
    val verbs = this.lexicalTuple.filter(tuple => tuple._2._2.matches("VB.*"))          //filter just verbs
    nouns.map(word => frequencyNouns.addValue(word._1))                                 //count nouns
    adjectives.map(word => frequencyAdjectives.addValue(word._1))                       //count adjectives
    verbs.map(word => frequencyVerbs.addValue(word._1))                                 //count verbs
    val mostFreqNoun = nouns.map(tuple =>
      (
        tuple._1,                                                                       //rebuild tuple
        (
          tuple._2._1,
          tuple._2._2,
          tuple._2._3,
          frequencyNouns.getCount(tuple._1).toDouble                                    //adding frequency count
        )
      )
    ).distinct.sortBy(_._2._4).reverse.take(1)                                          //sort by highest frequency and take top one
    val mostFreqAdj = adjectives.map(tuple =>
      (
        tuple._1,                                                                       //rebuild tuple
        (
          tuple._2._1,
          tuple._2._2,
          tuple._2._3,
          frequencyAdjectives.getCount(tuple._1).toDouble                               //adding frequency count
        )
      )
    ).distinct.sortBy(_._2._4).reverse.take(1)                                          //sort by highest frequency and take top one
    val mostFreqVerb = verbs.map(tuple =>
      (
        tuple._1,                                                                       //rebuild tuple
        (
          tuple._2._1,
          tuple._2._2,
          tuple._2._3,
          frequencyVerbs.getCount(tuple._1).toDouble                                    //adding frequency count
        )
      )
    ).distinct.sortBy(_._2._4).reverse.take(1)                                          //sort by highest frequency and take top one
    (                                                                                   //build map of most frequency word for each POS
      "noun" -> mostFreqNoun.head._1,
      "adjective" -> mostFreqAdj.head._1,
      "verb" -> mostFreqVerb.head._2._1                                                 //take lemma of verb
    )
  }

  //# of total distinct lemmas by part of speech
      //verb (VB.*)
      //adjective (JJ.*)
      //conjunctions (CC)
  def countDistinctPOS(pos: String) = {
    this.lexicalTuple.toVector.
      filter(_._2._2.matches(pos)).                 //take only desired POS - use regex
      map(_._2._1).                                 //extract just the lemmas from tuple
      distinct.length.                              //count distinct
      toDouble / this.wordCountMinusProperNouns     //normalized over wordCountMinusProperNouns
  }

  //total # of conjunctions used
  def conjunctionFrequency = {
    this.lexicalTuple.toVector.
    filter(_._2._2.matches("CC")).
    map(_._2._1).length.                          //count all uses
    toDouble / this.sentenceSize.toDouble         //normalized over number of sentences
  }

  //TODO normalize over wordCountMinusProperNouns
  //# of distinct word families
  def wordFamilyCount = {
    //stemmer? to detect word families? can I do it?
  }

  //word concreteness
  def getWordConcreteness = {
    this.getLemmas.map(lemma =>                     //uses lemmas
      (
        lemma,                                      //the lemma
        concretenessMap.getOrElse(lemma, "99")      //its concreteness score (0 - 5; 5 very concrete; 99 not in database)
      )
    )
  }

  def wordConcretenessStats = {
    val stat = new DescriptiveStatistics()
    val removed = this.getWordConcreteness.filter(missing => missing._2 == "99").distinct.length.toDouble     //count of how many distinct words weren't in database
    val concretenessDouble = this.getWordConcreteness.map(item =>                                             //process results of .getWordConcreteness
      (
        item._1,
        item._2.toDouble                                                                                      //converts concreteness score to Double
      )
    ).filterNot(missing =>
      missing._2 == 99)                                                                                       //remove words not in database
    concretenessDouble.map(tuple => stat.addValue(tuple._2))                                                  //count
    (
      "number of tokens present in database normalized over non-proper noun word count" -> concretenessDouble.length.toDouble / this.wordCountMinusProperNouns,
      "number of tokens not present in database normalized over non-proper noun word count" -> removed / this.wordCountMinusProperNouns,
      "minimum concreteness score present in text" -> stat.getMin,
      "25th %ile concreteness" -> stat.getPercentile(25),
      "mean concreteness" -> stat.getMean,
      "median concreteness" -> stat.getPercentile(50),
      "75th %ile concreteness" -> stat.getPercentile(75),
      //"maximum concreteness score present in text" -> stat.getMax,             //only 280 items = 5; too subjective of a list to use as measure?
      "concreteness score of most used noun" -> concretenessDouble.toMap.getOrElse(this.mostFrequentWords._1._2, 0.toDouble),
      "concreteness score of most used adjective" -> concretenessDouble.toMap.getOrElse(this.mostFrequentWords._2._2, 0.toDouble),
      "concreteness score of most used verb" -> concretenessDouble.toMap.getOrElse(this.mostFrequentWords._3._2, 0.toDouble)
    )
  }

  //# of distinct Named Entities
    // could be an approximation of character? --> PERSON only
    //In combination with Capital letter, could represent proper locations too --> LOCATION + capital letter
  //TODO normalize over wordCounts
  def getNamedEntities = {
    //implement chunking
  }


  //implements Naive Bayes - output: (best grade level classification, condProbs Map)
  //TODO implement Naive Bayes
    //make sure that intermediate function captures conditional probabilities for each word

  ////////////////////////// syntactic //////////////////////////

  def getSentences = {
    document.map(_.sentences.map(_.words)).flatten
  }

  def getSentenceLengths = {
    document.map(_.sentences.                         //for each sentence in each paragraph
      map(_.words.filter(_.matches("[A-Za-z]+"))      //get the tokens that are words
      .length))                                       //get their sizes
      .flatten                                        //remove the paragraphs
  }

  def sentenceLengthStats = {
    val stat = new DescriptiveStatistics()
    this.getSentenceLengths.map(stat.addValue(_))           //count
    (
      "sentence length minimum" -> stat.getMin,
      "25th %ile sentence length" -> stat.getPercentile(50),
      "sentence length mean" -> stat.getMean,
      "sentence length median" -> stat.getPercentile(50),
      "75th %ile sentence length" -> stat.getPercentile(75),
      "sentence length maximum" -> stat.getMax
    )
  }

  def getConstituents = {
    this.getParseTrees.map(_.constituents)
  }

  def constituentsCountStats = {
    val stat = new DescriptiveStatistics()
    this.getConstituents.map(_.size).map(stat.addValue(_))                //count
    (
      "minimum number of constituents in a sentence" -> stat.getMin,
      "25th %ile number of constituents per sentence" -> stat.getPercentile(25),
      "mean number of constituents per sentence" -> stat.getMean,
      "median number of constituents per sentence" -> stat.getPercentile(50),
      "75th %ile number of constituents per sentence" -> stat.getPercentile(75),
      "maximum number of constituents in a sentence" -> stat.getMax
    )
  }

  def getConstituentLengths = {
    this.getConstituents.map(
    _.asScala.toVector).map(                          //convert consituent pairs to Scala vectors
    constituent =>
      for (c <- constituent) yield c.size).flatten    //get size (difference) of each constituent pair
  }

  def constituentLengthStats = {
    val stat = new DescriptiveStatistics()
    this.getConstituentLengths.map(stat.addValue(_))            //count
    (
      "constituent length minimum" -> stat.getMin,
      "25th %ile constituent length" -> stat.getPercentile(25),
      "constituent length mean" -> stat.getMean,
      "constituent length median" -> stat.getPercentile(50),
      "75th %iler constituent length" -> stat.getPercentile(75),
      "constituent length maximum" -> stat.getMax
      )
  }

  def getParseTrees = {
    document.map(_.sentences.map(
      _.syntacticTree.toString).map(          //get the trees and convert to String
      Tree.valueOf)).flatten                  //convert back to SISTA tree type
  }

  def getTreeSizes = {
    this.getParseTrees.toVector.map(          //get the trees
    _.size.toDouble)                          //capture their sizes
  }

  def treeSizeStats = {
    val stat = new DescriptiveStatistics()
    this.getTreeSizes.map(stat.addValue(_))       //count
    (
      "minimum tree size" -> stat.getMin,
      "25th %ile tree size" -> stat.getPercentile(25),
      "mean tree size" -> stat.getMean,
      "median tree size" -> stat.getPercentile(50),
      "75th %ile tree size" -> stat.getPercentile(75),
      "maximum tree size" -> stat.getMax
    )
  }

  def getTreeDepths = {
    this.getParseTrees.toVector.map(          //get the trees
    _.depth.toDouble)                         //capture their depths
  }

  def treeDepthStats = {
    val stat = new DescriptiveStatistics()
    this.getTreeDepths.map(stat.addValue(_))       //count
    (
      "minimum tree depth" -> stat.getMin,
      "25th %ile tree depth" -> stat.getPercentile(25),
      "mean tree depth" -> stat.getMean,
      "median tree depth" -> stat.getPercentile(50),
      "75th %ile tree depth" -> stat.getPercentile(75),
      "maximum tree depth" -> stat.getMax
      )
  }

  ////////////////////////// paragraph //////////////////////////

  //paragraph size stats

  //coreference

  //discourse
  //https://github.com/sistanlp/processors/blob/master/src/main/scala/edu/arizona/sista/discourse/rstparser/DiscourseTree.scala


  ////////////////////////// document //////////////////////////

  ////////////////////////// narrative //////////////////////////

  //number of characters

}
