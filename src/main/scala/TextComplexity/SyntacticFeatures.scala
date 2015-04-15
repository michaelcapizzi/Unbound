package TextComplexity

import breeze.linalg.SparseVector
import breeze.numerics._
import edu.stanford.nlp.trees.tregex.TregexPattern
import edu.stanford.nlp.trees.{CollinsHeadFinder, Tree}
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import scala.collection.JavaConverters._
import scala.io.Source
import Similarity._
import scala.collection.mutable._

/**
 * Created by mcapizzi on 4/7/15.
 */
class SyntacticFeatures(textDocument: TextDocument) {

  def getSentences = {
    textDocument.document.map(_.sentences.map(_.words)).flatten
  }

  def getSentenceLengths = {
    textDocument.document.map(_.sentences.                         //for each sentence in each paragraph
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

  def getParseTrees = {
    textDocument.document.map(_.sentences.map(
      _.syntacticTree.toString).map(          //get the trees and convert to String
        Tree.valueOf)).flatten                  //convert back to SISTA tree type
  }

  def getTreeSizes = {
    this.getParseTrees.toVector.map(          //get the trees
      _.size.toDouble)                          //capture their sizes
  }

  def treeSizeStats = {
    val stat = new DescriptiveStatistics()
    this.getTreeSizes.map(stat.addValue)       //count
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
    this.getTreeDepths.map(stat.addValue)       //count
    (
      "minimum tree depth" -> stat.getMin,
      "25th %ile tree depth" -> stat.getPercentile(25),
      "mean tree depth" -> stat.getMean,
      "median tree depth" -> stat.getPercentile(50),
      "75th %ile tree depth" -> stat.getPercentile(75),
      "maximum tree depth" -> stat.getMax
      )
  }

  //from Coh-Metrix research
  def getDistanceToVerb = {
    //assuming CollinsHeadFinder ALWAYS finds the main verb first
    val cHF = new CollinsHeadFinder()
    val sentences = this.getSentences
    val trees = this.getParseTrees
    val tuple = sentences zip trees
    for (item <- tuple) yield {
      item._1.indexOf(item._2.headTerminal(cHF).toString).toDouble + 1d      //the index of the main verb in the original sentence
    }
  }

  def distanceToVerbStats = {
    val stat = new DescriptiveStatistics()
    this.getDistanceToVerb.map(stat.addValue)       //count
    (
      "minimum distance to verb" -> stat.getMin,
      "25th %ile distance to verb" -> stat.getPercentile(25),
      "mean distance to verb" -> stat.getMean,
      "median distance to verb" -> stat.getPercentile(50),
      "75th %ile distance to verb" -> stat.getPercentile(75),
      "maximum distance to verb" -> stat.getMax
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
      val summedSentenceSimilarities = sentenceSimilarities.map(wordLevel => wordLevel.sum)             //sum up the similarity scores
      if (summedSentenceSimilarities.isEmpty) 0.0 else summedSentenceSimilarities.min                   //take the least similar score from the sentence
    }
  }

  //TODO build stats
  def wordSimilaritySentenceScoreStats = {
    //
  }

 //Tregex patterns
  //modified from http://personal.psu.edu/xxl13/papers/Lu_inpress_ijcl.pdf

  val clause = TregexPattern.compile("S [< (VP < (VP . CC <# MD|VBD|VBP|VBZ)) | < (VP <# MD|VBD|VBP|VBZ)]")
  //matches any S node that either (1) dominates a VP whose head is a finite verb or (2) dominates a VP consisting of conjoined VPs whose head is a finite verb

  val fragment = TregexPattern.compile("ROOT !<< VP")
  //matches any tree without a VP

  val independentClause = TregexPattern.compile("S !> SBAR [< (VP < (VP . CC <# MD|VBD|VBP|VBZ)) | < (VP <# MD|VBD|VBP|VBZ)]")
  //matches any S node that is a clause but is NOT dominated by an SBAR

  val dependentClause = TregexPattern.compile("S > SBAR [< (VP < (VP . CC <# MD|VBD|VBP|VBZ)) | < (VP <# MD|VBD|VBP|VBZ)]")
  //matches any S node that is a clause that IS dominated by an SBAR


  def getClauseCounts = {
    (for (tree <- this.getParseTrees) yield {
      var counter = 0
      val clauseMatcher = clause.matcher(tree)
      while (clauseMatcher.find) {
        counter += 1
      }
      counter.toDouble
    }).toVector
  }

  //TODO figure out how to do
  def getClauseLengths = {
    //how to do?
    //use loop through and extract + tree.size
  }

  //# of clauses / # of sentences
  def getSentenceComplexityScore = {
    this.getClauseCounts.sum / textDocument.sentenceSize.toDouble
  }

  //return sentences grouped by structure
  def getClauseTypes = {
    val trees = this.getParseTrees
    for (tree <- trees) yield {
      var indCounter = 0
      var depCounter = 0
      val independentMatcher = independentClause.matcher(tree)
      val dependentMatcher = dependentClause.matcher(tree)
      val clauseMatcher = clause.matcher(tree)
      while (clauseMatcher.find) {
        if (independentMatcher.find) indCounter += 1
        else if (dependentMatcher.find) depCounter += 1
      }
      ("independent clauses" -> indCounter, "dependent clauses" -> depCounter)
    }
  }

  //TODO build to identify sentence type
  def getSentenceStructureTypes = {
    this.getClauseTypes
  }
}
