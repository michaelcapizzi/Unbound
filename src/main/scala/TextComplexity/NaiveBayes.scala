package TextComplexity

/**
 * Created by mcapizzi on 4/15/15.
 */
class NaiveBayes(trainingData: Vector[TextDocument], testDocument: TextDocument) {

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
        //number of documents in a class / total number of documents
  def priorProbabilities = {
    (for (possibleClass <- this.classCounts.keySet) yield {
      possibleClass -> classCounts(possibleClass) / this.trainingDataSize
    }).toMap
  }

  //extract all vocabulary from all documents in training
  def extractVocabulary = {
    trainingData.map(textDocument => textDocument.getWords.map(_.toLowerCase)).flatten.distinct
  }

  //concatenate all documents for each class into one document
  def makeDocumentConcatenized = {
    //
  }









}
