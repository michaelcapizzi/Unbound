Text Complexity Model
========================

Data
-----------------

The data is sparse, consisting of full-texts and excerpts from different grade-level bands as identified by the Common Core Standards:

Grade-Level Band | # of Full-text Samples | # of Excerpt Samples | Total # of Samples
---|---|---|---
K-1 |
2-3 |
4-5 |
6-8 |
9-10 |
11-12 |

As a result of the small data size, leave-one-out testing will be utilized.

Features Collected
---------------------

Lexical Features | Syntactic Features | Paragraph Features
---|---|---
\# of distinct conjunctions used | \**average # of conjunctions per sentence | \**length of paragraphs
% of distinct nouns used | \**sentence length in text | \# of relations per paragraph
% of distinct verbs used | \**tree size in text | % of relations in each direction
% of distinct adjectives used | \**tree depth in text | % of each relation type
% of tokens not present in concreteness database | \**distance to verb in sentences in text
\*concreteness score present in text | \**\# of constituents in a sentence in text
\*concreteness score of most-used noun in text | \**constituent length in text
\*concreteness score of most-used verb in text | \# of clauses per sentence in text
\*concreteness score of most-used adjective in text | % of simple sentences in text
word similarity in sentences in text (parameterized)| % of complex sentences in text
 | % of compound sentences in text |
 | % of compound-complex sentences in text |
 | % of fragments in text |
 
 \* = easily normalized
 \** = not easily normalized
 
Two issues discovered:

- The word similarity method that I developed took entirely too long to run.  Initially, it was set to generate an approximation of the word similarity of a sentence.  But in doing so, it required calculating the cosine similarity of every noun, adjective, verb, and adverb in the text.  And for the longer texts (10k lines) it was too much.  On Becky Sharp's suggestion, I tried to shorten it by elementwise adding the similarity vectors for each word in the sentence first and then running cosine similarity on every two sentences within a 5-sentence window, and it still took very long to run.  I am still curious as to whether or not it will add any value to the model, but at this point, it is too time consuming to run in my tests.
- I have data that ranges from a percent to whole numbers under 100.  I fear that this difference is affecting the model, and so I need to scale the data.  I know, in theory, how to do this, but I was unable to utilize the methods available in learning to do it and did not have a chance to implement it myself.
 
Performance (6 classes)
---------------------

Perceptron: epochs = 20, marginRatio = 1.0
LogisticRegression: bias = false
RandomForest: numTrees = 1000, featureSampleRatio = -.20, maxTreeDepth = 4
 
 Features Used | Model Used | Accuracy | Precision | Recall | F1
 ---|---|---|---|---|---
 -- | Perceptron | 29.6% | .05 | .17 | .08
 Lexical | Logistic Regression | 29.6% | .16 | .21 | .18
 -- | **Random Forest** | **42.6%** | **.45** | **.43** | **.44**
 | | | | |
 -- | Perceptron | 29.6% | .10 | .17 | .13
 Syntactic | Logistic Regression | 22.2% | .18 | .18 | .18 
 -- | Random Forest | 27.7% | .13 | .19 | .15
 | | | | |
 -- | Perceptron | 33.3% | .16 | .19 | .18
 Paragraph | Logistic Regression | 24.1% | .17 | .18 | .18
 -- | Random Forest | 30% | .21 | .23 | .22
 | | | | |
 -- | Perceptron | 31.5% | .13 | .18 | .15
 Lex + Syn | Logistic Regression | 29.6% | .24 | .28 | .26
 -- | Random Forest | 27.7% | .16 | .20 | .18
 | | | | |
 -- | Perceptron | 24% | .12 | .16 | .14
 Lex + Par | Logistic Regression | 31.5% | .24 | .25 | .24
 -- | Random Forest | 30% | .15 | .22 | .18
 | | | | |
 -- | Perceptron | 26% | .09 | .15 | .11
 Syn + Par | Logistic Regression | 22.2% | .15 | .17 | .16
 -- | Random Forest | 26% | .12 | .19 | .15
 | | | | |
 -- | Perceptron | 27.7% | .05 | .16 | .08
 Lex + Syn + Par | Logistic Regression | 31.5% | .32 | .30 | .31
 -- | Random Forest | 25.9% | .13 | .19 | .15
 | | | | |
 -- | **Lexile** | **24.07%** | **.32** | **.36** | **.34**

Performance over all 6 classes was very poor with, surprisingly, just lexical features run over a Random Forest model performing the best.  That performance, however, is much better than Lexile, a popular industry model.  Lexile officially presents a score-band for each grade level with significant overlap between grade levels (see below), which isn't very useful.  In order to generate the scores above, I forced Lexile to choose the lowest grade-level from their Lexile band.

![](https://cloud.githubusercontent.com/assets/8990766/7360833/6f1ba746-ed04-11e4-974e-8d01e02f3b33.png)

Gus Hahn-Powell suggested I considered a two-step classifier that first classifies among three classes: Elementary, Middle School, High School, and then classifies into the specific band within each large class.

In preparation for that, I tested all models and features on three classes.  See below.

Performance (3 classes)
---------------------

Perceptron: epochs = 20, marginRatio = 1.0
LogisticRegression: bias = false
RandomForest: numTrees = 1000, featureSampleRatio = -.20, maxTreeDepth = 4

 Features Used | Model Used | Accuracy | Precision | Recall | F1
 ---|---|---|---|---|---
 -- | Perceptron | 33.3% | .11 | .33 | .17
 Lexical | Logistic Regression | 51.9% | .31 | .35 | .33
 -- | **Random Forest** | **70.4%** | **.47** | **.53** | **.50**
 | | | | |
 -- | Perceptron | 33.3% | .11 | .33 | .17
 Syntactic | Logistic Regression | 46.3% | .33 | .34 | .33
 -- | **Random Forest** | **70.4%** | **.48** | **.52** | **.50**
 | | | | |
 -- | Perceptron | 33.3% | .11 | .33 | .17
 Paragraph | Logistic Regression | 57.4% | .49 | .44 | .47
 -- | **Random Forest** | **70.4%** | **.80** |**.56** | **.66**
 | | | | |
 -- | Perceptron | 31.5% | .11 | .31 | .16
 Lex + Syn | Logistic Regression | 50% | .34 | .37 | .35
 -- | **Random Forest** | **74.1%** | **.50** | **.55** | **.53**
 | | | | |
 -- | Perceptron | 37% | .33 | .34 | .34
 Lex + Par | Logistic Regression | 55.5% | .50 | .47 | .49
 -- | Random Forest | 66.6% | .44 | .50 | .47
 | | | | |
 -- | Perceptron | 33.3% | .11 | .33 | .17
 Syn + Par | Logistic Regression | 50% | .36 | .37 | .37
 -- | **Random Forest** | **70.4%** | **.47** | **.53** | **.50**
 | | | | |
 -- | Perceptron | 33.3% | .11 | .33 | .17
 Lex + Syn + Par | Logistic Regression | 61.1% | .57 | .49 | .53
 -- | Random Forest | 68.5% | .45 | .51 | .48
 
 Performance was, as expected, better in a number of cases.  And while Lex + Syn features run through a Random Forest had the best accuracy, their precision, recall, and F1 scores were significantly lower than Paragraph features run through a Random Forest.  The reason for the discrepancy in P, R, and F1 scores lies in the class scores for "0608."  In that class, all the high-accuracy feature sets scored a 0 for both precision and recall while the Paragraph feature set scored a precision of 1, but a recall of .14.  On even further analysis, none of the highly accurate feature sets labeled any texts as "0608" while the Paragraph feature set yielded only one text labeled as "0608."  
 
This not surprising as (1) it is the middle class and (2) more specific to the situation of school texts, it is not unlikely to have both very easy and very difficult middle-school-level texts.  And if I could somehow improve the model's likelihood to label a text as "0608", performance should improve even more.
 
It was my intention to take the sets generated by the three-class model using Paragraph features and re-run them each on a second-tier classifier to distinguish the sub-classes.  However, I struggled to find a way to implement this correctly. I was confused as to how to generate the training set.  If I were to do it exactly like I did leave-one-out in the first tier (where I tested each text individually having trained on all the other texts), then I would be leaving in incorrect classes in the training model.  For instance, there might be a "0912" text in the "0005" class, and if I were to train on that, it would be giving the model the impression that it has the option of choosing "0912" as a possible class.  And yet, it seemed illegitimate to remove those cases because it would imply a level of knowledge that the system itself did not have.  The best option I could come up would be to train the six-class models on all the correctly labeled texts in that class.  And then when implementing leave-one-out testing, I would simply remove the text being tested from the training set, and I could ignore the texts that were incorrectly labeled after the first-tier as those would be wrong no matter what the second-tier classifier labeled them.  

Next Steps
==============

- I'd like to scale the features and see if that improves performance.
- I'd like to add more texts to the data, and hopefully more full-length texts as well, to see if that improves performance.
- I'd also like to find a way to force the three-tier model to generate more "0608" labels.  Perhaps that would include adding a third-tier of classifying where tier-one is binary and simply labels as "high" or "low".  Then tier-two separates between "0005" \& "0608" and "0608" \& "0912".  Then tier-three separates the "0005" class into "0001", "0203", and "0405" and the "0912" class into "0910" and "1112".
  
 
