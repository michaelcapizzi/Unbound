package TextComplexity

import edu.arizona.sista.discourse.rstparser.DiscourseTree
import org.apache.commons.math3.stat.Frequency
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics

import scala.util.matching.Regex

/**
 * Created by mcapizzi on 4/7/15.
 */
class ParagraphFeatures(val textDocument: TextDocument) {

  //paragraph size stats
  def getParagraphLengths = {
    textDocument.document.map(_.sentences.length)
  }

  def paragraphLengthStats = {
    val stat = new DescriptiveStatistics()
    this.getParagraphLengths.map(stat.addValue(_))
    Map(
      "minimum paragraph length" -> stat.getMin,
      "25th %ile paragraph length" -> stat.getPercentile(25),
      "mean paragraph length" -> stat.getMean,
      "median paragraph length" -> stat.getPercentile(50),
      "75th %ile paragraph length" -> stat.getPercentile(75),
      "maximum paragraph length" -> stat.getMax
    )
  }

  def getDiscourseTrees = {
    textDocument.document.map(_.discourseTree).map(_.get)
  }

  def getDiscourseRelations = {
    this.getDiscourseTrees.map(_.                     //get discourse trees
      toString.split("\n").map(_.trim)).map(_.        //to String and split at new lines, trim whitespace
      filterNot(_.startsWith("TEXT")).map(_.          //remove simple text lines (e.g. no relations)
      split(" ")).map(item =>                         //split relation from direction
      ( //make tuple
        item.head,
        if (item.length == 1) "n/a" else item(1)      //for relations with no tuple, put "n/a"
        )
      ))
  }

  //TODO count empty Arrays? --> indicates no relations
  def discourseRelationsStats = {
    val paragraphLengths = this.getParagraphLengths
    val relations = this.getDiscourseRelations.map(_.map(_._1))
    val directions = this.getDiscourseRelations.map(_.map(_._2))
    val tuple = (paragraphLengths, relations, directions).zipped.toVector

    val relationCountStats = new DescriptiveStatistics()
    val relationCount = tuple.map(paragraph =>                              //number of relations per paragraph normalized over paragraph size
      paragraph._2.length.toDouble / paragraph._1.toDouble)
    relationCount.map(relationCountStats.addValue)

    val directionFreq = new Frequency()
    tuple.map(_._3).flatten.map(directionFreq.addValue)                     //count directions
    val relationFreq = new Frequency()
    tuple.map(_._2).flatten.map(relationFreq.addValue)                      //count relations

    val directionRatio = tuple.map(_._3).flatten.map(direction =>           //ratio of each direction (including n/a)
        direction -> directionFreq.getPct(direction)
      ).distinct.toMap                                                        //make map

    val relationTypes = tuple.map(_._2).flatten.map(relation =>            //count of each relation
        relation -> relationFreq.getCount(relation) / tuple.map(_._2).flatten.length.toDouble   //percent of each relation type
      ).distinct.toMap

    Map(
      "minimum number of relations per sentence" -> relationCountStats.getMin,
      "25th %ile number of relations per sentence" -> relationCountStats.getPercentile(25),
      "mean number of relations per sentence" -> relationCountStats.getMean,
      "median number of relations per sentence" -> relationCountStats.getPercentile(50),
      "75th %ile number of relations per sentence" -> relationCountStats.getPercentile(75),
      "maximum number of relations per sentence" -> relationCountStats.getMax,
      "percentage of L->R relations in document" -> directionRatio.getOrElse("(LeftToRight)", 0d),
      "percentage of R->L relations in document" -> directionRatio.getOrElse("(RightToLeft)", 0d),
      "percentage of directionless relations in document" -> directionRatio.getOrElse("n/a", 0d),
      "percent of 'span' relations normalized over paragraph size" -> relationTypes.getOrElse("span", 0.0),
      "percent of 'comparison' relations in text" -> relationTypes.getOrElse("comparison", 0.0),
      "percent of 'background' relations in text" -> relationTypes.getOrElse("background", 0.0),
      "percent of 'textual-organization' relations in text" -> relationTypes.getOrElse("textual-organization", 0.0),
      "percent of 'joint' relations in text" -> relationTypes.getOrElse("joint", 0.0),
      "percent of 'attribution' relations in text" -> relationTypes.getOrElse("attribution", 0.0),
      "percent of 'enablement' relations in text" -> relationTypes.getOrElse("enablement", 0.0),
      "percent of 'condition' relations in text" -> relationTypes.getOrElse("condition", 0.0),
      "percent of 'temporal' relations in text" -> relationTypes.getOrElse("temporal", 0.0),
      "percent of 'explanation' relations in text" -> relationTypes.getOrElse("explanation", 0.0),
      "percent of 'cause' relations in text" -> relationTypes.getOrElse("cause", 0.0),
      "percent of 'contrast' relations in text" -> relationTypes.getOrElse("contrast", 0.0),
      "percent of 'evaluation' relations in text" -> relationTypes.getOrElse("evaluation", 0.0),
      "percent of 'topic-change' relations in text" -> relationTypes.getOrElse("topic-change", 0.0),
      "percent of 'same-unit' relations in text" -> relationTypes.getOrElse("same-unit", 0.0),
      "percent of 'manner-means' relations in text" -> relationTypes.getOrElse("manner-means", 0.0),
      "percent of 'summary' relations in text" -> relationTypes.getOrElse("summary", 0.0),
      "percent of 'topic-comment' relations in text" -> relationTypes.getOrElse("topic-comment", 0.0),
      "percent of 'elaboration' relations in text" -> relationTypes.getOrElse("elaboration", 0.0)
    )
  }

  def makeParagraphFeatureVector = {
    Vector(
      (textDocument.title, textDocument.title),
      (textDocument.gradeLevel, textDocument.gradeLevel),
      ("minimum paragraph length", this.paragraphLengthStats("minimum paragraph length")),
      ("25th %ile paragraph length", this.paragraphLengthStats("25th %ile paragraph length")),
      ("mean paragraph length", this.paragraphLengthStats("mean paragraph length")),
      ("median paragraph length", this.paragraphLengthStats("median paragraph length")),
      ("75th %ile paragraph length", this.paragraphLengthStats("75th %ile paragraph length")),
      ("maximum paragraph length", this.paragraphLengthStats("maximum paragraph length")),
      ("minimum number of relations per sentence", this.discourseRelationsStats("minimum number of relations per sentence")),
      ("25th %ile number of relations per sentence", this.discourseRelationsStats("25th %ile number of relations per sentence")),
      ("mean number of relations per sentence", this.discourseRelationsStats("mean number of relations per sentence")),
      ("median number of relations per sentence", this.discourseRelationsStats("median number of relations per sentence")),
      ("75th %ile number of relations per sentence", this.discourseRelationsStats("75th %ile number of relations per sentence")),
      ("maximum number of relations per sentence", this.discourseRelationsStats("maximum number of relations per sentence")),
      ("percentage of L->R relations in document", this.discourseRelationsStats("percentage of L->R relations in document")),
      ("percentage of R->L relations in document", this.discourseRelationsStats("percentage of R->L relations in document")),
      ("percentage of directionless relations in document", this.discourseRelationsStats("percentage of directionless relations in document")),
      ("percent of 'span' relations normalized over paragraph size", this.discourseRelationsStats("percent of 'span' relations normalized over paragraph size")),
      ("percent of 'comparison' relations in text", this.discourseRelationsStats("percent of 'comparison' relations in text")),
      ("percent of 'background' relations in text", this.discourseRelationsStats("percent of 'background' relations in text")),
      ("percent of 'textual-organization' relations in text", this.discourseRelationsStats("percent of 'textual-organization' relations in text")),
      ("percent of 'joint' relations in text", this.discourseRelationsStats("percent of 'joint' relations in text")),
      ("percent of 'attribution' relations in text", this.discourseRelationsStats("percent of 'attribution' relations in text")),
      ("percent of 'enablement' relations in text", this.discourseRelationsStats("percent of 'enablement' relations in text")),
      ("percent of 'condition' relations in text", this.discourseRelationsStats("percent of 'condition' relations in text")),
      ("percent of 'temporal' relations in text", this.discourseRelationsStats("percent of 'temporal' relations in text")),
      ("percent of 'explanation' relations in text", this.discourseRelationsStats("percent of 'explanation' relations in text")),
      ("percent of 'cause' relations in text", this.discourseRelationsStats("percent of 'cause' relations in text")),
      ("percent of 'contrast' relations in text", this.discourseRelationsStats("percent of 'contrast' relations in text")),
      ("percent of 'evaluation' relations in text", this.discourseRelationsStats("percent of 'evaluation' relations in text")),
      ("percent of 'topic-change' relations in text", this.discourseRelationsStats("percent of 'topic-change' relations in text")),
      ("percent of 'same-unit' relations in text", this.discourseRelationsStats("percent of 'same-unit' relations in text")),
      ("percent of 'manner-means' relations in text", this.discourseRelationsStats("percent of 'manner-means' relations in text")),
      ("percent of 'summary' relations in text", this.discourseRelationsStats("percent of 'summary' relations in text")),
      ("percent of 'topic-comment' relations in text", this.discourseRelationsStats("percent of 'topic-comment' relations in text")),
      ("percent of 'elaboration' relations in text", this.discourseRelationsStats("percent of 'elaboration' relations in text"))
    )
  }

}