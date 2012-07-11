/*
 DepNode.java
 
 Created on Aug 19, 2005 by Richard Johansson.
 
 $Log: DepNode.java,v $
 Revision 1.4  2010-06-07 11:02:07  johansson
 Major update.

 Revision 1.3  2009/03/20 11:46:57  johansson
 Can handle multi-headed graphs.

 Revision 1.2  2009/02/23 11:46:11  johansson
 Implements comparable.

 Revision 1.1  2009/01/16 10:05:25  johansson
 Added to brenta repository.

 Revision 1.9  2008/05/29 12:17:31  richard
 Added clone in DepGraph.

 Revision 1.8  2008/04/21 08:40:15  richard
 Adds position to toString.

 Revision 1.7  2008/03/18 08:33:50  richard
 Constructors.

 Revision 1.6  2008/01/11 07:53:34  richard
 Fixed bug in removeChild.

 Revision 1.5  2007/12/18 15:13:12  richard
 AddChild and removeChild.

 Revision 1.4  2007/08/27 06:12:04  richard
 getLeftmost/RightmostDescendent

 Revision 1.3  2007/03/30 14:32:41  richard
 Depth.

 Revision 1.2  2007/03/30 13:27:03  richard
 Added comparator.

 Revision 1.1  2007/03/08 13:04:34  richard
 Created new package.

 Revision 1.5  2006/10/18 13:02:41  richard
 Added isSameOrAncestorOf.

 Revision 1.4  2006/09/07 17:51:19  richard
 New revision.

 Revision 1.3  2006/04/11 13:31:39  richard
 Added documentation.
 
 Revision 1.2  2006/04/11 13:25:13  richard
 New revision.
 
 Revision 1.2  2006/01/11 14:19:33  richard
 Added some attributes for the CoNLL shared task.
 
 Revision 1.1  2005/11/02 13:56:23  richard
 Added the file.
 
 
 */
package se.lth.cs.nlp.nlputils.depgraph;

import se.lth.cs.nlp.nlputils.core.*;

import java.util.*;

/**
 * A node in a dependency tree.
 * 
 * @author Richard Johansson (richard@cs.lth.se)
 */
public class DepNode implements Comparable<DepNode> {
    
    /**
     * The parent of this node. Is null for the dummy root node.
     */
    public DepNode[] parents = new DepNode[0];
    
    /**
     * The children of this node.
     */
    public DepNode[] children = new DepNode[0];
    
    /**
     * The relation of this node to its parent.
     */
    public String[] relations = new String[0];
    
    /**
     * The word.
     */
    public String word;
    
    /**
     * The part of speech.
     */
    public String pos;
    
    /**
     * The "fine" part-of-speech (in CoNLL-X terminology).
     */
    public String posFine;
    
    /**
     * The "features" in CoNLL-X terminology.
     */
    public String features;
    
    /**
     * The lemma.
     */
    public String lemma;
    
    /**
     * The position of this node in the dependency graph.
     */
    public int position;
    
    public DepNode() {
        
    }

    public DepNode(int position, String word, String pos, String lemma) {
        this.word = word;
        this.pos = this.posFine = pos;
        this.lemma = lemma;
        this.position = position;
    }
    
    DepNode shallowCopy() {
        DepNode out = new DepNode(position, word, pos, lemma);
        out.posFine = posFine;
        out.features = features;
        return out;
    }
    
    /**
     * The position of the leftmost descendent of this node.
     * @return the position of the leftmost descendent of this node.
     */
    public int getSpanStart() {
        if(children.length == 0 || children[0].position > position)
            return position;
        return children[0].getSpanStart();
    }
    
    public DepNode getLeftmostDescendent() {
        if(children.length == 0 || children[0].position > position)
            return this;
        return children[0].getLeftmostDescendent();
    }

    public DepNode getLeftmostChild() {
    	DepNode n = null;
    	int minPos = Integer.MAX_VALUE;
    	for(int i = 0; i < children.length; i++)
    		if(children[i].position < minPos) {
    			minPos = children[i].position;
    			n = children[i];
    		}
    	return n;
    }
    
    public DepNode getRightmostChild() {
    	DepNode n = null;
    	int maxPos = Integer.MIN_VALUE;
    	for(int i = 0; i < children.length; i++)
    		if(children[i].position > maxPos) {
    			maxPos = children[i].position;
    			n = children[i];
    		}
    	return n;
    }
    
    public DepNode getLeftmostDescendentNew() {
        if(children.length == 0)
            return this;
        return getLeftmostChild().getLeftmostDescendent();
    }
    
    public DepNode getRightmostDescendentNew() {
        if(children.length == 0)
            return this;
        return getRightmostChild().getRightmostDescendent();
    }
    
    /**
     * The position of the rightmost descendent of this node.
     * @return the position of the rightmost descendent of this node.
     */
    public int getSpanEnd() {
        if(children.length == 0 || children[children.length - 1].position < position)
            return position;
        return children[children.length - 1].getSpanEnd();
    }

    public DepNode getRightmostDescendent() {
        if(children.length == 0 || children[children.length - 1].position < position)
            return this;
        return children[children.length - 1].getRightmostDescendent();
    }
    
    public String toString() {
        return "(" + position + ", "+ word + "," + pos + "," + lemma + ")";
    }
    
    /**
     * Adds this node and all its descendents to a collection. The traversal is 
     * inorder, i.e. if the tree is projective, the nodes will be sorted
     * according to their position in the sentence.
     *
     * @param c the collection.
     */
    public void collectSubtree(Collection<DepNode> c) {
        int i = 0;
        for(; i < children.length && children[i].position < position; i++)
            children[i].collectSubtree(c);
        c.add(this);
        for(; i < children.length; i++)
            children[i].collectSubtree(c);
    }
    
    public boolean isSameOrAncestorOf(DepNode n) {
        /*if(n == this)
            return true;
        if(n.parents == null)
            return false;
        for(DepNode p: n.parents)
            if(isSameOrAncestorOf(p))
                return true;
        return false;*/
        return isSameOrAncestorOf(n, new HashSet());
    }

    private boolean isSameOrAncestorOf(DepNode n, HashSet<DepNode> seen) {
        if(n == this)
            return true;
        if(n.parents == null)
            return false;
        if(seen.contains(n))
            return false;
        seen.add(n);
        for(DepNode p: n.parents)
            if(isSameOrAncestorOf(p, seen))
                return true;
        return false;
    }  
    
    public int depth() {
        DepNode d = this;
        int n = 0;
        while(d.parents != null && d.parents.length > 0) {
            n++;
            d = d.parents[0];
        }
        return n;
    }

    public int getMaxBranching() {
    	if(children == null)
    		return 0;
    	int max = children.length;
    	for(DepNode c: children) {
    		int mbc = c.getMaxBranching();
    		if(mbc > max)
    			max = mbc;
    	}
    	return max;
    }
    
    public void addChild(DepNode child, String rel) {
        int pl = child.parents.length;
        child.parents = CollectionUtils.resize(child.parents, pl + 1);
        child.relations = CollectionUtils.resize(child.relations, pl + 1);
        child.parents[pl] = this;
        child.relations[pl] = rel;
        int cl = children.length;
        children = CollectionUtils.resize(children, cl + 1);
        children[cl] = child;        
    }
 
    public void removeChild(DepNode child) {
        int pl = child.parents.length;
        int ix = CollectionUtils.indexOf(child.parents, this);
        if(ix == -1)
            throw new IllegalArgumentException("Parent is not in parent list");
        CollectionUtils.swap(child.parents, ix, pl-1);
        CollectionUtils.swap(child.relations, ix, pl-1);
        child.parents = CollectionUtils.resize(child.parents, pl-1);
        child.relations = CollectionUtils.resize(child.relations, pl-1);
        int cl = children.length;        
        ix = CollectionUtils.indexOf(children, child);
        if(ix == -1)
            throw new IllegalArgumentException("Child is not in child list");
        CollectionUtils.swap(children, ix, cl-1);
        children = CollectionUtils.resize(children, cl - 1);        
    }

    public int compareTo(DepNode o) {
        return position - o.position;
    }
    
}
