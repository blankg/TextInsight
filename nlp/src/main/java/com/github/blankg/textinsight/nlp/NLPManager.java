package com.github.blankg.textinsight.nlp;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

import java.util.*;

public class NLPManager {

	private static NLPManager instance = null;
	private StanfordCoreNLP pipeline;

	protected NLPManager() {
		// creates a StanfordCoreNLP object, with POS tagging,
		// lemmatization, NER, parsing, and coreference resolution
		Properties props = new Properties();
		// props.put("annotators",
		// "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, sentiment");
		// props.put("annotators", "tokenize, ssplit, parse, sentiment");
		this.pipeline = new StanfordCoreNLP(props);
	}

	public static NLPManager getInstance() {
		if (instance == null) {
			instance = new NLPManager();
		}
		return instance;
	}
	
	private static final Set<String> blackList = new HashSet<String>(Arrays.asList("<i>", "</i>", "?", "!"));
	
	public List<NLPData> getNLPData(String text) {

		// create an empty Annotation just with the given text
		Annotation document = new Annotation(text);

		// run all Annotators on this text
		pipeline.annotate(document);

		// these are all the sentences in this document
		// a CoreMap is essentially a Map that uses class objects as keys and
		// has values with custom types
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		List<NLPData> result = new ArrayList<NLPData>();

		for (CoreMap sentence : sentences) {
			// traversing the words in the current sentence
			// a CoreLabel is a CoreMap with additional token-specific methods
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				// this is the text of the token
				String word = token.get(TextAnnotation.class);
				
				if (word != null && !word.isEmpty()) {
					if (blackList.contains(word)) {
						continue;
					}
					// this is the POS tag of the token
					String pos = token.get(PartOfSpeechAnnotation.class);
					// this is the NER label of the token
					String ne = token.get(NamedEntityTagAnnotation.class);
					
					String lemma = token.lemma();
					
					NLPData data = new NLPData(word, pos, ne, lemma);
					result.add(data);
					System.out.println(word + " POS: " + pos + " NE: " + ne);
				}
			}
			
			
//			// this is the parse tree of the current sentence
//			Tree tree = sentence.get(TreeAnnotation.class);
//
//			// this is the Stanford dependency graph of the current sentence
//			SemanticGraph dependencies = sentence
//					.get(CollapsedCCProcessedDependenciesAnnotation.class);
		}
		
		return result;
		
		// This is the coreference link graph
		// Each chain stores a set of mentions that link to each other,
		// along with a method for getting the most representative mention
		// Both sentence and token offsets start at 1!
		// Map<Integer, CorefChain> graph =
		// document.get(CorefChainAnnotation.class);
	}
	
	
	public int analyzeSentiment(String line) {
//		Properties props = new Properties();
//		// props.put("annotators",
//		// "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
//		props.put("annotators", "tokenize, ssplit, parse, sentiment");
//		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
//		Annotation annotation = pipeline.process(line);
//		for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
//			Tree tree = sentence.get(SentimentCoreAnnotations.AnnotatedTree.class);
//			Tree copy = tree.deepCopy();
//	        this.setSentimentLabels(copy);
//            System.out.println(copy);
//          }
		int longest = 0;
		int mainSentiment = 0;
        Annotation annotation = pipeline.process(line);
        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
            int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
            String partText = sentence.toString();
            if (partText.length() >= longest) {
                mainSentiment = sentiment;
                longest = partText.length();
            }
        }
        System.out.println(line + " SENTIMENT: " + mainSentiment);
        return mainSentiment;
	}

	private void setSentimentLabels(Tree tree) {
		if (tree.isLeaf()) {
			return;
		}

		for (Tree child : tree.children()) {
			setSentimentLabels(child);
		}

		Label label = tree.label();
		if (!(label instanceof CoreLabel)) {
			throw new IllegalArgumentException(
					"Required a tree with CoreLabels");
		}
		CoreLabel cl = (CoreLabel) label;
		cl.setValue(Integer.toString(RNNCoreAnnotations.getPredictedClass(tree)));
	}
	
	public static class NLPData {
		String word;
		String lemma;
		String pos;
		String ne;
		long time;
		
		public NLPData(String word, String pos, String ne, String lemma) {
			this.word = word;
			this.pos = pos;
			this.ne = ne;
			this.lemma = lemma;
		}
		
		public String getWord() {
			return word;
		}
		public void setWord(String word) {
			this.word = word;
		}
		public String getPos() {
			return pos;
		}
		public void setPos(String pos) {
			this.pos = pos;
		}
		public String getNe() {
			return ne;
		}
		public void setNe(String ne) {
			this.ne = ne;
		}

		public String getLemma() {
			return lemma;
		}

		public void setLemma(String lemma) {
			this.lemma = lemma;
		}
		
		public long getTime() {
			return time;
		}

		public void setTime(long time) {
			this.time = time;
		}
	}
}
