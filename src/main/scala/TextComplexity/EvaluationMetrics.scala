package TextComplexity

import scala.util.Random._
import scala.collection.mutable

/**
 * Created by mcapizzi on 4/17/15.
 */
class EvaluationMetrics(fullScoreList: Vector[(String, Vector[(String, String, String)])]) {

  //Note: must have separate instance for each model used --> mlScoreList from machineLearning instance is Vector[(String, Vector[(String, String, String)])]

  //scoreList.map(ml => (ml._1 -> eval.[method](ml._2)))

  val possibleLabels = fullScoreList.head._2.map(_._3).distinct

  def accuracy(mlScoreList: Vector[(String, String, String)]) = {
    def isAccurate(mlScore: String, actualScore: String): Int = {
      if (mlScore == actualScore) 1 else 0
    }

    (mlScoreList.map(item => isAccurate(item._2, item._3)).sum.toDouble / mlScoreList.length.toDouble) * 100          //sum up correct and divide by total number of items then multiply by 100
  }

  //TODO build
  def distanceAccuracy = {
    //score of +/- 1,2,3 representing how far off
  }


  def relevanceLabels(mlScoreList: Vector[(String, String, String)]): Map[String, Vector[String]] = {
    def determineRelevanceLabels(relevantClass: String, mlScore: String, actualScore: String): String = {
      if (relevantClass == actualScore & relevantClass == mlScore) "truePositive"           //it was relevant, and it was correctly scored as relevant
      else if (relevantClass != actualScore & relevantClass == mlScore) "falsePositive"     //it was irrelevant, but it was incorrectly scored as relevant
      else if (relevantClass == actualScore & relevantClass != mlScore) "falseNegative"     //it was relevant, but it was incorrectly scored as irrelevant
      else "trueNegative"
    }

    (for (label <- possibleLabels) yield {                                                      //for every possible label
      label -> mlScoreList.map(score => determineRelevanceLabels(label, score._2, score._3))      //generate relevance tags for each item
    }).toMap
  }


  def recall(mlScoreList: Vector[(String, String, String)]) = {
    def calculateRecall(truePositive:Double, falseNegative: Double): Double = {
      if ((truePositive + falseNegative) == 0) 0                                            //in case denominator is 0
      else truePositive / (truePositive + falseNegative)                                    //otherwise calculate recall
    }

    val relevanceLabelsMap = relevanceLabels(mlScoreList)
    (for (relevance <- relevanceLabelsMap.keySet.toList) yield {
      relevance -> calculateRecall(relevanceLabelsMap(relevance).count(_.matches("truePositive")).toDouble, relevanceLabelsMap(relevance).count(_.matches("falseNegative")).toDouble)
    }).toMap
  }


  def precision(mlScoreList: Vector[(String, String, String)]) = {
    def calculatePrecision(truePositive: Double, falsePositive: Double): Double = {
      if ((truePositive + falsePositive) == 0) 0 //in case denominator is 0
      else truePositive / (truePositive + falsePositive) //otherwise calculate recall
    }

    val relevanceLabelsMap = relevanceLabels(mlScoreList)
    (for (relevance <- relevanceLabelsMap.keySet.toList) yield {
      relevance -> calculatePrecision(relevanceLabelsMap(relevance).count(_.matches("truePositive")).toDouble, relevanceLabelsMap(relevance).count(_.matches("falsePositive")).toDouble)
    }).toMap
  }


  def calculateF1(precisionScore: Double, recallScore: Double): Double = {
    if ((precisionScore + recallScore) == 0) 0                                            //in case denominator is 0
    else (2 * precisionScore * recallScore) / (precisionScore + recallScore)              //otherwise calculate recall
  }

  def f1(mlScoreList: Vector[(String, String, String)]) = {
    val relevanceLabelsMap = relevanceLabels(mlScoreList)
    (for (relevance <- relevanceLabelsMap.keySet.toList) yield {
      val precisionScore = precision(mlScoreList)(relevance)
      val recallScore = recall(mlScoreList)(relevance)
      relevance -> calculateF1(precisionScore, recallScore)
    }).toMap
  }

  def macroScores(mlScoreList: Vector[(String, String, String)]) = {
    val macroPrecision = precision(mlScoreList: Vector[(String, String, String)]).values.toList.sum / possibleLabels.length
    val macroRecall = recall(mlScoreList: Vector[(String, String, String)]).values.toList.sum / possibleLabels.length
    Map(
      "macroPrecision" -> macroPrecision,
      "macroRecall" -> macroRecall,
      "macroF1" -> calculateF1(macroPrecision, macroRecall)
    )
  }

  //NOTE: may be inaccurate...

  //to extract
  //metrics(key) --> will yield you an Any of all three
  //to go deeper
  //metrics(key).asInstanceOf[Map[???]](key)



  def fullEvaluation(mlScoreList: Vector[(String, String, String)]) = {

  }



}
