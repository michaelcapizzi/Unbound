package TextComplexity

import scala.math._


/**
 * Created by mcapizzi on 4/15/15.
 */
class NaiveBayes(val trainingData: Vector[TextDocument], val testDocument: TextDocument, val stopWords: Vector[String], featureFrequencyThreshold: Int, mutualInformationThreshold: Int) {

  //TODO build test from NBSample in SISTA555 - project 2
  //TODO why errors when trying to access doubles?

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
      val smoothing = docConcat(possibleClass).length + vocabulary.length.toDouble
      (
        possibleClass,
        1.0 / smoothing, {
        vocabulary.map(word =>
          word -> (docConcat(possibleClass).count(item => item == word) + 1).toDouble / smoothing
        )
      }.toMap
        )
    }
  }

  //tokenize test document
  def testDocumentTokenize = {
    testDocument.getWords
  }

  //calculate score of test document
  def getTestScores = {
    val priors = this.priorProbabilities
    val concatenizedDocs = this.makeDocumentsConcatenized
    val tokenizedTestDoc = this.testDocumentTokenize
    for (individualClass <- this.possibleClasses) yield {                                       //for each class
      val conditionalProbs = this.conditionalProbabilities.find(_._1 == individualClass).get
      (
        individualClass,                                                                          //class name
        log(priors(individualClass)) + tokenizedTestDoc.map(word =>                               //for each word
          conditionalProbs._3.getOrElse(word, conditionalProbs._2)).map(number =>                   //lookup conditional probability
          log(number)).sum                                                                          //take the log and sum
      )
    }
  }

  def determineArgMax = {
    val sorted = this.getTestScores.sortBy(_._2).reverse
    sorted.head._1
  }












}
