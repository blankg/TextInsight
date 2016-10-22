package com.github.blankg.textinsight.cli;

import com.github.blankg.textinsight.nlp.NLPManager;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


public class CLIService {

    public static void main(String[] args) {

        if (args.length < 0) {
            throw new IllegalArgumentException("No input provided");
        }

        String srtPath = args[0];

        try (InputStream is = Files.newInputStream(Paths.get(srtPath))) {
            Set<String> allCategories = new TreeSet<String>();
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            Dictionary dictionary = Dictionary.getDefaultResourceInstance();
            SrtSubtitles subs = SrtParser.parse(is);
            List<SrtSubtitles.Line> lines = subs.getSubs();
            StringBuffer content = new StringBuffer();
            for (SrtSubtitles.Line line : lines) {
                List<NLPManager.NLPData> nlpData = line.getNLPData();
                if (nlpData != null) {
                    Map<String,List<String>> map = new HashMap<String, List<String>>();
                    for (NLPManager.NLPData data : nlpData) {
                        String pos = data.getPos();
                        if (pos.startsWith("NN")) {
                            String wordStr = data.getWord();
                            String namedEntity = data.getNe();
                            if (!namedEntity.equals("O")) {
                                addToCategoriesCount(namedEntity, false);
                                data.setTime(line.getFrom());
                                addCategoryAndWord(namedEntity, data, false);
                                ArrayList<String> tempList = new ArrayList<String>();
                                tempList.add(namedEntity);
                                map.put(wordStr, tempList);
                            }
                            else {
                                IndexWord word = dictionary.getIndexWord(POS.NOUN, data.getLemma());

                                List<String> categories = null;
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
                            }

//								nouns.add(data.getWord());
                        }
                        //Aggregate all NE data
//							String ne = data.getNe();
//							if (!ne.equalsIgnoreCase("o")) {
//								if (table.containsKey(ne)) {
//									((List<String>)table.get(ne)).add(data.getWord());
//								}
//								else {
//									ArrayList<String> words = new ArrayList<String>();
//									words.add(data.getWord());
//									table.put(ne, words);
//								}
//							}
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
//							allNouns.add(line.getFrom() + "-->" + line.getTo() + IOUtils.eolChar);
//							allNouns.add(Arrays.toString(nouns.toArray()) + IOUtils.eolChar);
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

//				IOUtils.writeObjectToFile(allNouns, "/Users/i031231/Documents/EliGuy/output.zip");
//				for (String key : table.keySet()) {
//					System.out.println(key + ": " + Arrays.toString(table.get(key).toArray()));
//				}
            URL jarLocation = CLIService.class.getProtectionDomain().getCodeSource().getLocation();
            URL outputUrl = new URL(jarLocation, "output.txt");
            writeContentToFile(content.toString(), outputUrl.getPath());



        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JWNLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private static Map<String,Integer> categoriesCount = new TreeMap<String,Integer>();
    private static Map<String,List<NLPManager.NLPData>> categoriesToWords = new HashMap<String,List<NLPManager.NLPData>>();
    private static final Set<String> blackList = new HashSet<String>(Arrays.asList("entity", "abstraction", "abstract entity", "physical entity", "object", "physical object"));

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
        List<NLPManager.NLPData> words = categoriesToWords.containsKey(category) ? categoriesToWords.get(category) : new ArrayList<NLPManager.NLPData>();
        words.add(word);
        categoriesToWords.put(category, words);
    }

    private static List<String> getCategories(IndexWord word) throws JWNLException {
        PointerTargetTree hypernyms = PointerUtils.getHypernymTree(word.getSenses().get(0));
        return getAllHypernymsFromTree(hypernyms.getRootNode().getChildTreeList().getFirst());
    }

    private static List<String> getAllHypernymsFromTree(PointerTargetTreeNode first) {
        ArrayList<String> hypers = new ArrayList<String>();
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
        SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
                new Comparator<Map.Entry<K,V>>() {
                    @Override public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
//	                return e1.getValue().compareTo(e2.getValue());
                        int res = e2.getValue().compareTo(e1.getValue());
                        return res != 0 ? res : 1;
                    }
                }
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }

    static void writeContentToFile(String content, String path) {
        FileOutputStream fop = null;
        File file;

        try {

            file = new File(path);
            if(!file.getParentFile().exists())
            {
                file.getParentFile().mkdirs();
            }
            fop = new FileOutputStream(file);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            // get the content in bytes
            byte[] contentInBytes = content.getBytes();

            fop.write(contentInBytes);
            fop.flush();
            fop.close();

            System.out.println("Done");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fop != null) {
                    fop.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
