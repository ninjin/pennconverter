/*
 HeadFinder.java
  
 Created on Aug 22, 2006 by Richard Johansson (richard@cs.lth.se).

 $Log: HeadFinder.java,v $
 Revision 1.1  2009/01/16 10:09:50  johansson
 Added to brenta repository.

 Revision 1.1  2006/09/07 17:55:16  richard
 Added.

   
 */
package se.lth.cs.nlp.nlputils.pstree;

/**
 * Abstract class for finding the head nodes in phrase-structure trees.
 * 
 * @author Richard Johansson (richard@cs.lth.se)
 */
public abstract class HeadFinder {

    /**
     * Set the head of a non-terminal node. The heads of its children
     * will already be set.
     * 
     * @param node the node.
     */
    public abstract void setHeads(NonterminalNode node);
    
}
