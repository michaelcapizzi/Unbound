package TextComplexity

import scala.util.Random._
import scala.collection.mutable

/**
 * Created by mcapizzi on 4/17/15.
 */
class EvaluationMetrics(scoreList: Vector[(String, String, String)]) {

  //Note: must have separate instance for each model used

  //TODO test all

  val possibleLabels = scoreList.map(_._3).distinct

  def accuracy = {
    def isAccurate(mlScore: String, actualScore: String): Int = {
      if (mlScore == actualScore) 1 else 0
    }

    (scoreList.map(item => isAccurate(item._2, item._3)).sum.toDouble / scoreList.length.toDouble) * 100          //sum up correct and divide by total number of items then multiply by 100
  }

  //TODO build
  def distanceAccuracy = {
    //score of +/- 1,2,3 representing how far off
  }


  def relevanceLabels = {
    def determineRelevanceLabels(relevantClass: String, mlScore: String, actualScore: String): String = {
      if (relevantClass == actualScore & relevantClass == mlScore) "truePositive"           //it was relevant, and it was correctly scored as relevant
      else if (relevantClass != actualScore & relevantClass == mlScore) "falsePositive"     //it was irrelevant, but it was incorrectly scored as relevant
      else if (relevantClass == actualScore & relevantClass != mlScore) "falseNegative"     //it was relevant, but it was incorrectly scored as irrelevant
      else "trueNegative"
    }

    (for (label <- possibleLabels) yield {                                                      //for every possible label
      label -> scoreList.map(score => determineRelevanceLabels(label, score._2, score._3))      //generate relevance tags for each item
    }).toMap
  }


  def recall = {
    def calculateRecall(truePositive:Double, falseNegative: Double): Double = {
      if ((truePositive + falseNegative) == 0) 0                                            //in case denominator is 0
      else truePositive / (truePositive + falseNegative)                                    //otherwise calculate recall
    }

    val relevanceLabels = this.relevanceLabels
    (for (relevance <- relevanceLabels.keySet.toList) yield {
      relevance -> calculateRecall(relevanceLabels(relevance).count(_.matches("truePositive")).toDouble, relevanceLabels(relevance).count(_.matches("falseNegative")).toDouble)
    }).toMap
  }


  def precision = {
    def calculatePrecision(truePositive: Double, falsePositive: Double): Double = {
      if ((truePositive + falsePositive) == 0) 0 //in case denominator is 0
      else truePositive / (truePositive + falsePositive) //otherwise calculate recall
    }

    val relevanceLabels = this.relevanceLabels
    (for (relevance <- relevanceLabels.keySet.toList) yield {
      relevance -> calculatePrecision(relevanceLabels(relevance).count(_.matches("truePositive")).toDouble, relevanceLabels(relevance).count(_.matches("falsePositive")).toDouble)
    }).toMap
  }


  def calculateF1(precisionScore: Double, recallScore: Double): Double = {
    if ((precisionScore + recallScore) == 0) 0                                            //in case denominator is 0
    else (2 * precisionScore * recallScore) / (precisionScore + recallScore)              //otherwise calculate recall
  }

  def f1 = {
    val relevanceLabels = this.relevanceLabels
    (for (relevance <- relevanceLabels.keySet.toList) yield {
      relevance -> calculateF1(this.precision(relevance), this.recall(relevance))
    }).toMap
  }

  def macroScores = {
    val macroPrecision = this.precision.values.toList.sum / possibleLabels.length
    val macroRecall = this.recall.values.toList.sum / possibleLabels.length
    Map(
      "macroPrecision" -> macroPrecision,
      "macrosRecall" -> macroRecall,
      "macroF1" -> calculateF1(macroPrecision, macroRecall)
    )
  }

  //NOTE: may be inaccurate...

  //to extract
  //metrics(key) --> will yield you an Any of all three
  //to go deeper
  //metrics(key).asInstanceOf[Map[???]](key)






}
