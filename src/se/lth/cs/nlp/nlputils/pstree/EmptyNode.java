/*
 EmptyNode.java
 
 Copyright (C) 2007, Richard Johansson (richard@cs.lth.se).
 
 Created in July 1, 2007 by Richard Johansson (richard@cs.lth.se).
 
 $Log: EmptyNode.java,v $
 Revision 1.5  2011-06-09 08:39:58  johansson
 Removed LGPL preamble.

 Revision 1.4  2011-05-26 09:25:19  johansson
 Added getText.

 Revision 1.3  2010-06-07 10:57:04  johansson
 Added clone.

 Revision 1.2  2009/03/20 11:44:27  johansson
 New constructor.

 Revision 1.1  2009/01/16 10:09:50  johansson
 Added to brenta repository.

 Revision 1.2  2007/08/07 12:38:56  richard
 Complete refactoring.

 Revision 1.1  2007/07/03 06:47:27  richard
 Added.

 
 */
package se.lth.cs.nlp.nlputils.pstree;

/**
 * A terminal node in a phrase structure tree.
 */
public class EmptyNode extends TerminalNode {

    public EmptyNode(String label) {
        this.label = label;
    }

    EmptyNode() {        
    }
    
    public EmptyNode clone() {
    	EmptyNode out = new EmptyNode();
    	out.label = label;
    	out.setFunction(getFunction());
    	out.setPosition(getPosition());
    	return out;
    }
    
    /**
     * The word.
     */
    private String label;
    
    public String toString() {
        return "[EMPTY: " + label + "]";
    }
    
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    /**
     * Returns true if this is a punctuation token.
     *
     * @return true if this is a punctuation token.
     */
    public boolean isPunctuation() {
        return false;
    }
    
    public boolean isEmpty() {        
        return true;
    }

	public String getText() {
		return toString();
	}
    
}
