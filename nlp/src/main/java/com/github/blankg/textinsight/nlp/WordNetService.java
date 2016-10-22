package com.github.blankg.textinsight.nlp;


import com.github.blankg.textinsight.nlp.input.subs.SrtParser;
import com.github.blankg.textinsight.nlp.input.subs.SrtSubtitles;

import edu.stanford.nlp.io.IOUtils;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.PointerUtils;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.data.list.PointerTargetTree;
import net.sf.extjwnl.data.list.PointerTargetTreeNode;
import net.sf.extjwnl.data.list.PointerTargetTreeNodeList;
import net.sf.extjwnl.dictionary.Dictionary;

import java.util.*;

public class WordNetService {

    public String createContentFromNLPData(List<SrtSubtitles.Line> lines) throws NLPException {
        Set<String> allCategories = new TreeSet<>();
        Dictionary dictionary;
        try {
            dictionary = Dictionary.getDefaultResourceInstance();
        } catch (JWNLException e) {
            throw new NLPException(e.getMessage(), e.getCause());
        }
        StringBuffer content = new StringBuffer();
        for (SrtSubtitles.Line line : lines) {
            List<NLPManager.NLPData> nlpData = line.getNLPData();
            if (nlpData != null) {
                Map<String,List<String>> map = new HashMap<>();
                for (NLPManager.NLPData data : nlpData) {
                    String pos = data.getPos();
                    if (pos.startsWith("NN")) {
                        String wordStr = data.getWord();
                        String namedEntity = data.getNe();
                        if (!namedEntity.equals("O")) {
                            addToCategoriesCount(namedEntity, false);
                            data.setTime(line.getFrom());
                            addCategoryAndWord(namedEntity, data, false);
                            ArrayList<String> tempList = new ArrayList<>();
                            tempList.add(namedEntity);
                            map.put(wordStr, tempList);
                        }
                        else {
                            IndexWord word;
                            List<String> categories = null;
                            try {
                                word = dictionary.getIndexWord(POS.NOUN, data.getLemma());

                                if (word != null) {
                                    categories = getCategories(word);

                                    for (String category : categories) {
                                        allCategories.add(category);
                                        addToCategoriesCount(category, true);
                                        data.setTime(line.getFrom());
                                        addCategoryAndWord(category, data, true);
                                    }
                                }

                                map.put(wordStr, categories);
                            } catch (JWNLException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                if (!map.isEmpty()) {
                    content = content.append(SrtParser.parse(line.getFrom()));
                    content = content.append("-->");
                    content = content.append(SrtParser.parse(line.getTo()));
                    content = content.append(IOUtils.eolChar);
                    for (String key : map.keySet()) {
                        content = content.append(key);
                        content = content.append(": ");
                        List<String> vals = map.get(key);
                        if (vals != null) {
                            content = content.append(Arrays.toString(vals.toArray()));
                        }
                        content = content.append(IOUtils.eolChar);
                    }

                    // print sentiment
                    content = content.append("Sentiment: ");
                    content = content.append(line.getSentiment());
                    content = content.append(IOUtils.eolChar);
                }
            }

        }

        //Print top categories
        SortedSet<Map.Entry<String, Integer>> sortedCategories = entriesSortedByValues(categoriesCount);
        for (Map.Entry<String, Integer> entry : sortedCategories) {
            content = content.append(entry.getKey());
            content = content.append(": ");
            content = content.append(entry.getValue());
            content = content.append(IOUtils.eolChar);
        }

        return content.toString();
    }

    private static Map<String,Integer> categoriesCount = new TreeMap<>();
    private static Map<String,List<NLPManager.NLPData>> categoriesToWords = new HashMap<>();
    private static final Set<String> blackList = new HashSet<>(Arrays.asList("entity", "abstraction", "abstract entity", "physical entity", "object", "physical object"));

    private static void addToCategoriesCount(String category, boolean checkBlackList) {
        if (checkBlackList && blackList.contains(category)) {
            return;
        }
        int count = categoriesCount.containsKey(category) ? categoriesCount.get(category) : 0;
        categoriesCount.put(category, count + 1);
    }

    private static void addCategoryAndWord(String category, NLPManager.NLPData word, boolean checkBlackList) {
        if (checkBlackList && blackList.contains(category)) {
            return;
        }
        List<NLPManager.NLPData> words = categoriesToWords.containsKey(category) ? categoriesToWords.get(category) : new ArrayList<>();
        words.add(word);
        categoriesToWords.put(category, words);
    }

    private static List<String> getCategories(IndexWord word) throws JWNLException {
        PointerTargetTree hypernyms = PointerUtils.getHypernymTree(word.getSenses().get(0));
        return getAllHypernymsFromTree(hypernyms.getRootNode().getChildTreeList().getFirst());
    }

    private static List<String> getAllHypernymsFromTree(PointerTargetTreeNode first) {
        ArrayList<String> hypers = new ArrayList<>();
        PointerTargetTreeNodeList treeNodes = first.getChildTreeList();
        if (!treeNodes.isEmpty()) {
            hypers.addAll(getAllHypernymsFromTree(treeNodes.getFirst()));
        }
        List<Word> words =first.getSynset().getWords();
        for (Word nWord : words) {
            hypers.add(nWord.getLemma());
        }
        return hypers;
    }

    static <K,V extends Comparable<? super V>>
    SortedSet<Map.Entry<K,V>> entriesSortedByValues(Map<K,V> map) {
        SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<>(
                (e1, e2) -> {
//	                return e1.getValue().compareTo(e2.getValue());
                    int res = e2.getValue().compareTo(e1.getValue());
                    return res != 0 ? res : 1;
                }
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }

}
