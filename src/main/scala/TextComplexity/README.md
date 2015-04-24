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
 
 * = easily normalized
 ** = not easily normalized
 
 Performance
 ---------------------
 
 Features Used | Model Used | Accuracy | Precision | Recall | F1
 ---|---|---|---|---|---
 -- | Perceptron | 29.6% | .05 | .17 | .08
 Lexical | Logistic Regression | 29.6% | .16 | .21 | .18
 -- | Random Forest | 42.6% | .45 | .43 | .44
 | | | | |
 -- | Perceptron | 31.5% | .13 | .18 | .15
 Lex + Syn | Logistic Regression | 29.6% | .24 | .28 | .26
 -- | Random Forest | 27.7% | .16 | .20 | .18
 | | | | |
 -- | Perceptron | 27.7% | .05 | .16 | .08
 Lex + Syn + Par | Logistic Regression | 31.5% | .32 | .30 | .31
 -- | Random Forest | 25.9% | .13 | .19 | .15
 

 
