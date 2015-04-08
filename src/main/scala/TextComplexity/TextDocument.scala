package TextComplexity

import edu.arizona.sista.learning.Datum
import edu.arizona.sista.processors.corenlp.CoreNLPProcessor
import edu.arizona.sista.struct.Counter
import edu.stanford.nlp.trees.Tree
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import scala.collection.JavaConverters._


/**
 * Created by mcapizzi on 4/5/15.
 */

//each item in vector is a full paragraph
class TextDocument(text: Vector[String], processor: CoreNLPProcessor, document: Vector[edu.arizona.sista.processors.Document], author: String, title: String, chapter: String) {

  def fullText = {
    text.mkString(" ")
  }

  ////////////////////////// for normalizing //////////////////////////

  //# of total words
  def wordCount = {
    this.lexicalTuple.toVector.
      map(_._1).                                  //get the tokens
      count(_.matches("[A-Za-z]+"))               //only count words (not punctuation)
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

  ////////////////////////// lexical //////////////////////////

  //compare to NaiveBayes piped as features

  def lexicalTuple = {
    document.map(_.sentences.map(_.words.toVector)).flatten.flatten zip
    (
      document.map(_.sentences.map(_.lemmas.get.toVector)).flatten.flatten,
      document.map(_.sentences.map(_.tags.get.toVector)).flatten.flatten,
      document.map(_.sentences.map(_.entities.get.toVector)).flatten.flatten
    ).zipped.toVector
  }

  //# of total distinct lemmas by part of speech
      //verb (VB.*)
      //adjective (JJ.*)
      //conjunctions (CC)
  def countDistinctPOS(pos: String) = {
    this.lexicalTuple.toVector.
      filter(_._2._2.matches(pos)).             //take only desired POS - use regex
      map(_._2._1).                             //extract just the lemmas from tuple
      distinct.length.                          //count distinct
      toFloat / this.wordCount.toFloat          //normalize over wordCount
  }

  //total # of conjunctions used
  //TODO normalize over # of sentences
  def conjunctionsFrequency = {
    this.lexicalTuple.toVector.
    filter(_._2._2.matches("CC")).
    map(_._2._1).length.                        //count all uses
    toFloat / this.wordCount.toFloat            //normalize over wordCount
  }

  //TODO normalize over wordCount
  //# of distinct word families
  def wordFamilyCount = {
    //stemmer? to detect word families? can I do it?
  }

  //word concreteness
  def getWordConcreteness = {
    //
  }

  //TODO normalize over wordCount
  def wordConcretenessStats = {
    //use lemmas
  }

  //# of distinct Named Entities
    // could be an approximation of characters?
  //TODO normalize over wordCounts

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
    this.getSentenceLengths.map(stat.addValue(_))
    (
      "sentence length mean" -> stat.getMean,
      "sentence length median" -> stat.getPercentile(50),
      "sentence length minimum" -> stat.getMin,
      "sentence length maximum" -> stat.getMax
    )
  }

  def getConstituents = {
    this.getParseTrees.map(_.map(_.constituents)).flatten
  }

  def constituentsCountStats = {
    val stat = new DescriptiveStatistics()
    this.getConstituents.map(_.size).map(stat.addValue(_))
    (
      "mean number of constituents per sentence" -> stat.getMean,
      "median number of constituents per sentence" -> stat.getPercentile(50),
      "minimum number of constituents in a sentence" -> stat.getMin,
      "maximum number of constituents in a sentence" -> stat.getMax
    )
  }

  def getConstituentLengths = {
    this.getConstituents.map(
    _.asScala.toVector).map(
    constituent =>
      for (c <- constituent) yield c.size).flatten
  }

  def constituentLengthStats = {
    val stat = new DescriptiveStatistics()
    this.getConstituentLengths.map(stat.addValue(_))
    (
      "constituent length mean" -> stat.getMean,
      "constituent length median" -> stat.getPercentile(50),
      "constituent length minimum" -> stat.getMin,
      "constituent length maximum" -> stat.getMax
      )
  }

  def getParseTrees = {
    document.map(_.sentences.map(_.syntacticTree.toString).map(Tree.valueOf))
  }

  def getTreeSizes = {
    //
  }

  def treeSizeStats = {
    //
  }

  def getTreeDepths = {
    //
  }

  def treeDepthStats = {
    //
  }

  ////////////////////////// paragraph //////////////////////////

  //coreference

  //discourse
  //https://github.com/sistanlp/processors/blob/master/src/main/scala/edu/arizona/sista/discourse/rstparser/DiscourseTree.scala


  ////////////////////////// document //////////////////////////


}
