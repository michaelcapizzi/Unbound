package TextComplexity

import edu.arizona.sista.processors.corenlp.CoreNLPProcessor

/**
 * Created by mcapizzi on 4/27/15.
 */
object MainTextComplexity {
  def main(args: Array[String]) = {

    val p = new CoreNLPProcessor(withDiscourse = true)
    val ml6 = new MachineLearning("/home/mcapizzi/Github/Unbound/src/main/resources/annotatedText/", p, Vector("lexical"), Vector("randomForest"), 6, "/home/mcapizzi/Github/Unbound/src/main/resources/rawText/")
    val ml3 = new MachineLearning("/home/mcapizzi/Github/Unbound/src/main/resources/annotatedText/", p, Vector("paragraph"), Vector("randomForest"), 3, "/home/mcapizzi/Github/Unbound/src/main/resources/rawText/")

    val scores6 = ml6.leaveOneOut(false)

    val eval6 = new EvaluationMetrics(scores6)

    val accuracy6 = eval6.accuracy(scores6.head._2)
    val macro6 = eval6.macroScores(scores6.head._2)

    val scores3 = ml3.leaveOneOut(false)

    val eval3 = new EvaluationMetrics(scores3)

    val accuracy3 = eval3.accuracy(scores3.head._2)
    val macro3 = eval3.macroScores(scores3.head._2)

    println("Results of a six-tier Random Forest classifier using lexical features:")
    println("Overall accuracy: " + accuracy6)
    println("Macro precision: " + macro6("macroPrecision"))
    println("Macro recall: " + macro6("macroRecall"))
    println("Macro F1: " + macro6("macroF1"))
    println()
    println()

    println("Results of a three-tier Random Forest classifier using paragraph features:")
    println("Overall accuracy: " + accuracy3)
    println("Macro precision: " + macro3("macroPrecision"))
    println("Macro recall: " + macro3("macroRecall"))
    println("Macro F1: " + macro3("macroF1"))

  }
}
