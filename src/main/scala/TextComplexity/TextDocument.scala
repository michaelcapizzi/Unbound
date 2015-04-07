package TextComplexity

import edu.arizona.sista.learning.Datum
import edu.arizona.sista.processors.corenlp.CoreNLPProcessor
import edu.arizona.sista.struct.Counter
import edu.stanford.nlp.trees.Tree
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics


/**
 * Created by mcapizzi on 4/5/15.
 */

//each item in vector is a full paragraph
class TextDocument(text: Vector[String], processor: CoreNLPProcessor, document: Vector[edu.arizona.sista.processors.Document], author: String, title: String, chapter: String) {

  ////////////////////////// w/o annotation //////////////////////////

  //TODO test
  def fullText = {
    text.mkString(" ")
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

  def tuplePOS = {
    (
      document.map(_.sentences.map(_.words)).flatten.flatten,
      document.map(_.sentences.map(_.lemmas.get.toVector)).flatten.flatten,
      document.map(_.sentences.map(_.tags.get.toVector)).flatten.flatten
    ).zipped
  }

  //# of total words
  def wordCount = {
    this.tuplePOS.toVector.
      map(_._1).                                  //get the tokens
      count(_.matches("[A-Za-z]+"))               //only count words (not punctuation)
  }

  //# of total lemmas
  def lemmaCount = {
    this.tuplePOS.toVector.
      map(_._2).                                  //get the lemmas
      count(_.matches("[A-Za-z]+"))               //only count words (not punctuation)
  }

  //# of total distinct lemmas by part of speech
  def countDistinctPOS(pos: String) = {
    this.tuplePOS.toVector.
      filter(_._3.matches(pos)).              //take only desired POS - use regex
      map(_._2).                              //extract just the lemmas from tuple
      distinct.length                         //count distinct
  }

  //# of distinct word families
  def wordFamilyCount = {
    //stemmer? to detect word families? can I do it?
  }


  ////////////////////////// syntactic //////////////////////////

  //# of sentences
  def sentenceSize = {
    document.map(_.sentences.length).sum
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

  }

  def constituentsCount = {

  }

  def constituentLengthStats = {

  }

  def getParseTrees = {
    document.map(_.sentences.map(_.syntacticTree.toString).map(Tree.valueOf))
  }

  def getTreeDepths = {

  }

  def getTreeSizes = {

  }

}
