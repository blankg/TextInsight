# TextInsight

TextInsight is a Java tool that gives insight on textual input.

TextInsight uses [Stanford CoreNLP](http://stanfordnlp.github.io/CoreNLP/) and [WordNet](https://wordnet.princeton.edu/) (specifically [extJWNL](https://github.com/extjwnl/extjwnl)) to process textual input and generate insight.

## Build
### NLP Library
```
cd nlp
mvn clean install
```
### Command line tool
```
cd cli
mvn clean install
```

## Getting Started
The cli tool currently support subtitle files (SRT) as input, output is written into a file -> output.txt.
```
java -jar cli-1.0-SNAPSHOT-jar-with-dependencies.jar <path to .srt file>
```
### Example
Input:
```
1
00:00:00,000 --> 00:00:10,000
I love apples and chairs

2
00:00:10,000 --> 00:00:20,000
I like apples

3
00:00:20,000 --> 00:00:30,000
You hate telephones, pens and glasses

4
00:00:30,000 --> 00:00:40,000
She loves telephones, pens and glasses

5
00:00:40,000 --> 00:00:50,000
Jim loves apples, oranges and milk
```
Output:
```
0:0:0:0-->0:0:10:9990
chairs: [entity, physical entity, object, physical object, whole, unit, artifact, artefact, instrumentality, instrumentation, furnishing, furniture, piece of furniture, article of furniture, seat]
apples: [entity, physical entity, matter, solid, food, solid food, produce, green goods, green groceries, garden truck, edible fruit]
Sentiment: 3
0:0:10:9990-->0:0:20:19980
apples: [entity, physical entity, matter, solid, food, solid food, produce, green goods, green groceries, garden truck, edible fruit]
Sentiment: 2
0:0:20:19980-->0:0:30:29970
glasses: [entity, physical entity, matter, solid]
telephones: [entity, physical entity, object, physical object, whole, unit, artifact, artefact, instrumentality, instrumentation, equipment, electronic equipment]
pens: [entity, physical entity, object, physical object, whole, unit, artifact, artefact, instrumentality, instrumentation, implement, writing implement]
Sentiment: 1
0:0:30:29970-->0:0:40:39960
glasses: [entity, physical entity, matter, solid]
telephones: [entity, physical entity, object, physical object, whole, unit, artifact, artefact, instrumentality, instrumentation, equipment, electronic equipment]
pens: [entity, physical entity, object, physical object, whole, unit, artifact, artefact, instrumentality, instrumentation, implement, writing implement]
Sentiment: 3
0:0:40:39960-->0:0:50:49950
oranges: [entity, physical entity, matter, solid, food, solid food, produce, green goods, green groceries, garden truck, edible fruit, citrus, citrus fruit, citrous fruit]
milk: [entity, physical entity, matter, substance, food, nutrient, foodstuff, food product, dairy product]
apples: [entity, physical entity, matter, solid, food, solid food, produce, green goods, green groceries, garden truck, edible fruit]
Jim: [PERSON]
Sentiment: 3
```
