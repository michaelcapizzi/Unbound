package TextComplexity

import edu.arizona.sista.processors.corenlp.CoreNLPProcessor

import scala.util.matching.Regex


/**
 * Created by mcapizzi on 4/5/15.
 */
class TextDocument(text: Vector[Vector[String]], processor: CoreNLPProcessor, document: Vector[edu.arizona.sista.processors.Document], author: String, title: String, chapter: String) {

  //without annotation

  def fullText = {
    text.flatten
  }

  def paragraphSize = {
    text.length
  }

  def sentenceSize = {
    text.flatten.length
  }

  //using Processors

  def annotate = {
    document.map(processor.annotate(_))
  }

  //lexical

  def tuplePOS = {
    (
      document.map(_.sentences.map(_.words)).flatten.flatten,
      document.map(_.sentences.map(_.lemmas.get.toVector)).flatten.flatten,
      document.map(_.sentences.map(_.tags.get.toVector)).flatten.flatten
    ).zipped
  }

  def wordCount = {
    this.tuplePOS.toVector.
      map(_._1).                                  //get the tokens
      count(_.matches("[A-Za-z]+"))               //only count words (not punctuation)
  }

  def lemmaCount = {
    this.tuplePOS.toVector.
      map(_._2).                                  //get the lemmas
      count(_.matches("[A-Za-z]+"))               //only count words (not punctuation)
  }

  def wordFamilyCount = {
    //stemmer? to detect word families? can I do it?
  }

  def countDistinctPOS(pos: String) = {
    this.tuplePOS.toVector.
      filter(_._3.matches(pos)).              //take only desired POS - use regex
      map(_._2).                              //extract just the lemmas from tuple
      distinct.length                         //count distinct
  }


}
