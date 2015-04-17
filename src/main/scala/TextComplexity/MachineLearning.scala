package TextComplexity

import java.io.File

/**
 * Created by mcapizzi on 4/15/15.
 */
class MachineLearning(
                       val rawTextFileFolder: String = "",
                       val annotatedTextFileFolder: String,         //required
                       val featureVectorFileFolder: String = "",
                       val featuresToInclude: Vector[String],       //required
                       val modelsToUse: Vector[String]              //required
                       ) {

  val rawFile = new File(rawTextFileFolder)
  val annotatedFile = new File(annotatedTextFileFolder)
  val featureVectorFile = new File(featureVectorFileFolder)

  //load files from folder
      //create a textDocument for each folder
      //create a separate feature class instance for each item in featuresToInclude
      //concatenate features together
      //export to resources/featureVectors
        //with a folder and name
      //do the ML
      //create a evaluationmetric class instance



}
