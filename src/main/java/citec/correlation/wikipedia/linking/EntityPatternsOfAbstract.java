/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package citec.correlation.wikipedia.linking;

import citec.correlation.core.analyzer.Analyzer;
import citec.correlation.core.analyzer.LemmaAnalyzer;
import static citec.correlation.core.analyzer.TextAnalyzer.POS_TAGGER_TEXT;
import citec.correlation.wikipedia.utils.FormatAndMatch;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import org.apache.commons.lang3.StringUtils;
import org.javatuples.Pair;
import citec.correlation.wikipedia.parameters.PatternThresold;
import java.util.Objects;

/**
 *
 * @author elahi
 */
public class EntityPatternsOfAbstract implements PatternThresold{

    private Map<String,List<Pattern>> objectPatternsMap = new TreeMap<String,List<Pattern>>();
    private Map<String,Pattern> allpatternsHash = new TreeMap<String,Pattern>();
    private List<Pattern> allpatternList = new ArrayList<Pattern>();
    private static Analyzer analyzer=null;
    private static LemmaAnalyzer lemmaAnalyzer=null;


    public EntityPatternsOfAbstract(Analyzer analyzer,LemmaAnalyzer lemmaAnalyzer,Collection<String> patternsStr) throws Exception {
        this.analyzer = analyzer;
        this.lemmaAnalyzer=lemmaAnalyzer;
        for (String patternStr : patternsStr) {
            Pattern pattern = new Pattern(patternStr,contextLimit);
            allpatternList.add(pattern);
            if(pattern.getModifiedContextWord()!=null){                
               allpatternsHash.put(pattern.getModifiedContextWord(),pattern);
            }
            String object=pattern.getObjectOfProperty();
            List<Pattern> patterns=new ArrayList<Pattern>();
            if(objectPatternsMap.containsKey(object)){
                patterns=objectPatternsMap.get(object);
            }
            patterns.add(pattern);
            objectPatternsMap.put(object, patterns);
        }
    }

    public Map<String,List<Pattern>> getPatterns() {
        return objectPatternsMap;
    }

    public Map<String, List<Pattern>> getPatternsMap() {
        return objectPatternsMap;
    }

    public Map<String, Pattern> getAllpatterns() {
        return allpatternsHash;
    }

    public List<Pattern> getAllpatternList() {
        return allpatternList;
    }


    public void display() {
        for (String object : this.objectPatternsMap.keySet()) {
             System.out.println(object+" "+objectPatternsMap.get(object));
        }
    }
    
    public static List<Pair<String, String>> parseRankedPattern(String predicate, Map<String, Map<Integer, String>> predicateSortedContextWordsMap, Integer rankLimit) throws Exception {
        List<Pair<String, String>> pairList = new ArrayList<Pair<String, String>>();
        if (predicateSortedContextWordsMap.containsKey(predicate)) {
            Map<Integer, String> contextList = predicateSortedContextWordsMap.get(predicate);
            for (Integer index : contextList.keySet()) {
                String patternStr = contextList.get(index);
                Pair<String, String> pair = parseRankedPattern(patternStr, rankLimit);
                if (pair.getValue0() != null && pair.getValue1() != null) {
                    pairList.add(pair);
                }
            }
        }
        return pairList;
    }
 
    private static Pair<String, String> parseRankedPattern(String patternStr, Integer rankLImit) throws Exception {
        Integer rank = Integer.parseInt(StringUtils.substringBetween(patternStr, "(", ")"));
        String modifiedContextWord = StringUtils.substringBetween(patternStr, "[", "]");
        String posTaggedText = StringUtils.substringBetween(patternStr, "<", ">");
        if (rank > rankLImit) 
            return new Pair<String, String>(modifiedContextWord, posTaggedText);
        
        return new Pair<String, String>(null, null);
    }


    public class Pattern {

        private String subject = null;
        private String contextWord = null;
        private String modifiedContextWord = null;
        private String contextPosMix = null;
        private String objectOfProperty = null;
        private String posTaggedText=null;
        private Boolean valid=false;
        private Integer positionIndex=0;

        public Pattern(String patternStr, Integer contextLimit) throws Exception {
            this.subject = extractValue(patternStr, "(", ")");
            this.objectOfProperty = extractValue(patternStr, "<", ">");
            this.contextWord = this.extractValue(patternStr, "[", "]");

            try {
                if (this.contextWord != null) {
                    this.contextWord = FormatAndMatch.cutContexWords(contextWord, contextLimit);
                    this.setModifiedTextPosTagger(contextWord);
                    if (modifiedContextWord != null && posTaggedText != null) {
                        this.mixContextWordPosTag(modifiedContextWord, posTaggedText, positionIndex);
                    }
                    this.valid = this.checkValidity();
                }

            } catch (Exception ex) {
              System.out.println("contextWord is null:" + contextWord);
              this.valid =false;
            }

        }

        public String extractValue(String patternStr, String start, String end) {
            patternStr = StringUtils.substringBetween(patternStr, start, end);
            if (patternStr.contains("=")) {
                String info[] = patternStr.split("=");
                patternStr = info[1];
            }
            return patternStr;
        }
        
        
        private String  setModifyContextWords(String contextWords) {
           contextWords= FormatAndMatch.format(contextWords);
           contextWords=FormatAndMatch.furtherFilters(contextWords);
           return contextWords;
        }

        private void setModifiedTextPosTagger(String contextWords) throws Exception {
            String[] results = analyzer.posTaggerText(contextWords);
            String posTagged = results[2];
            String posTaggedText = results[1];
            if (FormatAndMatch.isPosTagValid(posTagged, regEx)) {
                this.posTaggedText = posTagged;
            }
            posTaggedText = lemmaAnalyzer.getLemma(posTaggedText);
            this.modifiedContextWord = this.setModifyContextWords(posTaggedText);
        }

        private void mixContextWordPosTag(String modifiedContextWord, String posTaggedText, Integer position) {
            if(!modifiedContextWord.contains("_")){
                this.contextPosMix=modifiedContextWord;
                return ; 
            }
               
            String[] modified = modifiedContextWord.split("_");
            String[] pos = posTaggedText.split("_");
            Integer index = 0;
            String str = "", line = null;
            for (String item : modified) {
                if (index == position) {
                    line = modified[index];
                } else {
                    line = pos[index];
                }
                index=index+1;
                str += line + "_";
            }
            this.contextPosMix = str.substring(0, str.length()-1);
        }

        private Boolean checkValidity() {
            if (this.contextWord!=null&&this.modifiedContextWord != null&&this.posTaggedText != null) {
                return true;
            }
            return false;
        }

        public Boolean isValid() {
            return this.valid;
        }
        
        public String getSubject() {
            return subject;
        }

        public String getContextPosMix() {
            return contextPosMix;
        }

        public String getContextWord() {
            return contextWord;
        }

        public String getObjectOfProperty() {
            return objectOfProperty;
        }

        public String getModifiedContextWord() {
            return modifiedContextWord;
        }

        public String getPosTaggedText() {
            return posTaggedText;
        }


        @Override
        public String toString() {
            return "Patterns{" + "subject=" + subject + ", contextWord=" + contextWord + ", objectOfProperty=" + objectOfProperty + '}';
        }

      


       
    }

}
