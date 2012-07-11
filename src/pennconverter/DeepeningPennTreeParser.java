
/*
 DeepeningPennTreeParser.java

 Created on Jun 30, 2007 by Richard Johansson (richard@cs.lth.se).

 $Log: DeepeningPennTreeParser.java,v $
 Revision 1.1  2009/03/10 10:18:03  johansson
 Added to brenta repository.

 Revision 1.10  2008/08/14 08:18:03  richard
 Removed GLARF stuff. Refactored and added more options.

 Revision 1.9  2008/08/06 07:21:09  richard
 Final version.

 Revision 1.8  2008/04/08 07:58:27  richard
 New GLARF.

 Revision 1.7  2008/03/06 14:17:10  richard
 Final CoNLL version before release of training data?

 Revision 1.6  2008/02/14 14:39:36  richard
 Updated.

 Revision 1.5  2008/01/11 07:49:02  richard
 First version using GLARF.

 Revision 1.4  2007/09/26 08:30:00  richard
 CoNLL-2008 version.

 Revision 1.3  2007/08/07 12:40:08  richard
 Uses refactored pstree package. Small COORD bugfixes.

 Revision 1.2  2007/07/04 15:01:19  richard
 Implicit right-branching. Advls in coords. Slash splitting.

 Revision 1.1  2007/07/03 07:28:54  richard
 Added.


 */

package pennconverter;

import java.util.*;
import java.io.*;

import se.lth.cs.nlp.nlputils.core.*;
import se.lth.cs.nlp.nlputils.pstree.*;
import se.lth.cs.nlp.nlputils.pstree.english.PennRules;

class DeepeningPennTreeParser extends PennTreeParser {

    private Options options;

    private static MessageLogger logger;
    static void setLogger(MessageLogger logger) {
        DeepeningPennTreeParser.logger = logger;
    }

    DeepeningPennTreeParser(BufferedReader reader, HeadFinder headFinder,
            Options options) {
        super(reader, headFinder);
        this.options = options;
    }

    public void beforeHeadFinder(PhraseStructureTree tree) {        
        if(options.splitSlash)
            splitSlashes(tree.getTopNode());
        super.beforeHeadFinder(tree);
        if(options.title)
            findTitles(tree.getTopNode());
        if(options.posthon)
            findPosthonorifics(tree.getTopNode());
        removeRedundantFunctionTags(tree.getTopNode());
        relabelPhraseTags(tree.getTopNode());
        removeTypo(tree.getTopNode());

        logger.message(2, "Before deepening: " + tree.tabbedOutput());

        deepen(tree.getTopNode());
        if(options.appo)
            findAppositions(tree.getTopNode());
    }

    private void removeRedundantFunctionTags(Node n) {
        if(n.getFunction() != null) {
            String f = n.getFunction();
            f = f.replaceAll("HLN|TTL|NOM|TPC", ""); // structural labels
            f = f.replaceAll("UNF|ETC|IMP", ""); // Brown labels

            if(!options.clr)
                f = f.replaceAll("CLR", "");
            if(options.noPennTags)
                f = "";
            f = f.replaceAll("\\-(\\-+)", "-");
            f = f.replaceAll("^\\-", "");
            f = f.replaceAll("\\-$", "");
            if(f.equals(""))
                f = null;
            n.setFunction(f);
        }
        if(options.noSecEdges)
            n.unlinkSecChildren();

        if(n instanceof NonterminalNode)
            for(Node c: (NonterminalNode) n)
                removeRedundantFunctionTags(c);
    }

    private void findTitles(Node n) {
        if(n instanceof NonterminalNode)
            for(Node c: (NonterminalNode) n)
                findTitles(c);
        else if(n instanceof TokenNode) {
            TokenNode tn = (TokenNode) n;
            NonterminalNode p = tn.getParent();
            if(p.size() < 2 || p.indexOfChild(tn) != 0)
                return;
            if(!tn.getPos().matches("NNP(S?)"))
                return;
            else if(tn.getWord().toLowerCase().matches("(mr|ms|mrs|president|sen|rep|dr|judge|justice|chairman|st|messrs|gen|minister|gov|mayor|cie|prof|col|adm|commodore|sens|king|judges|director|lord|leader|cardinal|professor|private|presidents|commissioner|maj)(\\.)?"))
                tn.setFunction("TITLE");
        }
    }

    private void findPosthonorifics(Node n) {
        if(n instanceof NonterminalNode)
            for(Node c: (NonterminalNode) n)
                findPosthonorifics(c);
        else if(n instanceof TokenNode) {
            TokenNode tn = (TokenNode) n;
            NonterminalNode p = tn.getParent();
            if(p.size() < 2 || p.indexOfChild(tn) != p.size() - 1)
                return;
            if(!tn.getPos().matches("NNP(S?)"))
                return;
            if(tn.getWord().matches("(V?)I+"))
                tn.setFunction("POSTHON");
            else if(tn.getWord().toLowerCase().matches("(corp|inc|co|ltd|jr|s\\.a|cos|n\\.v|bros|sr|phd|ph\\.d)(\\.)?"))
                tn.setFunction("POSTHON");
        }
    }

    private void findAppositions(Node n) {
        if(n instanceof NonterminalNode)
            for(Node c: (NonterminalNode) n)
                findAppositions(c);
        if(n.getLabel().matches("NP|NX|NML|NAC")) {

         	NonterminalNode nt = (NonterminalNode) n;
            if(PennRules.isCoordinated(nt)) {
                return;
            }

            if(nt.size() == 4) {
            	if(nt.getChild(1).getLabel().equals(",") && nt.getChild(3).getLabel().equals(",")
            	   && nt.getChild(0).getLabel().startsWith("NNP") && nt.getChild(2).getLabel().startsWith("NNP")) {
            		// TODO more general?
            		nt.getChild(2).setFunction("APPO");
            		logger.message(2, "Found NNP,NNP, (typically state apposition): " + nt);            	
            		return;
            	}
            }
            
            NonterminalNode np1 = null;
            for(Node c: nt) {
                if(c.getFunction() != null)
                    continue;
                if(c.getLabel().matches("NP|NX|NML|NAC")) {
                    NonterminalNode np = (NonterminalNode) c;
                    Node last = np.getChild(np.size()-1);
                    if(last.getLabel().equals("POS"))
                        continue;
                    if(last instanceof TokenNode
                            && ((TokenNode) last).getWord().equals("'s")) {
                        logger.warning(1, "Annotation error: 's tagged as VBZ.");
                        continue;
                    }
                    np1 = np;
                    break;
                }
            }
            if(np1 == null) {
                return;
            }
            
            /*if(debug)
            	logger.message(2, "XXX3: testing " + n);*/

            boolean sawComma = false;
            for(int i = nt.indexOfChild(np1) + 1; i < nt.size(); i++) {
                Node c = nt.getChild(i);
                if(c.getFunction() != null)
                    continue;
                if(c.getLabel().matches(",|:")) {
                    logger.message(2, "Found comma/colon");
                    sawComma = true;
                } else if(c.getLabel().matches("NP")) {
                    if(sawComma) {
                        logger.message(2, "Found appositive NP after comma: " + c);
                        c.setFunction("APPO");
                    } else {
                        Node f = c.getFirstTokenTextual();
                        if(((NonterminalNode) c).size() == 2
                                && f instanceof TokenNode
                                && ((TokenNode) f).getWord().equals("a")) {
                            logger.message(2, "Found \"a share\"-style NP");
                            c.setFunction("DEP");
                            // himself a successful businessman
                        } else {
                            logger.message(2, "Found appositive NP without comma: " + c);
                            c.setFunction("APPO");
                        }
                    }

                } else if(c.getLabel().equals("ADJP")) {
                    logger.message(2, "Found adjectival postmodifier: " + c);
                    c.setFunction("APPO");
                } else if(c.getLabel().equals("VP")) {
                    logger.message(2, "Found reduced relative: " + c);
                    c.setFunction("APPO");
                } else if(c.getLabel().startsWith("NNP") && sawComma) {
                    // (NAC Litchfield , Conn , )
                    if(nt.getLabel().equals("NAC")) {
                        logger.message(2, "Found state appositive: " + nt);
                        c.setFunction("APPO");
                    } else {
                        logger.message(2, "State appositive not NAC: " + nt);
                    }

                }

                /*else if(c.getLabel().equals("QP")) {
                    logger.message(2, "Found QP postmodifier: " + c);
                    c.setFunction("QPPM");                    
                }*/
            }

        }
    }

    private void relabelPhraseTags(Node n) {
        if(n.getLabel().equals("ADV"))
            ((NonterminalNode) n).setLabel("ADVP");
        else if(n instanceof NonterminalNode
                && n.getLabel().equals("UH"))
            ((NonterminalNode) n).setLabel("INTJ");
        if(n instanceof NonterminalNode)
            for(Node c: (NonterminalNode) n)
                relabelPhraseTags(c);
    }

    private void removeTypo(Node n) {
        if(n instanceof NonterminalNode) {
            NonterminalNode nt = (NonterminalNode) n;
            if(nt.getLabel().equals("TYPO")) {
                if(nt.getChild(0).getLabel().equals("JJ"))
                    nt.setLabel("ADJP");
                else
                    nt.setLabel("NX");                    
            }
            for(Node c: nt)
                removeTypo(c);
        }
    }

    private void splitSlashes(Node n) {
        if(n instanceof TokenNode) {
            TokenNode tn = (TokenNode) n;
            if(tn.getWord().contains("\\/")) {
                if(tn.getWord().matches("[0-9\\.\\-]+(\\\\/[0-9\\.\\-]+)+"))
                    return;

                String[] ts = tn.getWord().split("\\\\/");

                logger.message(2, "Split this slashed token: " + tn);

                String label = null;             
                if(tn.getPos().equals("CD"))
                    label = "QP";
                else if(tn.getPos().startsWith("NN"))
                    label = "NX";
                else if(tn.getPos().startsWith("JJ"))
                    label = "ADJP";
                else if(tn.getPos().equals("IN"))
                    label = "PREP_AUX";
                else if(tn.getPos().equals("CC"))
                    label = "CONJP";
                else if(tn.getPos().startsWith("VB"))
                    label = "VP"; // hmm
                else
                    throw new RuntimeException("Can't split slashed token "
                            + "with pos " + tn.getPos());

                NonterminalNode nt = new NonterminalNode(label);

                tn.setWord(ts[0]);

                for(int i = 1; i < ts.length; i++) {
                    TokenNode sl = new TokenNode("/", "CC");
                    nt.addChild(sl);
                    TokenNode w2 = new TokenNode(ts[i], tn.getPos());
                    nt.addChild(w2);
                }

                tn.getParent().replaceChild(tn, nt);
                nt.addChild(0, tn);

                logger.message(2, "Result: " + nt);
            }
        } else if(n instanceof NonterminalNode)
            for(Node child: (NonterminalNode) n)
                splitSlashes(child);
    }

    private void deepen(Node n) {
        if(n instanceof TerminalNode)
            return;
        NonterminalNode nn = (NonterminalNode) n;
        for(Node c: nn)
            deepen(c);

        if(nn.getLabel().matches("(NP|NX|NML).*")) {
            deepenNP(nn);
        } else if(nn.getLabel().matches("(VP).*"))
            deepenVP(nn);
        else if(nn.getLabel().matches("((WH)?PP).*"))
            deepenPP(nn);
        else if(nn.getLabel().equals("SBAR"))
            deepenSBAR(nn);
        else if(options.deepenQP && nn.getLabel().equals("QP"))
            deepenQP(nn);
    }

    private void boxConjPair(NonterminalNode n, int start) {
        n.bracket(start, start + 3, null);
        NonterminalNode nn = (NonterminalNode) n.getChild(start);

        nn.bracket(0, 1, "NP");
        nn.bracket(2, 3, "NP");

        String pos = nn.getChild(0).getLabel();
        if(pos.startsWith("JJ"))
            nn.setLabel("ADJP");
        else if(pos.equals("CD"))
            nn.setLabel("QP");
        else if(pos.startsWith("RB"))
            nn.setLabel("ADVP");
        else
            nn.setLabel("NX");
    }

    private void deepen3(NonterminalNode node) {
        if(!node.getChild(1).getLabel().matches("CC|CONJP"))
            throw new RuntimeException("Conjunction is not intermediate!");

        node.bracket(0, 1, "NP");
        node.bracket(2, 3, "NP");
    }

    /* (x y) & z */
    private void deepenXY_ZLeft(NonterminalNode node) {
        node.bracket(0, 2, "NX");
        node.bracket(2, 3, "NP");
    }

    /* x (y & z) */
    private void deepenXY_ZRight(NonterminalNode node) {
        boxConjPair(node, 1);
    }

    /* (x & y) z */
    private void deepenX_YZLeft(NonterminalNode node) {
        boxConjPair(node, 0);
    }

    /* x & (y z) */
    private void deepenX_YZRight(NonterminalNode n) {
        throw new RuntimeException("Unimplemented!");
    }

    private boolean tryDeepen5(NonterminalNode node) {

        /* X, Y, Z, A, B, and C */
        int l = 0;
        int maxl = 0;
        for(Node child: node) {
            if(child.getLabel().matches("CONJP|CC")
                    || (child instanceof TokenNode
                            && ((TokenNode) child).isPunctuation())) {
                if(l > maxl)
                    maxl = l;
                l = 0;
            } else {
                l++;
            }
        }
        if(l > maxl)
            maxl = l;
        if(maxl == 1) {
            for(int i = 0; i < node.getChildren().size(); i++) {
                Node child = node.getChild(i);
                if(!child.getLabel().matches("CONJP|CC")
                        && !(child instanceof TokenNode
                                && ((TokenNode) child).isPunctuation()))
                    node.bracket(i, i + 1, "NP");
            }
            return true;
        }

        /* Heuristic: .... X & X ... */
        for(int i = 1; i < node.getChildren().size() - 1; i++) {
            Node child = node.getChild(i);
            if(child.getLabel().matches("CC|CONJP")) {

                Node c1 = node.getChild(i - 1);
                Node c2 = node.getChild(i + 1);

                if(c1.getLabel().equals(c2.getLabel())) {
                    boxConjPair(node, i - 1); // (c1, child, c2);
                    return true;
                }
            }
        }

        return false;
    }

    private void deepenLeftBranches(ArrayList l) {
        for(Object o: l)
            if(o instanceof ArrayList)
                deepenLeftBranches((ArrayList) o);
        if(l.size() > 2
                && l.get(1) instanceof TokenNode) {
            ArrayList lleft = new ArrayList();
            lleft.add(l.get(0));
            l.set(0, lleft);
        }
    }

    private boolean isCoord(ArrayList l) {
        return l.size() > 2 && l.get(1) instanceof TokenNode;
    }

    private void flatten(ArrayList l) {

        for(int i = 0; i < l.size(); i++) {
            Object o = l.get(i);
            if(o instanceof ArrayList) {
                ArrayList l2 = (ArrayList) o;
                if(l2.size() == 1 && l2.get(0) instanceof NonterminalNode)
                    l.set(i, l2.get(0));
                else
                    flatten(l2);
            }
        }

        if(l.get(l.size() - 1) instanceof ArrayList)
            if(l.size() == 2) {
                ArrayList c = (ArrayList) l.get(1);
                if(c.size() == 1)
                    l.set(1, c.get(0));
            } else if(isCoord(l)) {
                ArrayList c = (ArrayList) l.get(l.size() - 1);
                if(isCoord(c)) {
                    l.remove(l.size() - 1);
                    l.addAll(c);
                }
            }
    }

    private Node toNode(Object o) {
        if(o instanceof Node)
            return (Node) o;
        else {
            ArrayList l = (ArrayList) o;
            NonterminalNode out = new NonterminalNode("NX");
            for(Object c: l)
                out.addChild(toNode(c));
            if(PennRules.isCoordinated(out))
                addAdvlsInCoord(out);
            return out;
        }
    }

    private void deepenCoordinationRightBranching(NonterminalNode node) {
        ArrayList top = new ArrayList();
        ArrayList curr = top;
        curr.add(node.getChild(0));
        for(int i = 1; i < node.size(); i++) {
            Node next = node.getChild(i);
            if(!(next instanceof TokenNode
                    && ((TokenNode) next).isPunctuation()
                    || next.getLabel().matches("CC|CONJP"))) {
                ArrayList tmp = new ArrayList();
                curr.add(tmp);
                curr = tmp;
            }
            curr.add(next);
        }
        logger.message(2, "Right-branching this node: " + node);	
        logger.message(2, "  1. top = " + top);
        deepenLeftBranches(top);
        logger.message(2, "  2. top = " + top);        
        flatten(top);
        logger.message(2, "  3. top = " + top);

        while(node.size() > 0)
            node.removeChild(node.size() - 1);

        NonterminalNode out = (NonterminalNode) toNode(top);

        for(Node c: new ArrayList<Node>(out.getChildren()))
            node.addChild(c);

        logger.message(2, "  4. node = " + node);
    }


    private boolean tryDeepen4(NonterminalNode node) {

        Node n1 = node.getChild(0);
        Node n2 = node.getChild(1);
        Node n3 = node.getChild(2);
        Node n4 = node.getChild(3);

        if(n2.getLabel().matches("CC|CONJP")) {

            /* X & Y ltd. */
            if(n4 instanceof TokenNode) {
                TokenNode tn4 = (TokenNode) n4;
                if(tn4.getWord().toLowerCase().matches("ltd(\\.?)|corp(\\.|oration)")) {
                    deepenX_YZLeft(node);
                    return true;
                }
            }

            /* Heuristic: X & X Y */
            if(n1.getLabel().equals(n3.getLabel())) {
                deepenX_YZLeft(node);
                return true;
            }

        } else if(n3.getLabel().matches("CC|CONJP")) {
            /* both X & Y. */
            if(n1 instanceof TokenNode
                    && ((TokenNode) n1).getWord().toLowerCase().equals("both")) {
                deepenXY_ZRight(node);
                return true;
            }
            if(n4 instanceof NonterminalNode) {
                deepenXY_ZLeft(node);
                return true;
            }
            TokenNode tn4 = (TokenNode) n4;

            /* X Y & sons */
            if(tn4.getWord().toLowerCase().matches("co(\\.?)|sons")) {
                deepenXY_ZLeft(node);
                return true;               
            }            

            /* Heuristic: X Y & Y */
            if(n2.getLabel().equals(n4.getLabel())) {
                deepenXY_ZRight(node);
                return true;
            }

        } else if(n1 instanceof TokenNode
                && ((TokenNode) n1).getWord().toLowerCase().matches("both|either"))
            return true;
        else
            throw new RuntimeException("Conjunction is not intermediate! (" + node + ")");

        return false;
    }

    private void addADJP(NonterminalNode node) {
        /* Adjective phrases. */
        for(int i = 1; i < node.size() - 1; i++) {
            Node c = node.getChild(i);
            if(!(c instanceof TokenNode))
                continue;
            TokenNode tn = (TokenNode) c;
            if(tn.getPos().startsWith("JJ")) {
                if(!(node.getChild(i - 1) instanceof TokenNode))
                    continue;
                TokenNode tn2 = (TokenNode) node.getChild(i - 1);
                if(!tn2.getPos().equals("RB"))
                    continue;
                if(tn2.getWord().toLowerCase().matches("only|early|namely"))
                    continue;

                if((tn.getPos().equals("JJ")
                        && tn2.getWord().toLowerCase().matches("(.*)ly|very|once|pretty|quite|rather|too|as"))
                        ||
                        (tn.getPos().equals("JJR")
                                && tn2.getWord().toLowerCase().matches("even|much|slightly|no"))) {
                    node.bracket(i - 1, i + 1, "ADJP");
                    logger.message(2, "Added ADJP in " + node);
                }
            }
        }        
    }

    private static final String MONTH_STRING
    = "jan(\\.)?|january|feb(\\.)?|february|mar(\\.)?|march|apr(\\.)?|april"
        + "may|jun(\\.)?|june|jul(\\.)?|july|aug(\\.)?|august"
        + "sep(\\.)?|september|oct(\\.)?|october|nov(\\.)?|november"
        + "dec(\\.)?|december";

    private static final String DATE_STRING = "[0-3]?[0-9]";

    private void addDateNPTMP(NonterminalNode node) {
        for(int i = 0; i < node.size() - 2; i++) {
            Node c = node.getChild(i);
            if(!(c instanceof TokenNode))
                continue;
            TokenNode tn = (TokenNode) c;
            if(tn.getPos().startsWith("NN")
                    && tn.getWord().toLowerCase().matches(MONTH_STRING)) {
                if(!(node.getChild(i + 1) instanceof TokenNode))
                    continue;
                TokenNode tn2 = (TokenNode) node.getChild(i + 1);
                if(!tn2.getPos().equals("CD") 
                        || !tn2.getWord().matches(DATE_STRING))
                    continue;

                node.bracket(i, i + 2, "NP");
                node.getChild(i).setFunction("TMP");
                logger.message(2, "Added NP-TMP in " + node);
            }
        }        
    }

    private void deepenNPNX(NonterminalNode node) {

        if(!node.getLabel().startsWith("NP"))
            return;

        if(node.size() < 4)
            return;

        int i;
        for(i = node.size() - 1; i >= 0; i--) {
            if(!(node.getChild(i) instanceof TokenNode)
                    || !((TokenNode) node.getChild(i)).isPunctuation())
                break;
        }

        if(i == -1)
            return;

        Node c = node.getChild(i);
        if(c instanceof TerminalNode)
            return;

        NonterminalNode lastChild = (NonterminalNode) c;
        if(!lastChild.getLabel().startsWith("NX"))
            return;

        if(!PennRules.isCoordinated(node))
            return;

        node.bracket(0, i, "NX"); 

        logger.message(2, "Deepened NP-NX: " + node);
    }

    /* Handles coordinations like "golfing or even montgolfing" when
     * unbracketed.
     * 
     * These cases are inconsitently bracketed in the Treebank: usually there
     * is no bracketing, sometimes the adverb is bracketed with the following
     * NP, sometimes it is bracketed with the conjunction into a CONJP.
     * 
     * Here, we add it to the following NP, together with any intervening
     * punctuation.
     */
    private void addAdvlsInCoord(NonterminalNode node) {

        logger.message(2, "node = " + node);

        for(int i = node.size() - 1; i >= 1; i--) {
            Node c = node.getChild(i);
            if(c instanceof TokenNode && ((TokenNode) c).isPunctuation())
                continue;
            if(!(c instanceof NonterminalNode) || c.getLabel().equals("CONJP"))
                break;
            NonterminalNode nc = (NonterminalNode) c;

            logger.message(2, "nc = " + nc);

            for(int j = i - 1; j >= 0; j--) {
                Node prev = node.getChild(j);
                if(prev.getLabel().equals(","))
                    continue;
                else if(prev.getLabel().matches("RB|ADVP|PP")) {
                    logger.message(2, "*** Added coordination adverbial.");
                    logger.message(2, "node = " + node);
                    logger.message(2, "c = " + c);
                    logger.message(2, "prev = " + prev);

                    ArrayList<Node> moved = new ArrayList<Node>();
                    for(int k = j; k < i; k++)
                        moved.add(node.getChild(k));

                    i -= moved.size() - 1;

                    for(int k = moved.size() - 1; k >= 0; k--) {
                        Node m = moved.get(k);
                        node.removeChild(m);
                        nc.addChild(0, m);
                    }

                    logger.message(2, "done: c = " + c);

                    break;
                } else
                    break;
            }
        }        
    }

    private void deepenNP(NonterminalNode node) {        

        // TODO others

        if(node.size() == 0) {
            logger.message(2, "node = " + node);
            logger.message(2, "node.parent = " + node.getParent());
            throw new RuntimeException("Empty node!");
        }

        if(!options.rightBranching) {
            Node last = node.getChild(node.size() - 1);
            if(last.getLabel().equals("POS") && node.size() > 1) {

                node.bracket(0, node.size() - 1, "NP");

                deepenNP((NonterminalNode) node.getChild(0));
                return;
            }

            addADJP(node);
            addDateNPTMP(node);

            deepenNPNX(node); // Utf�rdes tidigare �ven vid rightBranching
        }

        if(!PennRules.isCoordinated(node))
            return;

        addAdvlsInCoord(node);

        boolean sawConj = false;
        boolean sawToken = false;
        for(Node c: node) {
            if(c.getLabel().matches("CC|CONJP"))
                sawConj = true;
            if(!(c instanceof TokenNode))
                continue;
            TokenNode t = (TokenNode) c;
            if(t.getPos().matches("NN(.*)|JJ(.*)|PRP|DT"))
                sawToken = true;
        }

        if(!sawConj || !sawToken)
            return;

        if(options.rightBranching) {
            deepenCoordinationRightBranching(node);
        } else {

            if(node.size() == 3) {
                /* Unambiguous. */

                deepen3(node);

                return;
            } else if(node.size() == 4) {
                if(tryDeepen4(node)) {
                    return;
                }
            } else if(node.size() > 4) {
                if(tryDeepen5(node)) {            
                    return;
                }            
            }
        }

    }

    private void deepenVP(NonterminalNode node) {        
        boolean sawRather = false;
        for(int i = 0; i < node.size() - 1; i++) {
            Node c = node.getChild(i);
            if(!(c instanceof TokenNode))
                continue;
            TokenNode tn = (TokenNode) c;

            if(tn.getWord().toLowerCase().equals("rather"))
                sawRather = true;
            else if(sawRather && tn.getWord().toLowerCase().equals("than")) {
                Node next = node.getChild(i + 1);
                if(!next.getLabel().startsWith("V"))
                    continue;

                node.bracket(i, i + 2, "PP");
                ((NonterminalNode) node.getChild(i)).bracket(0, 1, "PREP_AUX");

                break;
            }
        }
    }

    private void deepenPP(NonterminalNode node) {        

        int i;
        for(i = 0; i < node.size(); i++) {
            Node n = node.getChild(i);
            if(n.getLabel().matches("IN|TO|FW"))
                break;
        }

        if(i == node.size()) {
            for(i = 0; i < node.size(); i++) {
                Node n = node.getChild(i);
                if(n.getLabel().matches("RP|VBG|VBN"))
                    break;
            }
        }

        if(i == node.size())
            return;

        while(i >= 0 && node.getChild(i) instanceof TokenNode
                && !((TokenNode) node.getChild(i)).isPunctuation())
            i--;
        int start = i + 1;

        i++;
        while(i < node.size() 
                && node.getChild(i) instanceof TokenNode
                && !((TokenNode) node.getChild(i)).isPunctuation())
            i++;

        //int end = i - 1;

        node.bracket(start, i, "PREP_AUX");
    }

    private void deepenSBAR(NonterminalNode node) {        
    	// I guess this doesn't work with automatic POS tags
    	// for instance, a "that" might accidentally be tagged as DT
    	
        int i;
        for(i = 0; i < node.size(); i++) {
            Node n = node.getChild(i);
            if(n.getLabel().matches("IN|TO"))
                break;
        }

        if(i == node.size()) {
            for(i = 0; i < node.size(); i++) {
                Node n = node.getChild(i);
                if(n.getLabel().matches("RB"))
                    break;
            }
        }
        
        if(i == node.size())
            return;

        while(i >= 0 && node.getChild(i) instanceof TokenNode
                && !((TokenNode) node.getChild(i)).isPunctuation())
            i--;
        int start = i + 1;

        i++;
        while(i < node.size() 
                && node.getChild(i) instanceof TokenNode
                && !((TokenNode) node.getChild(i)).isPunctuation())
            i++;

        node.bracket(start, i, "PREP_AUX");
    }

    private void deepenQP(NonterminalNode node) {

        for(int i = 0; i < node.size() - 1; i++) {
            Node c = node.getChild(i);
            if(c.getLabel().matches("\\$|\\#")) {
                logger.message(2, "Adding intermediate QP in this QP: " + node);
                node.bracket(i + 1, node.size(), "QP");
                return;
            }
        }

    }

}
