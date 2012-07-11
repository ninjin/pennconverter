/*
 CollinsTreeParser.java
  
 Created on Aug 22, 2006 by Richard Johansson (richard@cs.lth.se).

 $Log: CollinsTreeParser.java,v $
 Revision 1.1  2009/01/16 10:09:50  johansson
 Added to brenta repository.

 Revision 1.5  2008/08/14 08:16:22  richard
 Removed subtree.

 Revision 1.4  2007/08/14 07:04:35  richard
 Can handle crashed parses.

 Revision 1.3  2007/08/10 13:25:26  richard
 Subtree for token.

 Revision 1.2  2007/08/07 12:38:56  richard
 Complete refactoring.

 Revision 1.1  2006/09/07 17:55:16  richard
 Added.

   
 */
package se.lth.cs.nlp.nlputils.pstree;

import java.io.BufferedReader;
import java.io.StringReader;

/**
 * Parser for the trees that are output by the Collins parser.
 * 
 * @author Richard Johansson (richard@cs.lth.se)
 */
public class CollinsTreeParser extends TreeParser {

    private Lexer lexer;
    
    public CollinsTreeParser(BufferedReader reader) {
        super();
        lexer = new Lexer(reader, true);
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
    
    public PhraseStructureTree parseTree() {
        lexer.advanceSentence();
        return super.parseTree();
    }
    
    public Node parseNode() {
        if(lexer.current().equals("(")) {
            lexer.advance();
            String[] nodeInfo = lexer.current().split("\\~"); 
            NonterminalNode n = new NonterminalNode(nodeInfo[0]);
            if(nodeInfo.length != 4) {
                /* The parser has given up. */
                lexer.eat("TOP");
                while(!lexer.current().equals(")")) {
                    Node child = parseNode();
                    if(child == null)
                        throw new RuntimeException("null");
                    if(!(child instanceof TokenNode))
                        throw new RuntimeException("Should be token node");
                    n.addChild(child);
                }
                if(n.getChildren().size() > 0)
                    n.setHeadChild(n.getChild(0));                
            } else {
                int headPos = 0, nchildren = 0;
                try {
                    nchildren = Integer.parseInt(nodeInfo[2]);
                    headPos = Integer.parseInt(nodeInfo[3]);
                } catch(Exception e) {
                    System.out.println(lexer.current());
                    e.printStackTrace();
                    throw new RuntimeException("Got exception.");
                }
                lexer.advance();
                int count = 0;
                while(lexer.current() != null && !lexer.current().equals(")")) {
                    Node child = parseNode();
                    if(child == null)
                        return null;
                    n.addChild(child);
                    if(child instanceof NonterminalNode ||
                            !child.getLabel().matches("PUNC.*")) {
                        count++;
                        if(count == headPos)
                            n.setHeadChild(child);
                    }
                }
            }
            lexer.eat(")");
            return n;
        } else {
            String[] nodeinfo = lexer.current().split("\\/");
            TokenNode n = new TokenNode();
            if(nodeinfo.length < 2)
                return null;
            String s = nodeinfo[0];
            for(int i = 1; i < nodeinfo.length - 1; i++)
                s += "/" + nodeinfo[i];
            n.setWord(s);
            n.setPos(nodeinfo[nodeinfo.length - 1]);
            //n.setSubtree(n);
            lexer.advance();
            return n;
        }
    }

    static final String TEST = "(TOP~read~1~1 (S~read~3~1 (S~read~2~2 (NPB~Thorne~2~2 Alida/NNP Thorne/NNP ) (VP~read~3~1 read/VBD (NPB~letter~2~2 the/DT letter/NN ) (ADVP~again~1~1 again/RB ,/PUNC, ) ) ) and/CC (S~tore~2~2 (ADVP~then~1~1 then/RB ) (VP~tore~4~1 tore/VBD (NPB~it~1~1 it/PRP ) (PP~into~2~1 into/IN (NPB~squares~5~5 forty/NN or/CC fifty/JJ tiny/JJ squares/NNS ,/PUNC, ) ) (S~shaking~2~2 (NPB~hand~2~2 her/PRP$ hand/NN ) (VP~shaking~2~1 shaking/VBG (PP~with~2~1 with/IN (NPB~anger~1~1 anger/NN ./PUNC. ) ) ) ) ) ) ) ) hejsan \n \n (TOP~may~1~1 (S~may~2~1 (S~may~2~2 (NPB~writers~2~2 (ADJP~monastic~2~2 Possibly/RB monastic/JJ ) writers/NNS ) (VP~may~2~1 may/MD (VP~have~2~1 have/VB (VP~been~3~1 been/VBN (ADJP~chauvinist~3~3 slightly/RB less/RBR chauvinist/JJ ) (PP~in~2~1 in/IN (NPB~outlook~1~1 outlook/NN ;/PUNC: ) ) ) ) ) ) (S~is~2~2 (NP~Albans~2~1 (NPB~Albans~3~3 the/DT St/NNP Albans/NNPS ) (PP~at~3~2 (ADVP~chronicler~1~1 chronicler/JJR ) at/IN (ADJP~least~1~1 least/JJS ) ) ) (VP~is~3~1 is/VBZ (ADJP~effusive~3~2 less/RBR effusive/JJ (PP~about~2~1 about/IN (NPB~Agincourt~1~1 Agincourt/NNP ,/PUNC, ) ) ) (SBAR~although~2~1 although/IN (S~notes~2~2 (NPB~he~1~1 he/PRP ) (VP~notes~3~2 (ADVP~too~1~1 too/RB ) notes/VBZ (NP~reception~2~1 (NPB~reception~3~3 the/DT triumphant/NN reception/NN ) (SBAR~which~2~1 (WHNP~which~1~1 which/WDT ) (S~received~2~2 (NPB~King~2~2 the/DT King/NNP ) (VP~received~2~1 received/VBD (PP~on~2~1 on/IN (NP~return~2~1 (NPB~return~2~2 his/PRP$ return/NN ) (PRN~-LRB-~6~1 -LRB-/-LRB- (NPB~16~2~1 16/CD ,/PUNC, p.111/CD ;/PUNC: ) (NPB~22~2~1 22/CD ,/PUNC, p.70/CD ;/PUNC: ) (NPB~39~1~1 39/CD ,/PUNC, ) (NP~pp.xviii~2~1 (NPB~pp.xviii~1~1 pp.xviii/NNP ,/PUNC, ) (NPB~98-9~1~1 98-9/CD ) ) -RRB-/-RRB- ./PUNC. ) ) ) ) ) ) ) ) ) ) ) ) ) )";
    
    public static void main(String[] argv) {
        try {
            //BufferedReader br = new BufferedReader(new StringReader(TEST));
            BufferedReader br = new BufferedReader(new java.io.FileReader(argv[0]));
            CollinsTreeParser tp = new CollinsTreeParser(br);

            while(tp.hasMoreTrees()) {
                PhraseStructureTree t = tp.parseTree();
                System.out.println(t.tabbedOutput());
            }
        
        } catch(Exception e) {
            e.printStackTrace();
        }        
    }
    
}
