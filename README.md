# TextInsight

TextInsight is a Java tool that gives insight on textual input.

TextInsight uses [Stanford CoreNLP](http://stanfordnlp.github.io/CoreNLP/) and [WordNet](https://wordnet.princeton.edu/) (specifically [extJWNL](https://github.com/extjwnl/extjwnl)) to process textual input and generate insight.

## Build
### NLP Library
'''
cd nlp
mvn clean install
'''
### Command line tool
'''
cd cli
mvn clean install
'''

## Getting Started
The cli tool currently support subtitle files (SRT) as input 
