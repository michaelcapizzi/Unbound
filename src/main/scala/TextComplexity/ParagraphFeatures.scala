package TextComplexity

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

  //TODO build better methods here

  def getDiscourseTrees = {
    textDocument.document.map(_.discourseTree).map(_.get)
  }

  def getDiscourseRelations = {
    val discourseRelationsRaw =
      this.getDiscourseTrees.map(_.           //get discourse trees
        toString.split("\n").map(_.             //split by line
        trim)).map(paragraph =>                 //remove whitespace
        paragraph.filterNot(_.                  //remove text, keeping details
          startsWith("TEXT")))
    for (paragraph <- discourseRelationsRaw) yield {
      if (paragraph.nonEmpty) {
        val relationsRegex = """\s*(\w+-?\w+)( \(\w+\))?""".r
        paragraph.map(each =>
          each match {
            case relationsRegex(relation, direction) => (relation, direction)
          }
        )
      }
    }
  }
}
