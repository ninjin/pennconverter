/*
 TreeParser.java
  
 Created on Aug 22, 2006 by Richard Johansson (richard@cs.lth.se).

 $Log: TreeParser.java,v $
 Revision 1.1  2009/01/16 10:09:50  johansson
 Added to brenta repository.

 Revision 1.4  2007/08/07 12:38:56  richard
 Complete refactoring.

 Revision 1.3  2007/07/04 14:58:58  richard
 Reordered collection steps.

 Revision 1.2  2007/07/03 07:08:38  richard
 Factory method.

 Revision 1.1  2006/09/07 17:55:16  richard
 Added.

   
 */
package se.lth.cs.nlp.nlputils.pstree;


/**
 * Parser of nested parse trees, such as the Penn Treebank format or
 * the format used by the Collins parser.
 * 
 * @author Richard Johansson (richard@cs.lth.se)
 */
public abstract class TreeParser {
    
    protected HeadFinder headFinder;
    
    public TreeParser() {
    }
    
    public TreeParser(HeadFinder headFinder) {
        this();
        this.headFinder = headFinder;
    }
    
    public PhraseStructureTree parseTree() {

        Node top = parseNode();
        if(top == null)
            return null;
        
        PhraseStructureTree out = new PhraseStructureTree(top);
        top.setTree(out);
        
        beforeHeadFinder(out);

        /* Find heads for each node, if a head finder was supplied. */
        if(headFinder != null)
            out.getTopNode().setHeads(headFinder);

        return out;
    }
    
    /**
     * Returns a node in a parse tree. The first call generates the top
     * node.
     * 
     * @return the node.
     */
    public abstract Node parseNode();

    public abstract boolean hasMoreTrees();
    
    public void beforeHeadFinder(PhraseStructureTree tree) {        
    }
    
}
