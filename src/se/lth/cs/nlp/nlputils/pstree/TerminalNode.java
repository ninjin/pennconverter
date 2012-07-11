/*
 TerminalNode.java
 
 Copyright (C) 2007, Richard Johansson (richard@cs.lth.se).
 
 Created on July 1, 2007 by Richard Johansson (richard@cs.lth.se).
 
 $Log: TerminalNode.java,v $
 Revision 1.3  2011-06-09 08:39:58  johansson
 Removed LGPL preamble.

 Revision 1.2  2010-06-07 10:59:13  johansson
 Major update.

 Revision 1.1  2009/01/16 10:09:50  johansson
 Added to brenta repository.

 Revision 1.3  2008/08/14 08:14:53  richard
 Removed subtree.

 Revision 1.2  2007/08/07 12:38:56  richard
 Complete refactoring.

 Revision 1.1  2007/07/03 06:48:23  richard
 Added.

 
 */
package se.lth.cs.nlp.nlputils.pstree;

import se.lth.cs.nlp.nlputils.core.Pair;

/**
 * A terminal node in a phrase structure tree.
 */
public abstract class TerminalNode extends Node implements Comparable<TerminalNode> {
    
    /**
     * The position in the sentence.
     */
    private int position;
    
    //private Node subtree;

    void setHeads(HeadFinder headFinder) {
        //subtree = this;
    }
    
    public Node getSubtree() {
        Node n = this;
        NonterminalNode p = n.getParent();
        while(p != null && p.getHeadChild() == n) {
            n = p;
            p = n.getParent();
        }
        
        return n;
    }
    
    //void setSubtree(Node subtree) {
    //    this.subtree = subtree;
    //}
    
    public Pair<Integer, Integer> span() {
        return new Pair<Integer, Integer>(new Integer(position), new Integer(position + 1));
    }
    
    public boolean isSameOrAncestorOf(Node n) {
        return n == this;
    }
    
    public TerminalNode getPrecedingTerminal() {
        NonterminalNode p = getParent();
        Node c = this;
        while(p != null && p.indexOfChild(c) == 0) {
            c = p;
            p = p.getParent();
        }
        if(p == null)
            return null;
        return p.getChild(p.indexOfChild(c) - 1).getLastTokenLinear();
    }

    public TerminalNode getFollowingTerminal() {
        NonterminalNode p = getParent();
        Node c = this;
        while(p != null && p.indexOfChild(c) == p.size() - 1) {
            c = p;
            p = p.getParent();
        }
        if(p == null)
            return null;
        return p.getChild(p.indexOfChild(c) + 1).getFirstTokenLinear();
    }
    
    public int getPosition() {
        return position;
    }
    
    void setPosition(int position) {
        this.position = position;
    }
    
    /**
     * The dependency parent of this token node.
     *
     * @return the dependency parent of this token node.
     */
    public TerminalNode depParent() {
        Node p = getSubtree().getParent();
        if(p == null)
            return null;
        return p.getHead();
    }
    
    public TerminalNode getHead() {
        return this;
    }
    
    public TerminalNode getLastTokenTextual() {
        return this;
    }
    
    public TerminalNode getFirstTokenTextual() {
        return this;
    }
    
    public TerminalNode getLastTokenLinear() {
        return this;
    }
    
    public TerminalNode getFirstTokenLinear() {
        return this;
    }
    
    /**
     * Returns true if this is a punctuation token.
     *
     * @return true if this is a punctuation token.
     */
    public abstract boolean isPunctuation();
    
    public boolean isSameOrAncestorOfDep(TerminalNode t2) {
        do {
            if(t2 == this)
                return true;
            t2 = t2.depParent();
        } while(t2 != null);
        return false;
    }
 
    public int compareTo(TerminalNode t) {
        return position - t.position;
    }
    
}
