/*
 Options.java
  
 Created on Aug 6, 2008 by Richard Johansson (richard@cs.lth.se).

 $Log: Options.java,v $
 Revision 1.2  2009/03/20 11:52:42  johansson
 Added deep-syntactic links.

 Revision 1.1  2009/03/10 10:18:03  johansson
 Added to brenta repository.

 Revision 1.1  2008/08/14 08:17:32  richard
 Added.

   
 */
package pennconverter;

/**
 * @author Richard Johansson (richard@cs.lth.se)
 */
class Options {
    int coordStructure = PennConverter.MELCHUK_COORD;
    boolean posAsHead = false;
    boolean prepAsHead = true;
    boolean subAsHead = true;
    boolean whAsHead = false;
    boolean imAsHead = true;    
    boolean splitSmallClauses = true;
    boolean advFuncs = true;
    boolean rootLabels = false;
    boolean labelCoords = false;
    boolean splitSlash = true;
    boolean ddtGapping = true;
    boolean name = true;
    boolean suffix = true;
    boolean title = true;
    boolean posthon = true;
    boolean clr = false;
    boolean iobj = false;        
    boolean conll2008clf = true;
    boolean conll2008exp = true;
    boolean relinkCyclicPRN = true;
    boolean oldVMOD = false;
    boolean appo = true;
           
    boolean noPennTags = false;
    boolean noSecEdges = false;

    boolean deepenQP = false;
    boolean qmod = false;

    boolean deepSyntax = false;
    
    boolean rightBranching = true;        
    //boolean conll2008Format = false;
    int format = PennConverter.CONLL_X_FORMAT;

    boolean keepEmpty = false;
    
}