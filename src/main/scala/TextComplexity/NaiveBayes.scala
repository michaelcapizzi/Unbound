package TextComplexity


/**
 * Created by mcapizzi on 4/15/15.
 */
class NaiveBayes(trainingData: Vector[TextDocument], testDocument: TextDocument, stopWords: Vector[String], featureFrequencyThreshold: Int, mutualInformationThreshold: Int) {

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

  //class prior probabilities
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
    for (possibleClass <- possibleClasses) yield {                                    //for each class
      trainingData.filter(doc => doc.gradeLevel == possibleClass).map(                  //take the documents of that class
        textDocument => textDocument.getWords.map(_.toLowerCase)).flatten.               //get words, put to lowercase, and flatten
        diff(stopWords)                                                                  //filter out stop words
    }
  }









}
