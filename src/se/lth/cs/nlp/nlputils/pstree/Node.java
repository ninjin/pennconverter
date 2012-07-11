/*
 Node.java
 
 Copyright (C) 2005, Richard Johansson (richard@cs.lth.se).
 
 Created in June, 2004 by Richard Johansson (richard@cs.lth.se).
 
 $Log: Node.java,v $
 Revision 1.4  2011-06-09 08:39:58  johansson
 Removed LGPL preamble.

 Revision 1.3  2011-05-26 09:25:19  johansson
 Added getText.

 Revision 1.2  2010-06-07 10:57:28  johansson
 Major update.

 Revision 1.1  2009/01/16 10:09:50  johansson
 Added to brenta repository.

 Revision 1.10  2008/08/14 08:16:09  richard
 Sec-children array not null.

 Revision 1.9  2007/08/07 12:38:56  richard
 Complete refactoring.

 Revision 1.8  2007/07/03 06:54:36  richard
 Any node can have a function tag; TerminalNode heads instead of TokenNode.

 Revision 1.7  2006/12/08 17:00:53  richard
 Created new package. Implemented new rules.

 Revision 1.6  2006/11/16 21:18:03  richard
 New revision.

 Revision 1.5  2006/10/17 10:45:24  richard
 Handles empty nodes and secondary edges.

 Revision 1.4  2006/09/07 17:51:19  richard
 New revision.

 Revision 1.3  2006/04/12 11:04:52  richard
 Added documentation.
 
 Revision 1.2  2005/11/30 09:34:48  richard
 Made some attributes public.
 
 Revision 1.1  2005/11/28 15:41:00  richard
 Added the file.
 
 
 */

package se.lth.cs.nlp.nlputils.pstree;

import java.util.*;
import se.lth.cs.nlp.nlputils.core.Pair;

/**
 * A node in a phrase structure tree. Is either a TokenNode or a NonterminalNode.
 * 
 * @author Richard Johansson (richard@cs.lth.se)
 */
public abstract class Node {
    
    /**
     * Parent node.
     */
    private NonterminalNode parent;

    void setParent(NonterminalNode parent) { // needed in NonterminalNode
        this.parent = parent;
    }

    public abstract Node clone();
    
    protected PhraseStructureTree tree;
    
    //private Node secParent;
    //private String secLabel;
    //private ArrayList<Node> secChildren = new ArrayList(0);
    
    private HashSet<Pair<Node, String>> secParents = new HashSet();
    private HashSet<Pair<Node, String>> secChildren = new HashSet();

    public void addSecChild(Node secChild, String secLabel) {
        //secChild.secParent = this;
        //secChild.secLabel = secLabel;
        //secChildren.add(secChild);

        secChild.secParents.add(new Pair(this, secLabel));
        secChildren.add(new Pair(secChild, secLabel));
    }
    
    /**
     * The function label of the constituent, if available.
     */
    private String functionLabel;
    
    public NonterminalNode getParent() {
        return parent;
    }
    
    /**
     * @deprecated
     */
    public Node getSecParent() {
        if(secParents.size() == 0)
            return null;
        if(secParents.size() > 1)
            throw new IllegalStateException("Has multiple secondary parents");
        return secParents.iterator().next().left;
    }
    
    /**
     * @deprecated
     */
    public String getSecLabel() {
        if(secParents.size() == 0)
            return null;
        if(secParents.size() > 1)
            throw new IllegalStateException("Has multiple secondary parents");
        return secParents.iterator().next().right;
    }
    
    public Collection<Pair<Node, String>> getSecParents() {
        return Collections.unmodifiableCollection(secParents);
    }
    
    public Collection<Pair<Node, String>> getSecChildren() {
        return Collections.unmodifiableCollection(secChildren);
    }
    
    
    public String getFunction() {
        return functionLabel;
    }

    public void setFunction(String functionLabel) {
        this.functionLabel = functionLabel;
    }

    
    public boolean hasParent() {
        return parent != null;
    }
    
    /**
     * Returns the span of the node, that is the index of the leftmost
     * dependent token and the index of the rightmost dependent token + 1.
     * 
     * @return the span of the node.
     */
    public abstract Pair<Integer, Integer> span();
    
    /**
     * Returns true if this node is identical to or is an ancestor of <code>n</code>.
     *
     * @param n the node.
     * @return true if this node is identical to or is an ancestor of <code>n</code>.
     */
    public abstract boolean isSameOrAncestorOf(Node n);
    
    /**
     * For a token node, returns the part of speech; for a nonterminal node,
     * returns its label.
     * 
     * @return the label.
     */
    public abstract String getLabel();
    
    /**
     * Returns the head token node.
     * 
     * @return the head token node.
     */
    public abstract TerminalNode getHead();
    
    /**
     * Returns the rightmost dependent token.
     *
     * @return the rightmost dependent token.
     */
    public abstract TerminalNode getLastTokenTextual();
    
    /**
     * Returns the leftmost dependent token.
     * 
     * @return the rightmost dependent token.
     */
    public abstract TerminalNode getFirstTokenTextual();

    public abstract TerminalNode getFirstTokenLinear();
    public abstract TerminalNode getLastTokenLinear();
    
    /**
     * Returns true if the spans of the nodes overlap.
     * 
     * @param n2 the other node.
     * @return true if the spans of the nodes overlap.
     */
    public boolean overlaps(Node n2) {
        Pair<Integer, Integer> s1 = span();
        Pair<Integer, Integer> s2 = n2.span();
        
        if(s1.left <= s2.left && s1.right >= s2.left)
            return true;
        if(s1.left <= s2.right && s1.right >= s2.right)
            return true;
        if(s2.left <= s1.left && s2.right >= s1.left)
            return true;
        if(s2.left <= s1.right && s2.right >= s1.right)
            return true;
        return false;
    }
    
    void setHeads(HeadFinder headFinder) {       
    }
    
//    public void unlinkSecParent() {
//        secLabel = null;
//        Node p = secParent;
//        p.secChildren.remove(this);
//        //if(p.secChildren.size() == 0)
//        //    p.secChildren = null;
//        secParent = null;
//    }
//
//    public void unlinkSecChildren() {
//	for(Node sc: secChildren) {
//	    sc.secParent = null;
//	    sc.secLabel = null;
//	}
//	secChildren.clear();
//        //secChildren = null;
//    }

    /**
     * @deprecated
     */
    public void unlinkSecParent() {
        if(secParents.size() != 1)
            throw new IllegalStateException("Number of sec parents must be 1");
        unlinkSecParent(secParents.iterator().next().left);
    }
    
    public void unlinkSecParent(Node secParent) {
        for(Iterator<Pair<Node, String>> it = secParents.iterator(); it.hasNext(); ) {
            Pair<Node, String> p = it.next();
            if(p.left != secParent)
                continue;
            boolean found = false;
            for(Iterator<Pair<Node, String>> it2 = secParent.secChildren.iterator(); it2.hasNext(); ) {
                Pair<Node, String> p2 = it2.next();
                if(p2.left == this) {
                    found = true;
                    it2.remove();
                    break;
                }
            }
            if(!found)
                throw new IllegalStateException("illegal graph");
            it.remove();
            break;
        }
    }

    public boolean hasSecParent() {
        return secParents.size() > 0;
    }
    
    public void unlinkSecChildren() {
        for(Iterator<Pair<Node, String>> it = secChildren.iterator(); it.hasNext(); ) {
            Pair<Node, String> p = it.next();
            boolean found = false;
            for(Iterator<Pair<Node, String>> it2 = p.left.secParents.iterator(); it2.hasNext(); ) {
                Pair<Node, String> p2 = it2.next();
                if(p2.left == this) {
                    found = true;
                    it2.remove();
                    break;
                }
            }
            if(!found)
                throw new IllegalStateException("illegal graph");
            it.remove();            
        }
    }
    
    public abstract boolean isEmpty();

    void setTree(PhraseStructureTree tree) {
        this.tree = tree;
    }
    
    boolean toStrippedBracketing(StringBuilder sb) {
    	return false;
    }
 
    public abstract String getText();
    
}

