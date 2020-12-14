/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package citec.correlation.wikipedia.parameters;

import citec.correlation.core.analyzer.LemmaAnalyzer;

/**
 *
 * @author elahi
 */
public interface PatternThresold {

    public static String ORIGINAL_CONTEXT = "ORIGINAL_CONTEX";
    public static String CONTEX_POS_MIX = "CONTEX_POS_MIX";
    public static final String PREDICATE_CONTEXT = "PREDICATE_CONTEXT";
    public static final String CONTEXT_PREDICATE = "CONTEXT_PREDICATE";
    public static String regEx = "(VB|VBD|VBG|VBN|VBP|VBZ|IN|NN|NNS|NNP|NNPS|JJ|JJR|JJS|TO|IN|)";
    public static String TXT=".txt";
    public static String JSON=".json";
    public static final Integer contextLimit=2;
    public static LemmaAnalyzer lemmaAnalyzer = new LemmaAnalyzer();
    public static Integer PREDICATE_CONTEXT_MinimumNumberOfEntities=1;
    public static Integer PREDICATE_CONTEXT_TopWords=10;
    public static String CONEXT_WORD_TYPE=ORIGINAL_CONTEXT;
    public static String ALL_CLASS="ALL_CLASS";
    public static String SPECIFIC_CLASS="SPECIFIC_CLASS";

}
