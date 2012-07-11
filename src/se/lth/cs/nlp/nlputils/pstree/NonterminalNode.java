/*
 NonterminalNode.java

 Copyright (C) 2005, Richard Johansson (richard@cs.lth.se).

 Created in June, 2004 by Richard Johansson (richard@cs.lth.se).

 $Log: NonterminalNode.java,v $
 Revision 1.4  2011-06-09 08:39:58  johansson
 Removed LGPL preamble.

 Revision 1.3  2011-05-26 09:25:19  johansson
 Added getText.

 Revision 1.2  2010-06-07 10:59:29  johansson
 Major update.

 Revision 1.1  2009/01/16 10:09:50  johansson
 Added to brenta repository.

 Revision 1.14  2008/08/14 08:15:34  richard
 Removed subtree and head.

 Revision 1.13  2008/02/14 14:37:37  richard
 Flatten.

 Revision 1.12  2008/01/11 07:54:11  richard
 Added indexOfChild.

 Revision 1.11  2007/08/07 12:38:56  richard
 Complete refactoring.

 Revision 1.10  2007/07/03 06:55:17  richard
 Any node can have a function tag; TerminalNode heads instead of TokenNode; isEmpty.

 Revision 1.9  2007/03/01 09:23:45  richard
 Added comment.

 Revision 1.8  2006/12/08 17:00:53  richard
 Created new package. Implemented new rules.

 Revision 1.7  2006/11/16 21:18:03  richard
 New revision.

 Revision 1.6  2006/09/07 17:51:19  richard
 New revision.

 Revision 1.5  2006/05/11 12:24:27  richard
 Made the function label public.

 Revision 1.4  2006/04/12 11:10:05  richard
 Added documentation.

 Revision 1.3  2006/04/05 12:11:34  richard
 Updated the code to Java 5 standard.

 Revision 1.2  2005/11/30 09:34:48  richard
 Made some attributes public.

 Revision 1.1  2005/11/28 15:41:00  richard
 Added the file.


 */
package se.lth.cs.nlp.nlputils.pstree;

import java.util.*;

import se.lth.cs.nlp.nlputils.core.Pair;

/**
 * Non-terminal node in a phrase structure tree.
 * 
 * @author Richard Johansson (richard@cs.lth.se)
 */
public class NonterminalNode extends Node implements Iterable<Node> {

    public NonterminalNode(String label) {
        this.label = label;
        children = new ArrayList<Node>();
    }

    public NonterminalNode clone() {
    	NonterminalNode out = new NonterminalNode(this.label);
    	out.setFunction(getFunction());
    	out.children = new ArrayList();
    	for(Node c: children) {
    		Node cc = c.clone();
    		cc.setParent(out);
    		out.children.add(cc);
    		if(headChild == c)
    			out.headChild = cc;
    	}
    	return out;
    }
    
    /**
     * The label of the node.
     */
    private String label;

    /**
     * The head token node.
     */
    //private TerminalNode head;

    /**
     * The children of the node.
     */
    private ArrayList<Node> children;

    /**
     * The child that contains the head.
     */
    private Node headChild;

    String tmpHeadToken;

    public String toString() {
        return "(" + label + "/" + getHead() + " " + children + ")";
    }

    /*public Pair<Integer, Integer> span() {
    Integer start = children.get(0).span().left; 
    Integer end = children.get(children.size() - 1).span().right;
    return new Pair<Integer, Integer>(start, end);
}*/
    public TerminalNode getLastTokenLinear() {
        return children.get(children.size() - 1).getLastTokenLinear();
    }
    
    public TerminalNode getFirstTokenLinear() {
        return children.get(0).getFirstTokenLinear();
    }

    public boolean isSameOrAncestorOf(Node n) {
        if(n == this)
            return true;
        if(n.getParent() == null)
            return false;
        return isSameOrAncestorOf(n.getParent());
    }

    public Pair<Integer, Integer> span() {
        Integer start = getFirstTokenTextual().getPosition(); 
        Integer end = getLastTokenTextual().getPosition() + 1;
        return new Pair<Integer, Integer>(start, end);
    }
    
    public TerminalNode getLastTokenTextual() {
        int max = -1;
        TerminalNode last = null;
        for(Node child: this) {
            TerminalNode lc = child.getLastTokenTextual();
            if(lc.getPosition() > max) {
                max = lc.getPosition();
                last = lc;
            }
        }
        return last;
    }

    public TerminalNode getFirstTokenTextual() {
        int min = Integer.MAX_VALUE;
        TerminalNode first = null;
        for(Node child: this) {
            TerminalNode lc = child.getFirstTokenTextual();
            if(lc.getPosition() < min) {
                min = lc.getPosition();
                first = lc;
            }
        }
        return first;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public TerminalNode getHead() {        
        return headChild == null? null: headChild.getHead();
    }

    public Node getHeadChild() {
        return headChild;
    }

    public void setHeadChild(Node headChild) {
        if(headChild != null && !children.contains(headChild))
            throw new IllegalArgumentException("Head child must be a child.");	
        //if(this.headChild != null)
        //    this.headChild.getHead().setSubtree(this.headChild);
        this.headChild = headChild;
        /*if(headChild != null) {            
            this.head = headChild.getHead();
            if(!this.head.getSubtree().isSameOrAncestorOf(this))
                this.head.setSubtree(this);
        } else
            this.head = null;*/
    }

    void setHeads(HeadFinder headFinder) {       
        for(Node child: children)
            child.setHeads(headFinder);
        headFinder.setHeads(this);
    }

    public boolean isEmpty() {        
        // TODO rekursion  
        if(children.size() == 0)
            return true;
        if(children.size() > 1)
            return false;
        if(children.get(0) instanceof EmptyNode)
            return true;
        return false;
    }

    public Iterator<Node> iterator() {
        return getChildren().iterator();
    }

    public List<Node> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public void addChild(Node child) {
        if(child.getParent() != null)
            child.getParent().removeChild(child);
        if(child.tree != null && child.tree != tree) {            
            throw new IllegalStateException("Child is already in another tree: " 
                    + "child = " + child + "\n child.tree id = " + child.tree.hashCode()
                    + "\n this = " + this);
        }
        child.setParent(this);
        children.add(child);
        if(tree != null)
            tree.invalidateCache();
        if(child.tree != tree)
            child.setTree(tree);
    }

    public void addChild(int n, Node child) {
        if(child.getParent() != null)
            child.getParent().removeChild(n);
        if(child.tree != null && child.tree != tree)
            throw new IllegalStateException("Child is already in another tree.");
        child.setParent(this);
        children.add(n, child);
        if(tree != null)
            tree.invalidateCache();
        if(child.tree != tree)
            child.setTree(tree);
    }

    public Node getChild(int i) {
        return children.get(i);
    }

    public Node findChildByLabel(String regex, boolean allowFunction,
            boolean reverse) {
        int start = reverse? children.size() - 1: 0;
        int end = reverse? -1: children.size();
        int d = reverse? -1: 1;
        for(int i = start; i != end; i += d) {
            Node n = children.get(i);
            if(allowFunction || n.getFunction() == null)
                if(n.getLabel().matches(regex))
                    return n;
        }
        return null;
    }
    
    public void replaceChild(Node child, Node replacement) {
        if(child == replacement)
            throw new IllegalArgumentException("child == replacement");
        if(replacement.getParent() != null)
            replacement.getParent().removeChild(replacement);
        if(replacement.tree != null && replacement.tree != tree)
            throw new IllegalStateException("Child is already in another tree.");
        if(headChild == child) {
            headChild = replacement;
            /*child.getHead().setSubtree(child);
            NonterminalNode nn = this;
            Node prev = replacement;
            TerminalNode h0 = head;
            while(nn != null && nn.head == h0) {
                nn.headChild = prev;
                nn.head = prev.getHead();
		if(nn.head != null)
		    nn.head.setSubtree(nn);
                prev = nn;
                nn = nn.getParent();
            }*/
        }
        replacement.setParent(this);
        for(ListIterator<Node> i = children.listIterator(); i.hasNext(); ) {
            Node c = i.next();
            if(c == child) {
                i.set(replacement);
                child.setParent(null);
                child.setTree(null);
                break;
            }
        }
        if(tree != null)
            tree.invalidateCache();
        if(replacement.tree != tree)
            replacement.setTree(tree);
    }

    public void setChild(int n, Node child) {
        if(child.tree != null && child.tree != tree)
            throw new IllegalStateException("Child is already in another tree.");
        if(child.getParent() != null)
            child.getParent().removeChild(child);
        if(headChild == children.get(n)) {
            headChild = child;
            /*NonterminalNode nn = this;
            Node prev = child;
            TerminalNode h0 = head;
            while(nn != null && nn.head == h0) {
                nn.headChild = prev;
                nn.head = child.getHead();
		if(nn.head != null)
		    nn.head.setSubtree(nn);
                prev = nn;
                nn = nn.getParent();
            }*/            
        }
        child.setParent(this);
        Node old = children.get(n);
        old.setParent(null);
        old.setTree(null);
        children.set(n, child);
        if(tree != null)
            tree.invalidateCache();
        if(child.tree != tree)
            child.setTree(tree);
    }

    public Node removeChild(int n) {
        Node child = children.get(n);
        if(headChild == child)
            throw new IllegalStateException("Cannot remove head child!");
        child.setParent(null);
        child.setTree(null);
        if(tree != null)
            tree.invalidateCache();
        Node out = children.remove(n);
        return out;
    }

    public void removeChild(Node child) {
        if(headChild == child)
            throw new IllegalStateException("Cannot remove head child!");
        child.setParent(null);
        if(tree != null)
            tree.invalidateCache();
        child.setTree(null);
        children.remove(child);
    }

    /**
     * 
     * @param start inclusive
     * @param end   non-inclusive
     * @param label
     */
    public void bracket(int start, int end, String label) {
        NonterminalNode newnode = new NonterminalNode(label);
        Node newNodeHeadChild = null;
        for(int i = start; i < end; i++) {
            Node child = children.get(start);
            if(headChild == child) {
                newNodeHeadChild = child;
                //newnode.headChild = child;
                //newnode.head = child.getHead();
                //headChild = newnode;

                //newnode.setHeadChild(child);
                //setHeadChild(newnode);
            }
            children.remove(start);
            child.setParent(newnode);
            newnode.children.add(child);
        }
        newnode.setParent(this);
        children.add(start, newnode);
        if(newNodeHeadChild != null) {
            newnode.setHeadChild(newNodeHeadChild);
            setHeadChild(newnode);
        }
        if(tree != null) {
            newnode.setTree(tree);
            tree.invalidateCache();
        }
    }

    public int indexOfChild(Node child) {
        return children.indexOf(child);
    }

    public int size() {
        return children.size();
    }

    void setTree(PhraseStructureTree tree) {
        super.setTree(tree);
        for(Node c: this)
            c.setTree(tree);
    }

    public void flatten() {
        ArrayList<Node> l = new ArrayList();
        for(Node c: this)
            if(c instanceof NonterminalNode) {
                NonterminalNode nc = (NonterminalNode) c;
                nc.flatten();
                l.addAll(nc.getChildren());
            } else
                l.add(c);
        for(Node c: l)
            c.setParent(this);
        children = l;
        if(tree != null)
            tree.invalidateCache();
    }

    boolean toStrippedBracketing(StringBuilder sb) {
    	StringBuilder sb2 = new StringBuilder();
    	boolean saw = false;
    	for(Node c: children)
    		if(!(c instanceof EmptyNode)) {
    			if(c.toStrippedBracketing(sb2)) {
    				saw = true;
    				sb2.append(" ");
    			}
    		}
    	if(saw) {
        	sb.append("(");
        	sb.append(label);
        	sb.append(" ");
        	sb.append(sb2.toString().trim());
    		sb.append(")");
    	}
    	return saw;
    }

	public String getText() {
		StringBuilder sb = new StringBuilder();
		addText(sb);
		return sb.toString();
	}

    private void addText(StringBuilder sb) {
    	for(int i = 0; i < children.size(); i++) {
    		if(i > 0)
    			sb.append(" ");
    		Node c = children.get(i);
    		if(c instanceof NonterminalNode)
    			((NonterminalNode) c).addText(sb);
    		else
    			sb.append(c.getText());
    	}    	
    }
    
}
