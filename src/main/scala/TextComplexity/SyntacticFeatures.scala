package TextComplexity

import breeze.linalg.SparseVector
import breeze.numerics._
import edu.stanford.nlp.trees.tregex.TregexPattern
import edu.stanford.nlp.trees.{CollinsHeadFinder, Tree}
import org.apache.commons.math3.stat.Frequency
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import scala.collection.JavaConverters._
import scala.io.Source
import Similarity._
import scala.collection.mutable._

/**
 * Created by mcapizzi on 4/7/15.
 */
class SyntacticFeatures(val textDocument: TextDocument) {

  //TODO test wordSimilarity on longer text
      //include print statements?

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

    Map(
      "sentence length minimum" -> stat.getMin,
      "25th %ile sentence length" -> stat.getPercentile(50),
      "sentence length mean" -> stat.getMean,
      "sentence length median" -> stat.getPercentile(50),
      "75th %ile sentence length" -> stat.getPercentile(75),
      "sentence length maximum" -> stat.getMax
      )
  }

  //average # of conjunctions used
  def conjunctionFrequency = {
    textDocument.lexicalTuple.toVector.
      filter(_._2._2.matches("CC")).
      map(_._2._1).length.                          //count all uses
      toDouble / textDocument.sentenceSize.toDouble         //normalized over number of sentences
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
    Map(
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

    Map(
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

    Map(
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

  def constituentCountStats = {
    val stat = new DescriptiveStatistics()
    this.getConstituents.map(_.size).map(stat.addValue(_))                //count

    Map(
      "minimum number of constituents in a sentence" -> stat.getMin,
      "25th %ile number of constituents in a sentence" -> stat.getPercentile(25),
      "mean number of constituents in a sentence" -> stat.getMean,
      "median number of constituents in a sentence" -> stat.getPercentile(50),
      "75th %ile number of constituents in a sentence" -> stat.getPercentile(75),
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

    Map(
      "constituent length minimum" -> stat.getMin,
      "25th %ile constituent length" -> stat.getPercentile(25),
      "constituent length mean" -> stat.getMean,
      "constituent length median" -> stat.getPercentile(50),
      "75th %ile constituent length" -> stat.getPercentile(75),
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
      val summedSentenceSimilarities = sentenceSimilarities.map(wordLevel => wordLevel.sum / wordLevel.length.toDouble)     //take similarity score for each word divided by number of words
      if (summedSentenceSimilarities.isEmpty) 0.0 else summedSentenceSimilarities.min                   //take the least similar score from the sentence
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

  def getClauseLengths = {
    //how to do?
    //use loop through and extract + tree.size
  }

  //# of clauses / # of sentences
  def getSentenceComplexityScore = {
    this.getClauseCounts.sum / textDocument.sentenceSize.toDouble
  }

  //return sentences grouped by structure
  def getClauseTypeCounts = {
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

  def getSentenceStructureTypes = {
    this.getClauseTypeCounts.map(sentence =>
      if (sentence._1._2 == 1 && sentence._2._2 == 0) "simple"
      else if (sentence._1._2 == 1 && sentence._2._2 >= 1) "complex"
      else if (sentence._1._2 >= 2 && sentence._2._2 == 0) "compound"
      else if (sentence._1._2 >= 2 && sentence._2._2 >= 1) "compound-complex"
      else "fragment"
    ).toVector
  }

  def sentenceStructureTypeStats = {
    val freq = new Frequency()
    this.getSentenceStructureTypes.map(freq.addValue(_))            //count

    Map(
      "ratio of simple sentences" -> freq.getPct("simple"),
      "ratio of complex sentences" -> freq.getPct("complex"),
      "ratio of compound sentences" -> freq.getPct("compound"),
      "ratio of compound-complex sentences" -> freq.getPct("compound-complex"),
      "ratio of fragments" -> freq.getPct("fragment")
      )
  }

  def makeSyntacticFeatureVector = {
    Vector(
        (textDocument.title, textDocument.title),
        (textDocument.gradeLevel, textDocument.gradeLevel),
        ("average number of conjunctions used per sentence", this.conjunctionFrequency),
        ("minimum sentence length", this.sentenceLengthStats("sentence length minimum")),
        ("25th %ile sentence length", this.sentenceLengthStats("25th %ile sentence length")),
        ("mean sentence length", this.sentenceLengthStats("sentence length mean")),
        ("median sentence length", this.sentenceLengthStats("sentence length median")),
        ("75th %ile sentence length", this.sentenceLengthStats("75th %ile sentence length")),
        ("maximum sentence length", this.sentenceLengthStats("sentence length maximum")),
        ("minimum tree size", this.treeSizeStats("minimum tree size")),
        ("25th %ile tree size", this.treeSizeStats("25th %ile tree size")),
        ("mean tree size", this.treeSizeStats("mean tree size")),
        ("median tree size", this.treeSizeStats("median tree size")),
        ("75th %ile tree size", this.treeSizeStats("75th %ile tree size")),
        ("maximum tree size", this.treeSizeStats("maximum tree size")),
        ("minimum tree depth", this.treeDepthStats("minimum tree depth")),
        ("25th %ile tree depth", this.treeDepthStats("25th %ile tree depth")),
        ("mean tree depth", this.treeDepthStats("mean tree depth")),
        ("median tree depth", this.treeDepthStats("median tree depth")),
        ("75th %ile tree depth", this.treeDepthStats("75th %ile tree depth")),
        ("maximum tree depth", this.treeDepthStats("maximum tree depth")),
        ("minimum distance to verb", this.distanceToVerbStats("minimum distance to verb")),
        ("25th %ile distance to verb", this.distanceToVerbStats("25th %ile distance to verb")),
        ("mean distance to verb", this.distanceToVerbStats("mean distance to verb")),
        ("median distance to verb", this.distanceToVerbStats("median distance to verb")),
        ("75th %ile distance to verb", this.distanceToVerbStats("75th %ile distance to verb")),
        ("maximum distance to verb", this.distanceToVerbStats("maximum distance to verb")),
        ("minimum number of constituents in a sentence", this.constituentCountStats("minimum number of constituents in a sentence")),
        ("25th %ile number of constituents in a sentence", this.constituentCountStats("25th %ile number of constituents in a sentence")),
        ("mean number of constituents in a sentence", this.constituentCountStats("mean number of constituents in a sentence")),
        ("median number of constituents in a sentence", this.constituentCountStats("median number of constituents in a sentence")),
        ("75th %ile number of constituents in a sentence", this.constituentCountStats("75th %ile number of constituents in a sentence")),
        ("maximum number of constituents in a sentence", this.constituentCountStats("maximum number of constituents in a sentence")),
        ("minimum constituent length", this.constituentLengthStats("constituent length minimum")),
        ("25th %ile constituent length", this.constituentLengthStats("25th %ile constituent length")),
        ("mean constituent length", this.constituentLengthStats("constituent length mean")),
        ("median constituent length", this.constituentLengthStats("constituent length median")),
        ("75th %ile constituent length", this.constituentLengthStats("75th %ile constituent length")),
        ("maximum constituent length", this.constituentLengthStats("constituent length maximum")),
        ("minimum word similarity sentence score", this.wordSimilaritySentenceScoreStats("minimum similarity sentence score")),
        ("25th %ile word similarity sentence score", this.wordSimilaritySentenceScoreStats("25th %ile similarity sentence score")),
        ("mean word similarity sentence score", this.wordSimilaritySentenceScoreStats("mean similarity sentence score")),
        ("median word similarity sentence score", this.wordSimilaritySentenceScoreStats("median similarity sentence score")),
        ("75th %ile similarity sentence score", this.wordSimilaritySentenceScoreStats("75th %ile similarity sentence score")),
        ("maximum similarity sentence score", this.wordSimilaritySentenceScoreStats("maximum similarity sentence score")),
        ("average number of clauses per sentence", this.getSentenceComplexityScore),
        ("% of simple sentences", this.sentenceStructureTypeStats("ratio of simple sentences")),
        ("% of complex sentences", this.sentenceStructureTypeStats("ratio of complex sentences")),
        ("% of compound sentences", this.sentenceStructureTypeStats("ratio of compound sentences")),
        ("% of compound-complex sentences", this.sentenceStructureTypeStats("ratio of compound-complex sentences")),
        ("% of fragments", this.sentenceStructureTypeStats("ratio of fragments"))
      )
  }

  def makeSyntacticMinusSimilarityFeatureVector = {
    Vector(
      (textDocument.title, textDocument.title),
      (textDocument.gradeLevel, textDocument.gradeLevel),
      ("average number of conjunctions used per sentence", this.conjunctionFrequency),
      ("minimum sentence length", this.sentenceLengthStats("sentence length minimum")),
      ("25th %ile sentence length", this.sentenceLengthStats("25th %ile sentence length")),
      ("mean sentence length", this.sentenceLengthStats("sentence length mean")),
      ("median sentence length", this.sentenceLengthStats("sentence length median")),
      ("75th %ile sentence length", this.sentenceLengthStats("75th %ile sentence length")),
      ("maximum sentence length", this.sentenceLengthStats("sentence length maximum")),
      ("minimum tree size", this.treeSizeStats("minimum tree size")),
      ("25th %ile tree size", this.treeSizeStats("25th %ile tree size")),
      ("mean tree size", this.treeSizeStats("mean tree size")),
      ("median tree size", this.treeSizeStats("median tree size")),
      ("75th %ile tree size", this.treeSizeStats("75th %ile tree size")),
      ("maximum tree size", this.treeSizeStats("maximum tree size")),
      ("minimum tree depth", this.treeDepthStats("minimum tree depth")),
      ("25th %ile tree depth", this.treeDepthStats("25th %ile tree depth")),
      ("mean tree depth", this.treeDepthStats("mean tree depth")),
      ("median tree depth", this.treeDepthStats("median tree depth")),
      ("75th %ile tree depth", this.treeDepthStats("75th %ile tree depth")),
      ("maximum tree depth", this.treeDepthStats("maximum tree depth")),
      ("minimum distance to verb", this.distanceToVerbStats("minimum distance to verb")),
      ("25th %ile distance to verb", this.distanceToVerbStats("25th %ile distance to verb")),
      ("mean distance to verb", this.distanceToVerbStats("mean distance to verb")),
      ("median distance to verb", this.distanceToVerbStats("median distance to verb")),
      ("75th %ile distance to verb", this.distanceToVerbStats("75th %ile distance to verb")),
      ("maximum distance to verb", this.distanceToVerbStats("maximum distance to verb")),
      ("minimum number of constituents in a sentence", this.constituentCountStats("minimum number of constituents in a sentence")),
      ("25th %ile number of constituents in a sentence", this.constituentCountStats("25th %ile number of constituents in a sentence")),
      ("mean number of constituents in a sentence", this.constituentCountStats("mean number of constituents in a sentence")),
      ("median number of constituents in a sentence", this.constituentCountStats("median number of constituents in a sentence")),
      ("75th %ile number of constituents in a sentence", this.constituentCountStats("75th %ile number of constituents in a sentence")),
      ("maximum number of constituents in a sentence", this.constituentCountStats("maximum number of constituents in a sentence")),
      ("minimum constituent length", this.constituentLengthStats("constituent length minimum")),
      ("25th %ile constituent length", this.constituentLengthStats("25th %ile constituent length")),
      ("mean constituent length", this.constituentLengthStats("constituent length mean")),
      ("median constituent length", this.constituentLengthStats("constituent length median")),
      ("75th %ile constituent length", this.constituentLengthStats("75th %ile constituent length")),
      ("maximum constituent length", this.constituentLengthStats("constituent length maximum")),
      ("average number of clauses per sentence", this.getSentenceComplexityScore),
      ("% of simple sentences", this.sentenceStructureTypeStats("ratio of simple sentences")),
      ("% of complex sentences", this.sentenceStructureTypeStats("ratio of complex sentences")),
      ("% of compound sentences", this.sentenceStructureTypeStats("ratio of compound sentences")),
      ("% of compound-complex sentences", this.sentenceStructureTypeStats("ratio of compound-complex sentences")),
      ("% of fragments", this.sentenceStructureTypeStats("ratio of fragments"))
    )
  }

  def similarityFeatureVector = {
    Vector(
      ("minimum word similarity sentence score", this.wordSimilaritySentenceScoreStats("minimum similarity sentence score")),
      ("25th %ile word similarity sentence score", this.wordSimilaritySentenceScoreStats("25th %ile similarity sentence score")),
      ("mean word similarity sentence score", this.wordSimilaritySentenceScoreStats("mean similarity sentence score")),
      ("median word similarity sentence score", this.wordSimilaritySentenceScoreStats("median similarity sentence score")),
      ("75th %ile similarity sentence score", this.wordSimilaritySentenceScoreStats("75th %ile similarity sentence score")),
      ("maximum similarity sentence score", this.wordSimilaritySentenceScoreStats("maximum similarity sentence score"))
    )
  }
}
