import _root_.sbt.Keys._

name := "Unbound"

version := "1.0"

scalaVersion := "2.11.5"

javaOptions += "-Xmx6G"

libraryDependencies ++= Seq(
  //"edu.arizona.sista" % "processors" % "3.3",
  //"edu.arizona.sista" % "processors" % "3.3" classifier "models",
  "edu.arizona.sista" % "processors_2.11" % "5.2",
  "edu.arizona.sista" % "processors_2.11" % "5.2" classifier "models",
  "org.apache.commons" % "commons-math3" % "3.3",
  "org.apache.commons" % "commons-compress" % "1.9",
  "org.apache.commons" % "commons-io" % "1.3.2",
  "com.quantifind" % "wisp_2.11" % "0.0.4",
  //"org.scalanlp" % "breeze-natives_2.10" % "0.11.2",
  "org.scalanlp" % "breeze_2.10" % "0.11.2"/*,
  "edu.cmu.cs" % "ark-tweet-nlp" % "0.3.2",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.5.0",
  */
)