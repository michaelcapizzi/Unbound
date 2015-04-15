package TextComplexity

import java.util.Properties

import edu.arizona.sista.learning.{Datum, Dataset}
import edu.arizona.sista.struct.Lexicon
import edu.arizona.sista.utils.StringUtils
import edu.stanford.nlp.stats.Counter

/**
 * Created by mcapizzi on 4/15/15.
 */
class NaiveBayesSISTA[L, F] (
                       val featureSelection: Int = 0,
                       val mutualInformationThreshold: Int = 0
                         )
{

  /*def this(props: Properties) =
    this(
      StringUtils.getInt(props, "featureSelection", 0),
      StringUtils.getInt(props, "mutualInformationThreshold", 0)
    )

  private var featureLexicon: Lexicon[F] = null
  private var labelLexicon: Lexicon[L] = null

  /** Total number of datums in training */
  private var totalDatums = 0

  def train(dataset: Dataset[L, F] {
  //
  }

  def classOf(d: Datum[L,F]): L = {
    ///
  }

  def scoresOf(d: Datum[L,F]): Counter[L] = {
    //
  }*/



}
