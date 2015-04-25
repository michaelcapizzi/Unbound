package TextComplexity

import TextComplexity.Concreteness._
import TextComplexity.Similarity._
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
class LexicalFeatures(val textDocument: TextDocument) {

  //TODO how to deal with no words in that part of speech
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

  //# of distinct word families
  def wordFamilyCount = {
    //stemmer? to detect word families? can I do it?
    //normalize over wordCountMinusProperNouns
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

    Map(
      "number of tokens present in database normalized over non-proper noun word count" -> concretenessDouble.length.toDouble / textDocument.wordCountMinusProperNouns,
      "number of tokens not present in database normalized over non-proper noun word count" -> removed / textDocument.wordCountMinusProperNouns,
      "minimum concreteness score present in text" -> stat.getMin,
      "25th %ile concreteness score present in text" -> stat.getPercentile(25),
      "mean concreteness score present in text" -> stat.getMean,
      "median concreteness score present in text" -> stat.getPercentile(50),
      "75th %ile concreteness score present in text" -> stat.getPercentile(75),
      //"maximum concreteness score present in text" -> stat.getMax,             //only 280 items = 5; too subjective of a list to use as measure?
      "concreteness score of most used noun" -> concretenessDouble.toMap.getOrElse(this.mostFrequentWords._1._2, 0.toDouble),
      "concreteness score of most used adjective" -> concretenessDouble.toMap.getOrElse(this.mostFrequentWords._2._2, 0.toDouble),
      "concreteness score of most used verb" -> concretenessDouble.toMap.getOrElse(this.mostFrequentWords._3._2, 0.toDouble)
      )
  }

  //calculates similarity score for every word compared to every other word in EACH SENTENCE
  def getWordSimilaritySentenceScores = {
    val importantWords = for (sentence <- textDocument.lexicalTupleInSentences) yield {
      sentence.filter(word => word._2._2.matches("NN.*") || word._2._2.matches("VB.*") || word._2._2.matches("JJ.*") || word._2._2.matches("RB.*")).    //keep *only important POS
        filterNot(entity => (entity._2._3.matches("PERSON") || entity._2._3.matches("LOCATION")) && entity._2._1.matches("[A-Z]")).                     //drop proper nouns
        map(_._1).map(_.toLowerCase).distinct                                                                                                           //make lowercase and distinct
    }

    import scala.collection.mutable.Map
    val similarityHashMap = Map[String, Array[Double]]()                                                //build mutable map to house previously looked up similarity vectors

    for (sentence <- importantWords) yield {                                                            //for each sentence
    val sentenceSimilarities = for (word <- sentence) yield {                                           //for each important target in sentence
        for (item <- sentence.filterNot(_ == word)) yield {                                                 //for every other word
          if (sentence.length == 1) 0.0
          else if (similarityHashMap.contains(word) && similarityHashMap.contains(item)) {                           //if both exist in hash map
          val wordOneVector = SparseVector(similarityHashMap(word))
            val wordTwoVector = SparseVector(similarityHashMap(item))
            wordSimilarityVector(wordOneVector, wordTwoVector)                                                    //call from map
          }
          else if (similarityHashMap.contains(word)) {                                                      //if only one appears in hash map
          val wordOneVector = SparseVector(similarityHashMap(word))                                             //call from map
          val wordTwoVector = SparseVector(Source.fromFile(                                                     //build the other
              "/home/mcapizzi/Github/Unbound/src/main/resources/wordSimilarityData.txt").getLines.
              find(line => line.startsWith(item)).map(_.                                                            //find the vector in the text file
              split(" ").drop(1)).                                                                                    //split and drop word (leaving just numbers)
              toArray.flatten.map(_.toDouble))                                                                        //flatten and turn into double
            similarityHashMap(item) = wordTwoVector.toArray                                                            //add it to map
            wordSimilarityVector(wordOneVector, wordTwoVector)
          }
          else if (similarityHashMap.contains(item)) {                                                      //if only one appears in hash map
          val wordTwoVector = SparseVector(similarityHashMap(item))                                             //call from map
          val wordOneVector = SparseVector(Source.fromFile(                                                     //build the other
              "/home/mcapizzi/Github/Unbound/src/main/resources/wordSimilarityData.txt").getLines.
              find(line => line.startsWith(word)).map(_.                                                            //find the vector in the text file
              split(" ").drop(1)).                                                                                    //split and drop word (leaving just numbers)
              toArray.flatten.map(_.toDouble))                                                                        //flatten and turn into double
            similarityHashMap(word) = wordOneVector.toArray                                                            //add it to map
            wordSimilarityVector(wordOneVector, wordTwoVector)
          }
          else {                                                                                            //if neither appears
          val wordOneVector = SparseVector(Source.fromFile(                                                     //build both
              "/home/mcapizzi/Github/Unbound/src/main/resources/wordSimilarityData.txt").getLines.
              find(line => line.startsWith(word)).map(_.                                                            //find the vector in the text file
              split(" ").drop(1)).                                                                                    //split and drop word (leaving just numbers)
              toArray.flatten.map(_.toDouble))                                                                        //flatten and turn into double
            similarityHashMap(word) = wordOneVector.toArray                                                            //add it to map

            val wordTwoVector = SparseVector(Source.fromFile(
              "/home/mcapizzi/Github/Unbound/src/main/resources/wordSimilarityData.txt").getLines.
              find(line => line.startsWith(item)).map(_.                                                            //find the vector in the text file
              split(" ").drop(1)).                                                                                    //split and drop word (leaving just numbers)
              toArray.flatten.map(_.toDouble))                                                                        //flatten and turn into double
            similarityHashMap(item) = wordTwoVector.toArray                                                            //add it to map
            wordSimilarityVector(wordOneVector, wordTwoVector)
          }
        }
      }
      val summedSentenceSimilarities = sentenceSimilarities.map(wordLevel => wordLevel.sum / wordLevel.length.toDouble)     //take similarity score for each word divided by number of words
      if (summedSentenceSimilarities.isEmpty) 0.0 else summedSentenceSimilarities.min                   //take the least similar score from the sentence
    }
  }

  //calculates word similarity for a SENTENCE and comapres it to every other sentence IN THE PARAGRAPH
  def getWordSimilarityDocumentScores = {
    val importantWords = for (sentence <- textDocument.lexicalTupleInSentences) yield {
      sentence.filter(word => word._2._2.matches("NN.*") || word._2._2.matches("VB.*") || word._2._2.matches("JJ.*") || word._2._2.matches("RB.*")). //keep *only important POS
        filterNot(entity => (entity._2._3.matches("PERSON") || entity._2._3.matches("LOCATION")) && entity._2._1.matches("[A-Z]")). //drop proper nouns
        map(_._1).map(_.toLowerCase).distinct //make lowercase and distinct
    }

    import scala.collection.mutable.Map
    val similarityHashMap = Map[String, SparseVector[Double]]() //build mutable map to house previously looked up similarity vectors

    val sentenceVectors = for (sentence <- importantWords) yield {                                        //for each sentence
      val sentenceSimilarities =
        for (word <- sentence) yield {
          if (sentence.length < 2) SparseVector.zeros[Double](200)
          else if (similarityHashMap.contains(word)) {
            println("accessing " + word)
            similarityHashMap(word)                                                                      //access the word vector
          } else {
            println("finding " + word)
            val wordVector = SparseVector(Source.fromFile(                                                //build the word vector
              "/home/mcapizzi/Github/Unbound/src/main/resources/wordSimilarityData.txt").getLines.
              find(line => line.startsWith(word)).map(_.                                                  //find the vector in the text file
              split(" ").drop(1)).                                                                        //split and drop word (leaving just numbers)
              toArray.flatten.map(_.toDouble))
            println("adding " + word)
            similarityHashMap(word) = if (wordVector.size == 0) SparseVector.zeros[Double](200) else wordVector     //add it to hash map
            similarityHashMap(word)
          }
        }
      println("elementwise adding word similarities")
      foldElementwiseSum(sentenceSimilarities) / sentenceSimilarities.length.toDouble                     //elementwise add vectors for the sentence normalized over # of words in the sentence
    }
    for (sentence <- sentenceVectors) yield {                                                                                               //for every sentence vector in the text
      val window = sentenceVectors.slice(sentenceVectors.indexOf(sentence) - 5, sentenceVectors.indexOf(sentence) + 6)                      //build a window of +/- 5 sentences
      val windowIndivScore = for (sentence2 <- window.filterNot(item => window.indexOf(item) == window.indexOf(sentence))) yield {            //calculate similarity score against each other sentence in window
        println("calculating similarity of sentences in window")
        wordSimilarityVector(sentence, sentence2)
      }
      windowIndivScore.sum / windowIndivScore.length.toDouble                                                                                 //normalize over number of vectors used
    }
  }

  def wordSimilaritySentenceScoreStats = {
    val stat = new DescriptiveStatistics()
    this.getWordSimilaritySentenceScores.map(stat.addValue)            //count

    Map(
      "minimum similarity sentence score" -> stat.getMin,
      "25th %ile similarity sentence score" -> stat.getPercentile(25),
      "mean similarity sentence score" -> stat.getMean,
      "median similarity sentence score" -> stat.getPercentile(50),
      "75th %ile similarity sentence score" -> stat.getPercentile(75),
      "maximum similarity sentence score" -> stat.getMax
    )
  }

  def wordSimilarityDocumentScoreStats = {
    val stat = new DescriptiveStatistics()
    this.getWordSimilarityDocumentScores.map(stat.addValue)            //count

    Map(
      "minimum similarity sentence score" -> stat.getMin,
      "25th %ile similarity sentence score" -> stat.getPercentile(25),
      "mean similarity sentence score" -> stat.getMean,
      "median similarity sentence score" -> stat.getPercentile(50),
      "75th %ile similarity sentence score" -> stat.getPercentile(75),
      "maximum similarity sentence score" -> stat.getMax
    )
  }

  def makeLexicalFeatureVector = {
    Vector(
      (textDocument.title, textDocument.title),
      (textDocument.gradeLevel, textDocument.title),
      ("number of distinct conjunctions", this.countDistinctPOS("CC.*")),
      ("% of distinct nouns in all words", this.countDistinctPOS("NN.*")),
      ("% of distinct verbs in all words", this.countDistinctPOS("VB.*")),
      ("% of distinct adjectives in all words", this.countDistinctPOS("JJ.*")),
      ("% of tokens not present in concreteness", this.wordConcretenessStats("number of tokens not present in database normalized over non-proper noun word count")),
      ("minimum concreteness score present in text", this.wordConcretenessStats("minimum concreteness score present in text")),
      ("25th %ile concreteness score present in text", this.wordConcretenessStats("25th %ile concreteness score present in text")),
      ("mean concreteness score present in text", this.wordConcretenessStats("mean concreteness score present in text")),
      ("median concreteness score present in text", this.wordConcretenessStats("median concreteness score present in text")),
      ("75th %ile concreteness score present in text", this.wordConcretenessStats("75th %ile concreteness score present in text")),
      ("concreteness score of most used noun", this.wordConcretenessStats("concreteness score of most used noun")),
      ("concreteness score of most used verb", this.wordConcretenessStats("concreteness score of most used verb")),
      ("concreteness score of most used adjective", this.wordConcretenessStats("concreteness score of most used adjective")),
      ("minimum word similarity sentence score", this.wordSimilarityDocumentScoreStats("minimum similarity sentence score")),
      ("25th %ile word similarity sentence score", this.wordSimilarityDocumentScoreStats("25th %ile similarity sentence score")),
      ("mean word similarity sentence score", this.wordSimilarityDocumentScoreStats("mean similarity sentence score")),
      ("median word similarity sentence score", this.wordSimilarityDocumentScoreStats("median similarity sentence score")),
      ("75th %ile similarity sentence score", this.wordSimilarityDocumentScoreStats("75th %ile similarity sentence score")),
      ("maximum similarity sentence score", this.wordSimilarityDocumentScoreStats("maximum similarity sentence score"))
    )
  }

  def makeLexicalFeatureMinusWordSimilarityVector = {
    Vector(
      (textDocument.title, textDocument.title),
      (textDocument.gradeLevel, textDocument.title),
      ("number of distinct conjunctions", this.countDistinctPOS("CC.*")),
      ("% of distinct nouns in all words", this.countDistinctPOS("NN.*")),
      ("% of distinct verbs in all words", this.countDistinctPOS("VB.*")),
      ("% of distinct adjectives in all words", this.countDistinctPOS("JJ.*")),
      ("% of tokens not present in concreteness", this.wordConcretenessStats("number of tokens not present in database normalized over non-proper noun word count")),
      ("minimum concreteness score present in text", this.wordConcretenessStats("minimum concreteness score present in text")),
      ("25th %ile concreteness score present in text", this.wordConcretenessStats("25th %ile concreteness score present in text")),
      ("mean concreteness score present in text", this.wordConcretenessStats("mean concreteness score present in text")),
      ("median concreteness score present in text", this.wordConcretenessStats("median concreteness score present in text")),
      ("75th %ile concreteness score present in text", this.wordConcretenessStats("75th %ile concreteness score present in text")),
      ("concreteness score of most used noun", this.wordConcretenessStats("concreteness score of most used noun")),
      ("concreteness score of most used verb", this.wordConcretenessStats("concreteness score of most used verb")),
      ("concreteness score of most used adjective", this.wordConcretenessStats("concreteness score of most used adjective"))
    )
  }

  def similaritySentenceFeatureVector = {
      Vector(
        ("minimum word similarity sentence score", this.wordSimilaritySentenceScoreStats("minimum similarity sentence score")),
        ("25th %ile word similarity sentence score", this.wordSimilaritySentenceScoreStats("25th %ile similarity sentence score")),
        ("mean word similarity sentence score", this.wordSimilaritySentenceScoreStats("mean similarity sentence score")),
        ("median word similarity sentence score", this.wordSimilaritySentenceScoreStats("median similarity sentence score")),
        ("75th %ile similarity sentence score", this.wordSimilaritySentenceScoreStats("75th %ile similarity sentence score")),
        ("maximum similarity sentence score", this.wordSimilaritySentenceScoreStats("maximum similarity sentence score"))
      )
    }

  def similarityDocumentFeatureVector = {
    Vector(
      ("minimum word similarity sentence score", this.wordSimilarityDocumentScoreStats("minimum similarity sentence score")),
      ("25th %ile word similarity sentence score", this.wordSimilarityDocumentScoreStats("25th %ile similarity sentence score")),
      ("mean word similarity sentence score", this.wordSimilarityDocumentScoreStats("mean similarity sentence score")),
      ("median word similarity sentence score", this.wordSimilarityDocumentScoreStats("median similarity sentence score")),
      ("75th %ile similarity sentence score", this.wordSimilarityDocumentScoreStats("75th %ile similarity sentence score")),
      ("maximum similarity sentence score", this.wordSimilarityDocumentScoreStats("maximum similarity sentence score"))
    )
  }

}
