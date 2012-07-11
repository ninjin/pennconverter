/*
 PennTreeParser.java
  
 Created on Aug 22, 2006 by Richard Johansson (richard@cs.lth.se).

 $Log: PennTreeParser.java,v $
 Revision 1.2  2010-06-07 10:58:03  johansson
 setPositions package-private

 Revision 1.1  2009/01/16 10:09:50  johansson
 Added to brenta repository.

 Revision 1.12  2007/09/26 08:31:59  richard
 Does not crash on reference errors.

 Revision 1.11  2007/08/23 13:40:11  richard
 Debug.

 Revision 1.10  2007/08/22 11:16:14  richard
 Test method.

 Revision 1.9  2007/08/07 12:38:56  richard
 Complete refactoring.

 Revision 1.8  2007/07/03 06:56:15  richard
 Removed lot of English-specific functionality. Also a parse tree factory.

 Revision 1.7  2006/12/14 15:29:44  richard
 Logs messages.

 Revision 1.6  2006/12/12 08:32:22  richard
 Bug fix in coordinated flat NPs.

 Revision 1.5  2006/12/08 17:00:53  richard
 Created new package. Implemented new rules.

 Revision 1.4  2006/11/16 21:18:03  richard
 New revision.

 Revision 1.3  2006/10/17 10:45:24  richard
 Handles empty nodes and secondary edges.

 Revision 1.2  2006/09/25 11:34:28  richard
 Bracket tag bug.

 Revision 1.1  2006/09/07 17:55:16  richard
 Added.

   
 */
package se.lth.cs.nlp.nlputils.pstree;

import java.util.*;
import java.io.*;

/**
 * Tree parser for Penn Treebank trees.
 * 
 * @author Richard Johansson (richard@cs.lth.se)
 */
public class PennTreeParser extends TreeParser {

    /* TODO log errors. */
    
    private Lexer lexer;
    
    public PennTreeParser(BufferedReader reader) {
        lexer = new Lexer(reader, false);
    }
    
    public PennTreeParser(BufferedReader reader, HeadFinder headFinder) {
        super(headFinder);
        lexer = new Lexer(reader, false);
    }

    public int getLineNbr() {
        return lexer.getSentenceLineNbr();
    }

    public String getSentence() {
        return lexer.getSentence();
    }
    
    public boolean hasMoreTrees() {
        return lexer.hasMoreSentences();
    }
    
    String decodeBracket(String word) {
        if(!word.startsWith("-"))
            return word;
        else if(word.equals("-LRB-"))
            return "(";
        else if(word.equals("-RRB-"))
            return ")";
        else if(word.equals("-LSB-"))
            return "[";
        else if(word.equals("-RSB-"))
            return "]";
        else if(word.equals("-LCB-"))
            return "{";
        else if(word.equals("-RCB-"))
            return "}";
        else
            return word;
    }

    static final String NONE = "-NONE-";
    
    public PhraseStructureTree parseTree() {
        lexer.advanceSentence();
        return super.parseTree();
    }
    
    public Node parseNode() {
        if(lexer.current() == null)
            return null;
        
        if(lexer.current().equals("(")) {
            lexer.advance();

            String lbl;
            if(lexer.current().equals("("))
                lbl = "S0";
            else {
                lbl = decodeBracket(lexer.current());
                lexer.advance();
            }
            
            NonterminalNode n = new NonterminalNode(lbl);
            //lexer.advance();

            while(lexer.current() != null && !lexer.current().equals(")")) {
                Node child = parseNode();
                if(child == null)
                    return null;
                if(child instanceof NonterminalNode && ((NonterminalNode) child).getChildren().size() == 1) {
                    Node c2 = (Node) ((NonterminalNode) child).getChildren().get(0);
                    
                    if(c2 instanceof TokenNode && c2.getLabel() == null) {
                        if(child.getLabel().equals(NONE)) {
                            EmptyNode en = new EmptyNode();
                            en.setLabel(((TokenNode) c2).getWord());
                            child = en;
                        } else {
                            ((TokenNode) c2).setPos(child.getLabel());
                            child = c2;
                        }
                    }
                }
                n.addChild(child);
            }
            lexer.eat(")");

            //System.err.println("Closed node, label = " + n.getLabel());
            //System.err.println("Closed node: n = " + n);

            //if(lexer.current() == null && n.getLabel() == null)
            //    n.setLabel("S0");
            
            return n;
        } else {
            TokenNode n = new TokenNode();
            n.setWord(decodeBracket(lexer.current()));
            lexer.advance();
            return n;
        }
    }
    
    public void beforeHeadFinder(PhraseStructureTree tree) {        
        //System.err.println("Here: " + tree.tabbedOutput());
        setPositions(tree.getTopNode(), new int[] {0});
        findSecondaryEdges(tree.getTopNode());
        findFunctionLabels(tree.getTopNode());
    }

    void setPositions(Node n, int[] current) {
        if(n instanceof TerminalNode)
            ((TerminalNode) n).setPosition(current[0]++);
        else
            for(Node c: (NonterminalNode) n)
                setPositions(c, current);
    }
    
    private void findSecondaryEdges(Node n) {
        HashMap<String, NonterminalNode> refs = new HashMap<String, NonterminalNode>();
        findReferents(n, refs);
        findReferences(n, refs);
    }

    private void findReferents(Node n, HashMap<String, NonterminalNode> refs) {
        if(n instanceof TerminalNode)
            return;
        NonterminalNode nn = (NonterminalNode) n;
        int dashIndex = nn.getLabel().lastIndexOf('-');
        if(dashIndex != -1) {
            String id = nn.getLabel().substring(dashIndex + 1);
            try {
                Integer.parseInt(id);
                nn.setLabel(nn.getLabel().substring(0, dashIndex));
                if(refs.containsKey(id)) {
                    //throw new RuntimeException("Node id " + id + " already defined!");
                    
                } else
                    refs.put(id, nn);
            } catch(NumberFormatException e) {
                /* Just to detect non-numbers. */
            }
        }
        for(Node c: nn)
            findReferents(c, refs);
    }
    
    private void findReferences(Node n, HashMap<String, NonterminalNode> refs) {
        if(n instanceof NonterminalNode) {
            NonterminalNode nn = (NonterminalNode) n;
            
            int eqIndex = nn.getLabel().lastIndexOf('=');
            if(eqIndex != -1) {
                String id = nn.getLabel().substring(eqIndex + 1);
                if(refs.containsKey(id)) {
		    //nn.label = nn.label.substring(0, eqIndex);
                    nn.setLabel(nn.getLabel().substring(0, eqIndex));
		    refs.get(id).addSecChild(nn, "=");
                    //nn.secParent = refs.get(id);
                    //nn.secLabel = "=";
                    //if(nn.secParent.secChildren == null)
                    //    nn.secParent.secChildren = new ArrayList<Node>();
                    //nn.secParent.secChildren.add(nn);
                } else {
                    nn.setLabel(nn.getLabel().substring(0, eqIndex));
                    //throw new RuntimeException("Reference to " + id 
                    //			       + " not found ("
		    //			       + nn.getLabel() + ")");
                }
            }
            
            for(Node c: nn)
                findReferences(c, refs);
        } else if(n instanceof EmptyNode) {
            EmptyNode tn = (EmptyNode) n;
            
            int dashIndex = tn.getLabel().lastIndexOf('-');
            if(tn.getLabel().startsWith("*") && dashIndex != -1) {
		String id = tn.getLabel().substring(dashIndex + 1);
                String lbl = tn.getLabel().substring(0, dashIndex);
                tn.setLabel(lbl);
                if(!refs.containsKey(id)) {
                    //throw new RuntimeException("Node id " + id + " not defined!");
                } else
                    refs.get(id).addSecChild(tn, lbl);
                //tn.secParent = refs.get(id);
                //tn.secLabel = tn.label.substring(0, dashIndex); // hmmm
                //tn.label = tn.secLabel;
                //if(tn.secParent.secChildren == null)
                //    tn.secParent.secChildren = new ArrayList<Node>();
                //tn.secParent.secChildren.add(tn);
            }
        }
    }

    private void findFunctionLabels(Node n) {
        if(n instanceof TerminalNode)
            return;
        NonterminalNode nn = (NonterminalNode) n;
        int dashIndex = nn.getLabel().indexOf('-');
        if(dashIndex != -1) {
	    nn.setFunction(nn.getLabel().substring(dashIndex + 1));
            nn.setLabel(nn.getLabel().substring(0, dashIndex));
        }
        for(Node c: nn)
            findFunctionLabels(c);
    }
     
   
    public static void main(String[] argv) {

        //int countError = 0;
        //int count = 0;
        try {
            //BufferedReader br = new BufferedReader(new StringReader(TEST));
            BufferedReader br = new BufferedReader(new java.io.FileReader(argv[0]));
            PennTreeParser tp = new PennTreeParser(br);

            int prev = 0;
            while(tp.hasMoreTrees()) {
                PhraseStructureTree t = tp.parseTree();
                int next = tp.getLineNbr();
                if(next - prev != 1)
                    throw new RuntimeException("prev = " + prev + ", next = " + next);
                prev = next;
                System.out.println(t.tabbedOutput());
            }
        
        } catch(Exception e) {
            e.printStackTrace();
        }        
    }
    
}
