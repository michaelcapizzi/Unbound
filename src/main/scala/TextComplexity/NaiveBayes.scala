package TextComplexity

import scala.math._


/**
 * Created by mcapizzi on 4/15/15.
 */
class NaiveBayes(val trainingData: Vector[TextDocument], val testDocument: TextDocument, val stopWords: Vector[String], countFrequencyThreshold: Int, documentFrequencyThreshold: Int, mutualInformationThreshold: Int) {

  //TODO implement feature selection parameters

  //total # of documents
  def trainingDataSize = {
    trainingData.length.toDouble
  }

  //all possible classes
  def possibleClasses = {
    trainingData.map(_.gradeLevel).distinct
  }

  //class counts
  def classCounts = {
    (for (possibleClass <- this.possibleClasses) yield {
      possibleClass -> trainingData.count(doc => doc.gradeLevel == possibleClass).toDouble
    }).toMap
  }

  //calculate class *prior probabilities*
  def priorProbabilities = {
    (for (possibleClass <- this.classCounts.keySet) yield {
      possibleClass -> classCounts(possibleClass) / this.trainingDataSize             //number of documents in a class / total number of documents
    }).toMap
  }

  //extract all vocabulary from all documents in training
  def extractVocabulary = {
    trainingData.map(textDocument =>                                                  //for each document,
      textDocument.getWords.map(                                                        //get words
      _.toLowerCase)).flatten.distinct.                                                 //put all to lowercase, flatten, and take only distinct
      diff(stopWords)                                                                   //filter out stop words
  }

  //concatenate all documents for each class into one document
  def makeDocumentsConcatenized = {
    (for (possibleClass <- possibleClasses) yield {                                    //for each class
      possibleClass -> trainingData.filter(doc => doc.gradeLevel == possibleClass).map(                  //take the documents of that class
        textDocument => textDocument.getWords.map(_.toLowerCase)).flatten.               //get words, put to lowercase, and flatten
        diff(stopWords)                                                                  //filter out stop words
    }).toMap
  }

  //calculate *conditional probabilities*
  def conditionalProbabilities = {
    val vocabulary = this.extractVocabulary
    val docConcat = this.makeDocumentsConcatenized

    for (possibleClass <- possibleClasses) yield {
      val smoothingDenominator = docConcat(possibleClass).length.toDouble + vocabulary.length.toDouble
      (
        possibleClass,                                                                                                    //the class
        1d / smoothingDenominator, {                                                                                      //the smoothing value for that class
        vocabulary.map(word =>                                                                                            //for each word
          word -> (docConcat(possibleClass).count(item => item == word) + 1).toDouble / smoothingDenominator.toDouble           //the count in the class + 1 / smoothingDenominator
        )
      }.toMap
        )
    }
  }

  //tokenize test document
  def testDocumentTokenize = {
    testDocument.getWords.map(_.toLowerCase)
  }

  //calculate score of test document
  def testScores = {
    val priors = this.priorProbabilities
    val concatenizedDocs = this.makeDocumentsConcatenized
    val tokenizedTestDoc = this.testDocumentTokenize
    for (individualClass <- this.possibleClasses) yield {                                       //for each class
      val conditionalProbs = this.conditionalProbabilities.find(_._1 == individualClass).get
      (
        individualClass,                                                                          //class name
        log(priors(individualClass)) + tokenizedTestDoc.map(word =>                               //for each word
          conditionalProbs._3.getOrElse(word, conditionalProbs._2)).map(number =>                   //lookup conditional probability or use smoothing value
          log(number.toDouble)).sum                                                                          //take the log and sum
      )
    }
  }

  def argMax = {
    val sorted = this.testScores.sortBy(_._2).reverse
    sorted.head._1
  }












}
