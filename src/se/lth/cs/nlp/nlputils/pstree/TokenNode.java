/*
 TokenNode.java
 
 Copyright (C) 2005, Richard Johansson (richard@cs.lth.se).
 
 Created in June, 2004 by Richard Johansson (richard@cs.lth.se).
 
 $Log: TokenNode.java,v $
 Revision 1.5  2011-06-09 08:39:58  johansson
 Removed LGPL preamble.

 Revision 1.4  2011-05-26 09:25:19  johansson
 Added getText.

 Revision 1.3  2011-02-10 11:48:22  johansson
 Escaping.

 Revision 1.2  2010-06-07 10:58:40  johansson
 Major update.

 Revision 1.1  2009/01/16 10:09:50  johansson
 Added to brenta repository.

 Revision 1.10  2007/08/07 12:38:56  richard
 Complete refactoring.

 Revision 1.9  2007/07/03 06:53:26  richard
 Subclass of TerminalNode.

 Revision 1.8  2006/12/08 17:00:53  richard
 Created new package. Implemented new rules.

 Revision 1.7  2006/11/13 16:22:33  richard
 Added isSameOrAncestorOfDep.

 Revision 1.6  2006/10/17 10:45:24  richard
 Handles empty nodes and secondary edges.

 Revision 1.5  2006/09/07 17:51:20  richard
 New revision.

 Revision 1.4  2006/04/12 11:14:41  richard
 Added documentation.
 
 Revision 1.3  2006/04/05 12:12:09  richard
 Changed the span convention to fit with the annotation layers.
 
 Revision 1.2  2005/11/30 09:34:48  richard
 Made some attributes public.
 
 Revision 1.1  2005/11/28 15:41:00  richard
 Added the file.
 
 
 */
package se.lth.cs.nlp.nlputils.pstree;


/**
 * A terminal node in a phrase structure tree.
 */
public class TokenNode extends TerminalNode {
    
    public TokenNode() {
        super();
    }

    public TokenNode(String word, String pos) {
	this();
	this.word = word;
	this.pos = pos;
    }

    public TokenNode(String word, String pos, String lemma) {
	this(word, pos);
	this.lemma = lemma;
    }

    public TokenNode clone() {
    	TokenNode out = new TokenNode();
    	out.word = word;
    	out.pos = pos;
    	out.lemma = lemma;
    	out.setPosition(getPosition());
    	out.setFunction(getFunction());
    	return out;
    }
    
    /**
     * The word.
     */
    private String word;
    
    /**
     * The part of speech.
     */
    private String pos;
    
    /**
     * The lemma, if available.
     */
    private String lemma;
    
    public String toString() {
        return word + "/" + pos + "(" + getPosition() + ")";
    }
    
    public String getLabel() {
        return pos;
    }
    
    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public String getWord() {
        return word;
    }
    
    public void setWord(String word) {
        this.word = word;
    }

    public String getLemma() {
        return lemma;
    }
    
    public void setLemma(String lemma) {
        this.lemma = lemma;
    }
    
    /**
     * Returns true if this is a punctuation token.
     *
     * Warning: this is not Unicode-adapted.
     * 
     * @return true if this is a punctuation token.
     */
    public boolean isPunctuation() {
    	if(pos.matches("IN|TO|RB|AUX|DT"))
    		return false;
        return word.matches("[\\!\\?\\-\\_\\/\\&\\+\\.\\,\\`\\'\\(\\)\\[\\]\\{\\}\\\"\\:\\;]+");
    }
    
    public boolean isEmpty() {        
        return false;
    }

    /* TODO also handle strings *containing* brackets */
    
    public static String escapeBracket(String t) {
    	if(t.length() > 1)
    		return t;
    	char c = t.charAt(0);
    	switch(c) {
    	case '(': return "-LRB-";
    	case ')': return "-RRB-";
    	case '[': return "-LSB-";
    	case ']': return "-RSB-";
    	case '{': return "-LCB-";
    	case '}': return "-RCB-";
    	default: return t;
    	}
    }

    public static String deescapeBracket(String t) {
    	if(t.equals("-LRB-"))
    		return "(";
    	else if(t.equals("-RRB-"))
    		return ")";
    	else if(t.equals("-LSB-"))
    		return "[";
    	else if(t.equals("-RSB-"))
    		return "[";
    	else if(t.equals("-LCB-"))
    		return "{";
    	else if(t.equals("-RCB-"))
    		return "}";
    	return t;
    }
    
    boolean toStrippedBracketing(StringBuilder sb) {
    	String w = escapeBracket(word);
    	String p = escapeBracket(pos);
    	sb.append("(" + p + " " + w + ")");
    	return true;
    }

    public String getText() {
		return word;
	}
    
}
