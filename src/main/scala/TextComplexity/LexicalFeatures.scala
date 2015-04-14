package TextComplexity

import TextComplexity.Concreteness._
import org.apache.commons.math3.stat.Frequency
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import java.io.File
import breeze.linalg.{sum, SparseVector, functions}
import breeze.numerics.sqrt
import scala.collection.mutable
import scala.io.Source

/**
 * Created by mcapizzi on 4/7/15.
 */
class LexicalFeatures(textDocument: TextDocument) {

  //most frequently used words by POS
  def mostFrequentWords = {
    val frequencyNouns = new Frequency()
    val frequencyAdjectives = new Frequency()
    val frequencyVerbs = new Frequency()
    val nouns = textDocument.lexicalTuple.filter(tuple => tuple._2._2.matches("NNS?"))          //filter just nouns
    val adjectives = textDocument.lexicalTuple.filter(tuple => tuple._2._2.matches("JJ.?"))     //filter just adjectives
    val verbs = textDocument.lexicalTuple.filter(tuple => tuple._2._2.matches("VB.*"))          //filter just verbs
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
    textDocument.lexicalTuple.toVector.
      filter(_._2._2.matches(pos)).                 //take only desired POS - use regex
      map(_._2._1).                                 //extract just the lemmas from tuple
      distinct.length.                              //count distinct
      toDouble / textDocument.wordCountMinusProperNouns     //normalized over wordCountMinusProperNouns
  }

  //total # of conjunctions used
  def conjunctionFrequency = {
    textDocument.lexicalTuple.toVector.
      filter(_._2._2.matches("CC")).
      map(_._2._1).length.                          //count all uses
      toDouble / textDocument.sentenceSize.toDouble         //normalized over number of sentences
  }

  //TODO normalize over wordCountMinusProperNouns
  //# of distinct word families
  def wordFamilyCount = {
    //stemmer? to detect word families? can I do it?
  }

  //word concreteness
  def getWordConcreteness = {
    textDocument.getLemmas.map(lemma =>                     //uses lemmas
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
      "number of tokens present in database normalized over non-proper noun word count" -> concretenessDouble.length.toDouble / textDocument.wordCountMinusProperNouns,
      "number of tokens not present in database normalized over non-proper noun word count" -> removed / textDocument.wordCountMinusProperNouns,
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
  def getNamedEntities = {
    val nerTuples = textDocument.lexicalTuple.map(item => (item._1, item._2._3))
    val newTuples = mutable.ListBuffer[(String, String)]()

    def loop(tuples: Vector[(String, String)]): Vector[(String, String)] = {
      if (tuples.isEmpty || tuples.tail.isEmpty) {
        newTuples.toVector
      }
      else if (tuples.head._2 != "O") {
        val ner = tuples.takeWhile(item => item._2 != "O")
        newTuples += (((ner.map(_._1).head /: ner.map(_._1).tail)(_ + " " + _), tuples.head._2))
        loop(tuples.drop(ner.length - 1).tail)
      }
      else {
        loop(tuples.tail)
      }
    }

    loop(nerTuples).distinct
  }

  def getCharacters = {
    val characterList = this.getNamedEntities.filter(_._2 == "PERSON").map(_._1)
    val buffer = mutable.Buffer[String]()                                             //to hold characters kept

    def loop(chars: Vector[String]): Vector[String] = {
      if (chars.isEmpty) buffer.toVector
      else if (chars.tail.map(_.contains(chars.head)).contains(true)) {                 //if a later item in the list contains the head
        loop(chars.tail)                                                                  //skip it
      }
      else {
        buffer += chars.head                                                            //otherwise add it to buffer
        loop(chars.tail)                                                                //continue
      }
    }

    loop(characterList.sortBy(_.length))
  }

}
