package TextComplexity

import edu.arizona.sista.learning._
import edu.arizona.sista.struct.Counter

import scala.collection.immutable.IndexedSeq

/**
 * Created by mcapizzi on 5/12/15.
 */
object Scaling {

  /* testing examples
  val trainDataSet = RVFDataset.mkDatasetFromSvmLightFormat("/home/mcapizzi/Github/Unbound/src/main/resources/featureVectors/3/lexical_syntactic_paragraph/1/1.train")
  val testDataSet = RVFDataset.mkDatumsFromSvmLightFormat("/home/mcapizzi/Github/Unbound/src/main/resources/featureVectors/3/lexical_syntactic_paragraph/1/1.test")

  //build range
  val range = Datasets.svmScaleRVFDataset(trainDataSet, 0, 1)

  //train on trainDataSet

  //apply to testData with methods below

  */

  //apply to every datum in dataset
  def normalizeData(dataSet: RVFDataset[Int, String]): IndexedSeq[Counter[String]] = {
    val normalizedScaleRange = Datasets.svmScaleRVFDataset(dataSet, -1, 1)
    for (i <- 0 to dataSet.size - 1) yield {
      Datasets.svmScaleDatum(dataSet.mkDatum(i).featuresCounter, normalizedScaleRange, 0, 1)
    }
  }

  //normalize each datum
  def normalizeDatum(datum: Datum[Int, String], range: ScaleRange[String]): Counter[String] = {
    Datasets.svmScaleDatum(datum.featuresCounter, range, 0, 1)
  }


}