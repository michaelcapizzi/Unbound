Text Complexity Model
========================

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
\*concreteness score of most-used verb in text | \**word similarity in sentences in text (parameterized)
\*concreteness score of most-used adjective in text | \# of clauses per sentence in text
 | % of simple sentences in text |
 | % of complex sentences in text |
 | % of compound sentences in text |
 | % of compound-complex sentences in text |
 | % of fragments in text |
 
 \* = easily normalized
 \** = not easily normalized
 
 
Performance (6 classes)
---------------------

Perceptron: epochs = 20, marginRatio = 1.0
LogisticRegression: bias = false
RandomForest: numTrees = 1000, featureSampleRatio = -.20, maxTreeDepth = 4
 
 Features Used | Model Used | Accuracy | Precision | Recall | F1
 ---|---|---|---|---|---
 -- | Perceptron | 29.6% | .05 | .17 | .08
 Lexical | Logistic Regression | 29.6% | .16 | .21 | .18
 -- | Random Forest | 42.6% | .45 | .43 | .44
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
 

 
