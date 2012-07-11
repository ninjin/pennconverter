/*
 PennRules.java
  
 Created on Dec 14, 2006 by Richard Johansson (richard@cs.lth.se).

 $Log: PennRules.java,v $
 Revision 1.3  2010-06-07 10:55:59  johansson
 Major update.

 Revision 1.2  2009/03/10 10:15:10  johansson
 Improved coordination detection.

 Revision 1.1  2009/01/16 10:09:50  johansson
 Added to brenta repository.

 Revision 1.5  2008/04/22 11:01:00  richard
 Updated.

 Revision 1.4  2007/08/07 12:39:20  richard
 Complete refactoring.

 Revision 1.3  2007/07/04 14:59:56  richard
 Readded.

 Revision 1.1  2007/07/03 07:29:26  richard
 Moved to a separate project.

 Revision 1.1  2006/12/14 15:28:47  richard
 Added the file.

   
 */
package se.lth.cs.nlp.nlputils.pstree.english;

import se.lth.cs.nlp.nlputils.core.MessageLogger;
import se.lth.cs.nlp.nlputils.pstree.*;

/**
 * @author Richard Johansson (richard@cs.lth.se)
 */
public class PennRules {

    private static MessageLogger logger;
    
    public static void setLogger(MessageLogger logger) {
        PennRules.logger = logger;
    }
    
    public static boolean isCoordinated(NonterminalNode node) {
        if(node.getLabel().equals("UCP"))
            return true;
        if(node.getChildren().size() < 2)
            return false;

        int start = 0;
        for(Node c: node)
            if(c instanceof TerminalNode && ((TerminalNode) c).isPunctuation())
                start++;
            else
                break;
        for(int i = start + 1; i < node.getChildren().size() - 1; i++) {
            if(node.getChild(i).getLabel().equals("CONJP"))
                return true;
            if(node.getChild(i).getLabel().equals("CC")
               && !((TokenNode) node.getChild(i)).getWord().toLowerCase().matches("either|neither"))
                return true;
        }
        
        if(!node.getLabel().matches("NP|NX|NML|NAC")) {
            String label = null;
            boolean sawComma = false;
            boolean uniform = true;
            boolean sawToken = false;
            int n = 0;
            for(Node c: node) {
                if(c instanceof NonterminalNode) {
                    n++;
                    if(label == null) {
                        label = c.getLabel();
                        if(label.matches("SINV|SQ|SBARQ"))
                            label = "S";
                    }
                    else { 
                        String label2 = c.getLabel();
                        if(label2.matches("SINV|SQ|SBARQ"))
                            label2 = "S";
                        if(!label.equals(label2))
                            uniform = false;
                    }
                } else if(c instanceof TokenNode) {
                    TokenNode tn = (TokenNode) c;
                    if(!tn.isPunctuation() && !tn.getPos().matches("RB|UH|IN|CC")) {
                        sawToken = true;
                    }
                }
            }
            for(int i = 1; i < node.getChildren().size(); i++)
                if(node.getChild(i).getLabel().matches("[,:]"))
                    sawComma = true;
            
            if(!sawComma)
                for(int i = 0; i < node.getChildren().size(); i++)
                    if(node.getChild(i).getLastTokenTextual().getLabel().matches("[,:]")) {
                        if(logger != null)
                            logger.message(2, "Constituent-ending comma: " + node.getChild(i));
                        sawComma = true;            
                    }
            
            if(sawComma && uniform && n > 1) {
                if(sawToken) {
                    if(logger != null)
                        logger.message(2, "Possible coordination, with token: " + node);
                    return false;
                } else {
                    if(logger != null)
                        logger.message(2, "Possible coordination, no token: " + node);
                    return true;
                }

            }
        } else {
            /* 
               We need to distinguish appositions from lists.                
             */
            
            /* If there is a function tag -> false */
            //logger.message(2, "XXX: Checking NP for coordination");
            //logger.message(2, "XXX: NP = " + node);
            
            for(Node c: node)
                if(c.getFunction() != null
                   && c.getFunction().matches("TMP|LOC")) {
                	if(logger != null)
                		logger.message(2, "Check for coord: Child has function tag " + c.getFunction() + ", returning false");
                    return false;
                }

            boolean sawComma = false;
            for(Node c: node)
                if(c.getLabel().matches("[,;]")) {
                    sawComma = true;
                    break;
                }
            if(!sawComma)
                return false;
            
            int nNP = 0;
            for(Node c: node)
                if(c.getLabel().matches("NP|NX|NML|NAC")) {
                    /* Check for an age "NP". */
                    NonterminalNode np = (NonterminalNode) c;
                    if(np.size() == 1 && np.getChild(0).getLabel().equals("CD")) {
                        if(logger != null)
                        	logger.message(2, "Check for coord: Found an age NP in " + node);
                    } else
                        nNP++;
                }

            if(nNP > 2) {
            	if(logger != null)
            		logger.message(2, "Check for coord: Number of NP children is " + nNP + ", returning true");                
                return true;
            }

        }
        
        return false;
    }
    
    public static boolean isCopula(TerminalNode tn) {
        if(tn instanceof EmptyNode)
            return false;
        TokenNode tn2 = (TokenNode) tn;
        return tn2.getWord().toLowerCase().matches("am|\\'s|is|are|was|were|be|being|been|become|became|becoming|becomes");
    }


    
}
