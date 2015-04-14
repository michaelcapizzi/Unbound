package TextComplexity

import java.io.{ByteArrayInputStream, InputStreamReader, InputStream}
import edu.arizona.sista.learning.Datum
import edu.arizona.sista.processors.corenlp.CoreNLPProcessor
import edu.arizona.sista.struct.Counter
import edu.stanford.nlp.trees.tregex.TregexPattern
import edu.stanford.nlp.trees.{CollinsHeadFinder, MemoryTreebank, DiskTreebank, Tree}
import org.apache.commons.math3.stat.Frequency
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import spire.std.tuples
import Concreteness._
import scala.collection.mutable


/**
 * Created by mcapizzi on 4/5/15.
 */

//each item in vector is a full paragraph
class TextDocument(val text: Vector[String], val processor: CoreNLPProcessor, val document: Vector[edu.arizona.sista.processors.Document], val author: String, val title: String, val chapter: String, val gradeLevel: String) {

  def fullText = {
    text.mkString(" ")
  }

  ////////////////////////// access Processors  //////////////////////////

  def annotate = {
    document.map(processor.annotate)
  }

  //TODO build method to retrieve from file
  def retrieveAnnotation = {
    //def getBufferedReader(filename: String): BufferedReader = {
    //val fileStream = new FileInputStream(filename);
    //val gzipStream = new GZIPInputStream(fileStream);
    //val decoder = new InputStreamReader(gzipStream, "UTF-8");
    //new BufferedReader(decoder);
    //}
  }

  ////////////////////////// for normalizing //////////////////////////

  def lexicalTuple = {
    this.document.map(_.sentences.map(_.words.toVector)).flatten.flatten zip             //the word
      (
        this.document.map(_.sentences.map(_.lemmas.get.toVector)).flatten.flatten,         //the lemma
        this.document.map(_.sentences.map(_.tags.get.toVector)).flatten.flatten,           //the POS tag
        this.document.map(_.sentences.map(_.entities.get.toVector)).flatten.flatten        //the NER label
        ).zipped.toVector
    }

  def lexicalTupleInSentences = {
    for (sentence <- this.document.map(_.sentences).flatten) yield {
        sentence.words.toVector zip
          (
            sentence.lemmas.get.toVector,
            sentence.tags.get.toVector,
            sentence.entities.get.toVector
            ).zipped.toVector
      }
  }

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


}
