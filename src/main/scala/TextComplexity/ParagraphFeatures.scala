package TextComplexity

import org.apache.commons.math3.stat.Frequency
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics

import scala.util.matching.Regex

/**
 * Created by mcapizzi on 4/7/15.
 */
class ParagraphFeatures(textDocument: TextDocument) {

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

  //discourse
  //https://github.com/sistanlp/processors/blob/master/src/main/scala/edu/arizona/sista/discourse/rstparser/DiscourseTree.scala

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


  //TODO add counts for each relation type
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

    val relationCounts = tuple.map(_._2).flatten.map(relation =>            //count of each relation
        relation -> relationFreq.getCount(relation)
      ).distinct

    Map(
      "minimum number of relations per sentence" -> relationCountStats.getMin,
      "25th %ile number of relations per sentence" -> relationCountStats.getPercentile(25),
      "mean number of relations per sentence" -> relationCountStats.getMean,
      "median number of relations per sentence" -> relationCountStats.getPercentile(50),
      "75th %ile number of relations per sentence" -> relationCountStats.getPercentile(75),
      "maximum number of relations per sentence" -> relationCountStats.getMax,
      "percentage of L->R relations in document" -> directionRatio("(LeftToRight)"),
      "percentage of R->L relations in document" -> directionRatio("(RightToLeft)"),
      "percentage of directionless relations in document" -> directionRatio("n/a")/*,
      "number of 'attribution' relations normalized over paragraph size" */
    )

  }
  //count empty Arrays --> indicates no relations
  //count relation types and directions

}