/*
 YamadaHeadFinder.java
  
 Created on Oct 17, 2006 by Richard Johansson (richard@cs.lth.se).

 $Log: ModifiedYamadaHeadFinder.java,v $
 Revision 1.4  2010-06-07 10:55:59  johansson
 Major update.

 Revision 1.3  2009/03/20 11:48:03  johansson
 Option whAsHead.

 Revision 1.2  2009/03/10 10:15:45  johansson
 NAC treated as NP.

 Revision 1.1  2009/01/16 10:09:50  johansson
 Added to brenta repository.

 Revision 1.19  2008/08/14 08:17:05  richard
 Removed GLARF stuff.

 Revision 1.18  2008/06/02 12:25:01  richard
 GLARF ULA bug.

 Revision 1.17  2008/04/21 08:39:43  richard
 Dollars in NPs.

 Revision 1.16  2008/03/06 14:17:55  richard
 INTJ, EDITED.

 Revision 1.15  2008/02/14 14:38:46  richard
 Updated rules.

 Revision 1.14  2008/01/11 07:56:34  richard
 GLARF-adapted. Added more systematic handling of wildcards.

 Revision 1.13  2007/12/10 10:04:53  richard
 CoNLL updates.

 Revision 1.12  2007/09/26 08:33:02  richard
 PRT|ADVP, JJ in VP.

 Revision 1.11  2007/08/21 11:51:41  richard
 Charniak POSs.

 Revision 1.10  2007/08/17 10:26:42  richard
 Logger may be null.

 Revision 1.9  2007/08/10 13:25:46  richard
 Also S1.

 Revision 1.8  2007/08/07 12:39:20  richard
 Complete refactoring.

 Revision 1.7  2007/07/05 08:09:44  richard
 Changed QP rules.

 Revision 1.6  2007/07/04 14:59:43  richard
 Improved head rules. Moved out coord detection again.

 Revision 1.5  2007/07/03 07:11:22  richard
 Separate class for empty nodes. Moved isCoordinated here.

 Revision 1.4  2007/03/01 09:21:19  richard
 Added AUX label.

 Revision 1.3  2006/12/15 08:28:58  richard
 HasFunction can use a part of the tag.

 Revision 1.2  2006/12/14 15:29:07  richard
 Configurable head rules.

 Revision 1.1  2006/12/08 17:00:33  richard
 Created new package.

   
 */
package se.lth.cs.nlp.nlputils.pstree.english;

import se.lth.cs.nlp.nlputils.pstree.*;
import se.lth.cs.nlp.nlputils.core.MessageLogger;

/**
 * Modified.
 * 
 * @author Richard Johansson (richard@cs.lth.se)
 */
public class ModifiedYamadaHeadFinder extends HeadFinder {

    private static final String ANY_FUNC = "<<<ANY_FUNC>>>";
    private static final String ANY_LBL = "<<<ANY_LBL>>>";
    
    private ModifiedYamadaHeadFinder() {}
    private static ModifiedYamadaHeadFinder instance = new ModifiedYamadaHeadFinder();

    private static MessageLogger logger;
    
    public static void setLogger(MessageLogger logger) {
        ModifiedYamadaHeadFinder.logger = logger;
    }
    
    public static ModifiedYamadaHeadFinder instance() {
        return instance;
    }
    
    private boolean prepAsHead = false;
    private boolean conjAsHead = false;
    private boolean subAsHead = false;
    private boolean posAsHead = false;
    private boolean imAsHead = false;
    private boolean whAsHead = false;
    
    public void setPrepAsHead(boolean b) {
        prepAsHead = b;
    }
    
    public void setConjAsHead(boolean b) {
        conjAsHead = b;        
    }
    
    public void setSubAsHead(boolean b) {
        subAsHead = b;
    }
    
    public void setIMAsHead(boolean b) {
        imAsHead = b;
    }
    
    public void setPosAsHead(boolean b) {
        posAsHead = b;
    }
    
    public void setWhAsHead(boolean b) {
        whAsHead = b;
    }
    
    private int findLastIndex(NonterminalNode node) {
        if(!PennRules.isCoordinated(node))
            return node.getChildren().size();

        boolean sawPhrase = false;
        for(Node n: node)
            if(n instanceof NonterminalNode) {
                sawPhrase = true;
                break;
            }
        if(!sawPhrase) {
            if(node.getChildren().size() == 3)
                return 1;
            if(logger != null)
                logger.warning(1, "Flat coordinated phrase: " + node);
            if(node.getLabel().matches("NP|NX|NML")) {
                if(logger != null)
                    logger.message(1, "NP. Guessing end.");
                return node.getChildren().size();
            }
        }

        int start = 0;
        for(Node c: node)
            if(c.getFunction() != null 
               || c instanceof TerminalNode && ((TerminalNode) c).isPunctuation())
                start++;
            else
                break;
        
        /* We start at 1, since the conj. might start the phrase. */

        for(int i = start + 1; i < node.getChildren().size(); i++) {
            Node n = node.getChildren().get(i);
            if(n.getLabel().matches("CC|CONJP"))
                return i;
            if(n.getLabel().matches("[,:]"))
                return i;
        }

        return node.getChildren().size();
    }
    
    private Node findChild(NonterminalNode node, int end,
            String labelExp, String funcExp, boolean reverse) {
        int lstart, lend, step;
        if(!reverse) {
            lstart = 0;
            lend = end;
            step = 1;
        } else {
            lstart = end - 1;
            lend = -1;
            step = -1;
        }

        for(int i = lstart; i != lend; i += step) {
            Node c = node.getChild(i);
            if((labelExp == ANY_LBL || c.getLabel().matches(labelExp))
               && hasFunction(c, funcExp)) {
                if(c.getHead() instanceof TokenNode
                   || c.getSecParents().size() > 0)
                    return c;
            }
        }

        if(labelExp == ANY_LBL)
        	return node.getChildren().get(lstart);

        return null;
    }

    private boolean trySetHead(NonterminalNode node, int end,
            String labelExp, String funcExp, boolean reverse) {
        Node child = findChild(node, end, labelExp, funcExp, reverse);
        if(child == null)
            return false;
	node.setHeadChild(child);
        return true;
    }   
    
    private boolean trySetHead(NonterminalNode node, int end,
            String labelExp, boolean reverse) {
        return trySetHead(node, end, labelExp, ANY_FUNC, reverse);
    }
    
    private boolean hasFunction(Node n, String f) {
        if(f == ANY_FUNC)
            return true;
        if(f == null)
            return n.getFunction() == null;
        if(n.getFunction() == null)
            return false;
        return n.getFunction().contains(f);
    }
    
    public void setHeads(NonterminalNode node) {
        int end;
        boolean test = false;

        if(conjAsHead)
            end = node.getChildren().size();
        else
            end = findLastIndex(node);

        /* TODO: 
         * 
         * We should take the NOM dash tag into account here.
         */
        
        /*if(node.getLabel().equals("GLARF")) {
            for(int i = end - 1; i >= 0; i--) {
                Node child = node.getChildren().get(i);
                if(hasFunction(child, null)) {
                    test = true;
                    node.setHeadChild(child);
                }
            }
            if(!test)
                test = trySetHead(node, end, ".*", true);
        } else*/ if(conjAsHead && PennRules.isCoordinated(node)) {
            test = trySetHead(node, end, "CC|CONJP", true)
            || trySetHead(node, end, "\\,|:|;", true)
            || trySetHead(node, end, ANY_LBL, false);
        } else if(node.getLabel().matches("NP|NX|NML|WHNP|NAC")) {
            
            /* Modification: NX also first. */
            if(!test)
                if(posAsHead)
                    test = trySetHead(node, end, "NN|NNP|NNPS|NNS|NX|POS", null, true);
                else
                    test = trySetHead(node, end, "NN|NNP|NNPS|NNS|NX", null, true);

            /* Moved down => Corrected several bugs. */
//            if(!test)
//                test = trySetHead(node, end, "JJR", true)
//                || trySetHead(node, end, "CD", true)
//                || trySetHead(node, end, "JJ", true)
//                || trySetHead(node, end, "JJS", true)
//                || trySetHead(node, end, "RB", true)
//                || trySetHead(node, end, "QP", true);

            /* Modification: Look for NPs with no function tags. */
            if(!test) 
                test = trySetHead(node, end, "NP|NML|WHNP", null, true);

            // Till�gg 080208.
            if(!test)
                test = trySetHead(node, end, "JJR", null, true)
                || trySetHead(node, end, "\\$|\\#", null, false) // 080418
                || trySetHead(node, end, "CD", null, true)
                || trySetHead(node, end, "JJ", null, true)
                || trySetHead(node, end, "JJS", null, true)
                || trySetHead(node, end, "RB", null, true)
                || trySetHead(node, end, "QP", null, false);

            if(!test)
                if(posAsHead)
                    test = trySetHead(node, end, "NN|NNP|NNPS|NNS|NX|POS", true);
                else
                    test = trySetHead(node, end, "NN|NNP|NNPS|NNS|NX", true);                
            
            if(!test)
                test = trySetHead(node, end, "JJR", true)
                || trySetHead(node, end, "\\$|\\#", false) // 080418
                || trySetHead(node, end, "CD", true)
                || trySetHead(node, end, "JJ", true)
                || trySetHead(node, end, "JJS", true)
                || trySetHead(node, end, "RB", true)
                || trySetHead(node, end, "QP", false);
            
            if(!test)
                test = trySetHead(node, end, ANY_LBL, null, true);

            if(!test)
                test = trySetHead(node, end, "NP|NML|WHNP", true)
                || trySetHead(node, end, ANY_LBL, true);
        }
        

        else if(node.getLabel().matches("ADJP|JJP"))
            test = trySetHead(node, end, "NNS", true)
            || trySetHead(node, end, "QP", true)
            || trySetHead(node, end, "NN", true)
            || trySetHead(node, end, "\\$", true)
            || trySetHead(node, end, "ADVP", true)
            || trySetHead(node, end, "JJ", true)
            || trySetHead(node, end, "VBN", true)
            || trySetHead(node, end, "VBG|AUXG", true)
            || trySetHead(node, end, "ADJP|JJP", true)
            || trySetHead(node, end, "JJR", true)
            || trySetHead(node, end, "NP|NML", true)
            || trySetHead(node, end, "JJS", true)
            || trySetHead(node, end, "DT", true)
            || trySetHead(node, end, "FW", true)
            || trySetHead(node, end, "RBR", true)
            || trySetHead(node, end, "RBS", true)
            || trySetHead(node, end, "SBAR", true)
            || trySetHead(node, end, "RB", true)
            || trySetHead(node, end, ANY_LBL, true);

        else if(node.getLabel().startsWith("ADVP"))
            test = trySetHead(node, end, "RB", false)
            || trySetHead(node, end, "RBR", false)
            || trySetHead(node, end, "RBS", false)
            || trySetHead(node, end, "FW", false)
            || trySetHead(node, end, "ADVP", false)
            || trySetHead(node, end, "TO", false)
            || trySetHead(node, end, "CD", false)
            || trySetHead(node, end, "JJR", false)
            || trySetHead(node, end, "JJ", false)
            || trySetHead(node, end, "IN", false)
            || trySetHead(node, end, "NP|NML", false)
            || trySetHead(node, end, "JJS", false)
            || trySetHead(node, end, "NN", false)
            || trySetHead(node, end, ANY_LBL, false);

        else if(node.getLabel().equals("AUX")) {
            test = trySetHead(node, end, "TO|MD", false)
            || trySetHead(node, end, "AUX", false)
            || trySetHead(node, end, ANY_LBL, false);
        }
        
        else if(node.getLabel().equals("CONJP"))            
            test = trySetHead(node, end, "CC", false)            
            || trySetHead(node, end, "RB", false)
            || trySetHead(node, end, "IN", false)            
            || trySetHead(node, end, ANY_LBL, false);

        else if(node.getLabel().equals("EDITED"))            
            test = trySetHead(node, end, ANY_LBL, false);
        
        else if(node.getLabel().equals("FRAG")) {
            /* Modified. */
            test = trySetHead(node, end, "NN|NNP|NNPS|NNS|NX|NP|NML", false)
                   || trySetHead(node, end, "W.*", false)
                   || trySetHead(node, end, "SBAR", false)
                   || trySetHead(node, end, "PP|IN", false)
                   || trySetHead(node, end, "ADJP|JJP|JJ", false)
                   || trySetHead(node, end, "ADVP", false)
                   || trySetHead(node, end, "RB", false);
            if(!test) {
                for(Node child: node)
                    if(child instanceof NonterminalNode) {
                        if(child.getHead() instanceof TokenNode) {
			    node.setHeadChild(child);
                            break;
                        }
                    } else if(!((TerminalNode) child).isPunctuation()) {
			node.setHeadChild(child);
                        break;
                    }
                if(node.getHead() == null) {
		    node.setHeadChild(node.getChild(0));
                }
            }
        }
        
        else if(node.getLabel().equals("INTJ")) {          
            for(Node child: node)
                if(child instanceof NonterminalNode) {
                    if(child.getHead() instanceof TokenNode) {
                            node.setHeadChild(child);
                        break;
                    }
                } else if(!((TerminalNode) child).isPunctuation()) {
                        node.setHeadChild(child);
                    break;
                }
            if(node.getHead() == null) {
                    node.setHeadChild(node.getChild(0));
            }            
        }
            
        else if(node.getLabel().equals("LST"))            
            test = trySetHead(node, end, "LS", false)            
            || trySetHead(node, end, ":", false)        
            || trySetHead(node, end, ANY_LBL, false);
        
        /* 090309
         else if(node.getLabel().equals("NAC"))                    
            test = trySetHead(node, end, "NN|NNP|NNPS|NNS|NX", true)
            || trySetHead(node, end, "NP|NML", true)
            || trySetHead(node, end, "NAC", true)
            || trySetHead(node, end, "EX", true)
            || trySetHead(node, end, "\\$", true)
            || trySetHead(node, end, "CD", true)
            || trySetHead(node, end, "QP", true)
            || trySetHead(node, end, "PRP", true)
            || trySetHead(node, end, "VBG|AUXG", true)
            || trySetHead(node, end, "JJ", true)
            || trySetHead(node, end, "JJS", true)
            || trySetHead(node, end, "JJR", true)
            || trySetHead(node, end, "ADJP|JJP", true)
            || trySetHead(node, end, "FW", true)
            || trySetHead(node, end, ANY_LBL, true);
        */
        
        else if(node.getLabel().equals("NEG")) {
            test = trySetHead(node, end, "RB", false)
            || trySetHead(node, end, ANY_LBL, false);
        }        
        
        else if(node.getLabel().equals("PREP_AUX"))
            test = trySetHead(node, end, "IN", false)
            || trySetHead(node, end, "TO", false)
            || trySetHead(node, end, "VBG|AUXG", false)
            || trySetHead(node, end, "VBN|AUX", false)
            || trySetHead(node, end, "RP", false)
            || trySetHead(node, end, "FW", false)
            || trySetHead(node, end, ANY_LBL, false);

        else if(node.getLabel().matches("(WH)?PP"))     
            
            /* Prepositionsfraser:

               1. IN/TO: "on the grass", "to the country"
               2. VBG: (t.ex.)
                  "according to"
                  "including"
                  "following"
                  "depending"
               3. VBN: "compared", "based", "given", "provided"
               4. RP: "up to" etc. En del buggar.
               5. FW: "vs.", "� la"

             */
            
            if(prepAsHead) {
                test = trySetHead(node, end, "PREP_AUX", false)
                || trySetHead(node, end, ANY_LBL, false);
            } else {
                Node pa = findChild(node, end, "PREP_AUX", ANY_FUNC, false);
                if(pa != null) {
                    Node n = null;
                    boolean found = false;
                    for(int i = end - 1; i >= 0; i--) {
                        n = node.getChild(i);
                        if(n == pa)
                            break;
                        if(!(n instanceof TokenNode)
                           || !((TokenNode) n).isPunctuation()) {
                            found = true;
                            break;
                        }
                    }
                    if(found) {
			node.setHeadChild(n);
                    } else {
                        trySetHead(node, end, ANY_LBL, true);
                    }
                } else {
                    trySetHead(node, end, ANY_LBL, false);
                }                    
            }

        else if(node.getLabel().equals("PRN")) {
            /* Modified. */
            //test = trySetHead(node, end, ".*", true);
            test = trySetHead(node, end, "S.*", false)
            || trySetHead(node, end, "NN|NNP|NNPS|NNS|NX|NP|NML", false)
            || trySetHead(node, end, "W.*", false)
            || trySetHead(node, end, "PP|IN", false)
            || trySetHead(node, end, "ADJP|JJP|JJ", false)
            || trySetHead(node, end, "ADVP", false)
            || trySetHead(node, end, "RB", false);

            if(!test)
                for(Node child: node)
                    if(child instanceof NonterminalNode) {
                        if(child.getHead() instanceof TokenNode) {
			    node.setHeadChild(child);
                            break;
                        }
                    } else if(!((TerminalNode) child).isPunctuation()) {
			node.setHeadChild(child);
                        break;
                    }
            if(!test) {
		node.setHeadChild(node.getChild(0));
            }
        }
        
        else if(node.getLabel().equals("PRT"))
            test = trySetHead(node, end, "RP", false)
            || trySetHead(node, end, ANY_LBL, false);

        else if(node.getLabel().equals("PRT|ADVP"))
            test = trySetHead(node, end, "RP", false)
            || trySetHead(node, end, "RB.*", false)
            || trySetHead(node, end, ANY_LBL, false);
        
        else if(node.getLabel().equals("QP"))
            test = trySetHead(node, end, "\\$|\\#", false)
            //|| trySetHead(node, end, "IN", true)
            || trySetHead(node, end, "NNS", true)
            || trySetHead(node, end, "NN", true)
            || trySetHead(node, end, "JJ", true)
            || trySetHead(node, end, "RB", true)
            || trySetHead(node, end, "DT", true)
            || trySetHead(node, end, "CD", true)
            || trySetHead(node, end, "NCD", true)
            || trySetHead(node, end, "QP", true)
            || trySetHead(node, end, "IN", true) // moved down
            || trySetHead(node, end, "JJR", true)
            || trySetHead(node, end, "JJS", true)
            || trySetHead(node, end, ANY_LBL, true);

        else if(node.getLabel().equals("RRC"))
            test = trySetHead(node, end, "VP", false)
            || trySetHead(node, end, "NP|NML", false)
            || trySetHead(node, end, "ADVP", false)
            || trySetHead(node, end, "ADJP|JJP", false)
            || trySetHead(node, end, "PP", false)
            || trySetHead(node, end, ANY_LBL, false);

        else if(node.getLabel().equals("S")) {
            /*
            test = trySetHead(node, end, "TO", true)
            || trySetHead(node, end, "IN", true)
            || trySetHead(node, end, "VP", true)
            || trySetHead(node, end, "S", true)
            || trySetHead(node, end, "SBAR", true)
            || trySetHead(node, end, "ADJP", true)
            || trySetHead(node, end, "UCP", true)
            || trySetHead(node, end, "NP", true)
            || trySetHead(node, end, ".*", true);
            */

            /* Modified: 

               IN seems to lead to "so" being set as head.

             */

            test = trySetHead(node, end, "VP", true);

            /* Modification: Look for anything with a PRD tag. */
            if(!test)
                for(int i = end - 1; i >= 0; i--) {
                    Node child = node.getChild(i);
                    if(hasFunction(child, "PRD")) {
                        test = true;
			node.setHeadChild(child);
                    }
                }
            if(!test)
                /* Modification: Searches for S from the left. */
                test = trySetHead(node, end, "S|SINV", false)
                || trySetHead(node, end, "SBAR", true)
                || trySetHead(node, end, "ADJP|JJP", true)
                || trySetHead(node, end, "UCP", true)
                || trySetHead(node, end, "NP|NML", true)
                || trySetHead(node, end, ANY_LBL, true);            
        }
            
        else if(node.getLabel().equals("SBAR")) {                   
            /*test = trySetHead(node, end, "WHNP", true)
            || trySetHead(node, end, "WHPP", true)
            || trySetHead(node, end, "WHADVP", true)
            || trySetHead(node, end, "WHADJP", true)
            || trySetHead(node, end, "IN", true)
            || trySetHead(node, end, "DT", true)
            || trySetHead(node, end, "S", true)
            || trySetHead(node, end, "SQ", true)
            || trySetHead(node, end, "SINV", true)
            || trySetHead(node, end, "SBAR", true)
            || trySetHead(node, end, "FRAG", true)
            || trySetHead(node, end, ".*", true);*/
            
            /* I think that DTs here are buggy "that" which should
               have been tagged IN.
             */
            
            test = subAsHead ? 
                    trySetHead(node, end, "PREP_AUX", true) || trySetHead(node, end, "DT", true):
                    false;

            if(!test && whAsHead) {
                test = trySetHead(node, end, "WHNP", false)
                || trySetHead(node, end, "WHPP", false)
                || trySetHead(node, end, "WHADVP", false)
                || trySetHead(node, end, "WHADJP", false);
            }
                    
            if(!test) {
                test = trySetHead(node, end, "S", true)
                || trySetHead(node, end, "SQ", true)
                || trySetHead(node, end, "SINV", true)
                || trySetHead(node, end, "SBAR", true)
                || trySetHead(node, end, "FRAG", true)

                || trySetHead(node, end, "IN", true)
                || trySetHead(node, end, "DT", true)

                || trySetHead(node, end, ANY_LBL, true);
            }
        } else if(node.getLabel().equals("SBARQ")) {               

            if(!test && whAsHead) {
                test = trySetHead(node, end, "WHNP", false)
                || trySetHead(node, end, "WHPP", false)
                || trySetHead(node, end, "WHADVP", false)
                || trySetHead(node, end, "WHADJP", false);
            }
            if(!test)
                test = trySetHead(node, end, "SQ", true)
                || trySetHead(node, end, "S", true)
                || trySetHead(node, end, "SBARQ", true)
                || trySetHead(node, end, "FRAG", true)
                || trySetHead(node, end, ANY_LBL, true);
        }
            
        else if(node.getLabel().equals("SINV")) {
            test = trySetHead(node, end, "VBZ|AUX", true)
            || trySetHead(node, end, "VBD", true)
            || trySetHead(node, end, "VBP", true)
            || trySetHead(node, end, "VB", true)
            || trySetHead(node, end, "MD", true)
            || trySetHead(node, end, "VP", true);
            
            if(!test)
                for(int i = end - 1; i >= 0; i--) {
                    Node child = node.getChild(i);
                    if(hasFunction(child, "PRD")) {
                        test = true;
			node.setHeadChild(child);
                    }
                }

            if(!test)
                test = trySetHead(node, end, "S", true)
                || trySetHead(node, end, "SINV", true)
                || trySetHead(node, end, "ADJP|JJP", true)
                || trySetHead(node, end, "NP|NML", true)
                || trySetHead(node, end, ANY_LBL, true);

        } else if(node.getLabel().equals("SQ")) {
            test = trySetHead(node, end, "VBZ|AUX", true)
            || trySetHead(node, end, "VBD", true)
            || trySetHead(node, end, "VBP", true)
            || trySetHead(node, end, "VB", true)
            || trySetHead(node, end, "MD", true);
            
            if(!test)
                for(int i = end - 1; i >= 0; i--) {
                    Node child = node.getChild(i);
                    if(hasFunction(child, "PRD")) {
                        test = true;
			node.setHeadChild(child);
                    }
                }

            if(!test)
                test = trySetHead(node, end, "SQ", true)
                || trySetHead(node, end, "VP", true) /* <- modified. */
                || trySetHead(node, end, ANY_LBL, true);

        } else if(node.getLabel().equals("UCP"))
            test = trySetHead(node, end, ANY_LBL, false);
        
        else if(node.getLabel().equals("VP")) {  

            if(imAsHead)
                test = trySetHead(node, end, "TO", false);
            else
                test = false;

            if(!test)
                test = trySetHead(node, end, "VBD|AUX", false)
                       || trySetHead(node, end, "VBN", false)
                       || trySetHead(node, end, "MD", false)
                       || trySetHead(node, end, "VBZ", false)
                       || trySetHead(node, end, "VB", false)
                       || trySetHead(node, end, "VBG", false)
                       || trySetHead(node, end, "VBP", false)
                       || trySetHead(node, end, "VP", false);
        
            if(!test)
                for(int i = 0; i < end; i++) {
                    Node child = node.getChild(i);
                    if(hasFunction(child, "PRD")) {
                        test = true;
			node.setHeadChild(child);
                    }
                }
            
            if(!test)
                test = trySetHead(node, end, "ADJP|JJP", false)
            || trySetHead(node, end, "JJ", true) // buggy VBN
            || trySetHead(node, end, "NN", false)
            || trySetHead(node, end, "NNS", false)
            || trySetHead(node, end, "NP|NML", false)
            || trySetHead(node, end, ANY_LBL, false);

        } else if(node.getLabel().equals("WHADJP"))
            test = trySetHead(node, end, "CC", true)
            || trySetHead(node, end, "WRB", true)
            || trySetHead(node, end, "JJ", true)
            || trySetHead(node, end, "ADJP|JJP", true)
            || trySetHead(node, end, ANY_LBL, true);

        else if(node.getLabel().equals("WHADVP"))                    
            test = trySetHead(node, end, "CC", false)
            || trySetHead(node, end, "WRB", false)
            || trySetHead(node, end, ANY_LBL, false);

        /* 080208. Slog ihop med NP. else if(node.getLabel().equals("WHNP")) {
            test = trySetHead(node, end, "NN.*", true)
            || trySetHead(node, end, "WDT", true)
            || trySetHead(node, end, "WP", true)
            || trySetHead(node, end, "WP\\$", true)
            || trySetHead(node, end, "WHADJP", true)
            || trySetHead(node, end, "WHPP", true)
            || trySetHead(node, end, "WHNP", true)
            || trySetHead(node, end, ANY_LBL, true);
        }*/
        /*else if(node.label.equals("WHPP")) {                   
            test = trySetHead(node, end, "IN", false)
            || trySetHead(node, end, "TO", false)
            || trySetHead(node, end, "FW", false)
            || trySetHead(node, end, ".*", false);
        }*/
            
        else if(node.getLabel().equals("X"))
            test = trySetHead(node, end, ANY_LBL, true);
        
        else if(node.getLabel().matches("S0|TOP|S1"))
            test = trySetHead(node, end, ANY_LBL, false);
        
        else if(node.getLabel().matches("NNS")) /* GLARF ULA bug. */
            test = trySetHead(node, end, ANY_LBL, false);

        else
            throw new RuntimeException("Unknown phrase type: " + node.getLabel());
    }
    
}
