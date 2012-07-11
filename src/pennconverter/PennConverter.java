package pennconverter;
/*
 PennConverter.java

 Created on May 10, 2006 by Richard Johansson (richard@cs.lth.se).

 $Log: PennConverter.java,v $
 Revision 1.2  2009/03/20 11:52:42  johansson
 Added deep-syntactic links.

 Revision 1.1  2009/03/10 10:18:03  johansson
 Added to brenta repository.

 Revision 1.15  2008/08/14 08:18:03  richard
 Removed GLARF stuff. Refactored and added more options.

 Revision 1.14  2008/08/06 07:21:50  richard
 Final version of GLARF.

 Revision 1.13  2008/04/08 07:59:19  richard
 New GLARF. Normalizes labels.

 Revision 1.12  2008/03/06 14:17:11  richard
 Final CoNLL version before release of training data?

 Revision 1.11  2008/02/14 14:39:43  richard
 Updated.

 Revision 1.10  2008/01/11 07:49:02  richard
 First version using GLARF.

 Revision 1.9  2007/12/18 15:35:37  richard
 Factored out the printing of the CoNLL format.

 Revision 1.8  2007/12/10 10:04:08  richard
 CoNLL updates.

 Revision 1.7  2007/09/26 08:30:00  richard
 CoNLL-2008 version.

 Revision 1.6  2007/08/07 12:40:08  richard
 Uses refactored pstree package. Small COORD bugfixes.

 Revision 1.5  2007/07/05 08:10:18  richard
 Melchuk-style coordination.

 Revision 1.4  2007/07/04 15:14:34  richard
 VC-TMP.

 Revision 1.3  2007/07/04 15:10:22  richard
 Usage.

 Revision 1.2  2007/07/04 15:01:55  richard
 Implicit right-branching. Advls in coords. Slash splitting. Dep rules. Relinking cycles.

 Revision 1.1  2007/07/03 07:29:16  richard
 Moved to a separate project.

 Revision 1.3  2007/03/08 15:09:22  richard
 Uses new depgraph package.

 Revision 1.2  2006/12/15 08:29:28  richard
 Object predicatives.

 Revision 1.1  2006/12/14 15:28:47  richard
 Added the file.


 */

import java.util.*;
import java.io.*;

import se.lth.cs.nlp.nlputils.pstree.*;
import se.lth.cs.nlp.nlputils.depgraph.*;
import se.lth.cs.nlp.nlputils.core.*;

import se.lth.cs.nlp.nlputils.pstree.english.*;

/**
 * @author Richard Johansson (richard@cs.lth.se)
 */
public class PennConverter {

    private static void usage() {
        System.out.println("Valid options:");
        System.out.println();
        System.out.println("File options:");
        System.out.println("-f FILE\t\t\t\t\tread input from FILE (default: stdin)");
        System.out.println("-t FILE\t\t\t\t\toutput to FILE (default: stdout)");
        System.out.println("-log FILE\t\t\t\twrite log messages to FILE (default: no messages)");
        System.out.println("-verbosity N\t\t\t\tset verbosity level in log file to N (0, 1, or 2; default: 0)");
        System.out.println("-stopOnError[=*true*|false]\t\tterminate if an error is encountered");
        System.out.println();

        System.out.println("Input format options:");
        System.out.println("-rightBranching[=*true*|false]\t\tassume implicit right branching of NPs.");
        System.out.println("\t\t\t\t\tDisable this option if you are NOT using the NP bracketing by Vadas.");   
        System.out.println();
        System.out.println("Shorthand options:");
        System.out.println("-conll2007\t\t\t\tturns on options to emulate the conventions used in CoNLL Shared Task 2007");   
        System.out.println("-raw\t\t\t\t\tturns on options for trees without function tags and secondary edges");   
        System.out.println("-oldLTH\t\t\t\t\tturns on options to emulate the old conventions from the NODALIDA article");   
        System.out.println();
        
        System.out.println("Linguistic options:");
        //System.out.println("-conjAsHead[=true|*false*]\tlet conjunction be head in coordination");
        System.out.println("-coordStructure=oldLTH|prague|*melchuk*\tdetermines how to represent coordination");
        System.out.println("-posAsHead[=true|*false*]\t\tlet possessive be head in possessive NPs");    
        System.out.println("-prepAsHead[=*true*|false]\t\tlet preposition be head in PPs");   
        System.out.println("-subAsHead[=*true*|false]\t\tlet subordinating conjunction (IN/DT) be head in SBARs");   
        
        System.out.println("-whAsHead[=true|*false*]\t\tlet wh-phrase be head in relative clauses");
        
        System.out.println("-imAsHead[=*true*|false]\t\tlet infinitive marker (to) be head in VPs");
        System.out.println("-splitSmallClauses[=*true*|false]\tsplit small clauses into object/OPRD");
        System.out.println("-advFuncs[=*true*|false]\t\tuse adverbial tags such as LOC, TMP");
        System.out.println("-rootLabels[=true|*false*]\t\tuse separate root labels such as ROOT-S, ROOT-FRAG");   
        System.out.println("-labelCoords[=true|*false*]\t\tuse separate coordination labels: SCOORD, VCOORD, COORD");
        System.out.println("-splitSlash[=*true*|false]\t\trewrite A/B as A / B");   
        System.out.println("-ddtGapping[=*true*|false]\t\tDDT-style encoding of gapping"); 
        System.out.println("-conll2008clf[=*true*|false]\t\tannotate cleft sentences as in CoNLL-2008");
        System.out.println("-conll2008exp[=*true*|false]\t\tannotate expletive constructions as in CoNLL-2008");
        System.out.println("-iobj[=true|*false*]\t\t\tuse the IOBJ label for indirect objects");
        System.out.println("-relinkCyclicPRN[=*true*|false]\t\tmove cyclic parentheticals to top");
        System.out.println("-name[=*true*|false]\t\t\tannotate dependencies inside atomic names using NAME");
        System.out.println("-suffix[=*true*|false]\t\t\tuse the SUFFIX label for possessive suffixes");
        System.out.println("-title[=*true*|false]\t\t\tuse the TITLE label for titles in names");
        System.out.println("-posthon[=*true*|false]\t\t\tannotate posthonorifics using POSTHON");
        System.out.println("-appo[=*true*|false]\t\t\tannotate appositions using APPO");
        System.out.println("-clr[=true|*false*]\t\t\tuse the CLR function tag");
        System.out.println("-deepenQP[=true|*false*]\t\tadd additional structure to numerical phrases");
        System.out.println("-qmod[=true|*false*]\t\t\tannotate dependencies inside numerical phrases using QMOD");
        System.out.println("-keepEmpty[=true|*false*]\t\t\tdon't remove empty nodes");
        
        System.out.println("-noPennTags[=true|*false*]\t\tignore function tags if present");
        System.out.println("-noSecEdges[=true|*false*]\t\tignore secondary edges if present");   
        //System.out.println("-deepSyntax[=true|*false*]\t\tadd deep-syntactic links (implies -whAsHead and -format=tab)");
        System.out.println();        
        
        System.out.println("Output format options:");
        System.out.println("-format[=*conllx*|conll2008|tab]\tOutput format");
        
        System.exit(0);
    }

    private static MessageLogger logger = null;

    private static void die(Object message) {
        if(logger != null)
            logger.close();
        System.err.println("Error: " + message);
        System.exit(1);
    }

    static String exString(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private static boolean parseBoolArg(String arg, String argStr) {
        if(arg.equals(argStr))
            return true;
        String end = argStr.substring(arg.length() + 1);
        return Boolean.parseBoolean(end);
    }

    static final int LTH_OLD_COORD = 1;
    static final int PRAGUE_COORD = 2;
    static final int MELCHUK_COORD = 3;

    static final int CONLL_X_FORMAT = 1;
    static final int CONLL_2008_FORMAT = 2;
    static final int TAB_FORMAT = 3;
    
    /**
     * @param argv
     */
    public static void main(String[] argv) {

        try {

            InputStream in = System.in;
            OutputStream out = System.out;
            OutputStream log = null;
            int level = 0;

            boolean stopOnError = true;

            Options options = new Options();

            for(int i = 0; i < argv.length; i++) {
                if(i < argv.length - 1 && argv[i].equals("-f")) {
                    in = new FileInputStream(argv[++i]);
                } else if(i < argv.length - 1 && argv[i].equals("-t")) {
                    out = new FileOutputStream(argv[++i]);
                } else if(i < argv.length - 1 && argv[i].equals("-log")) {
                    log = new FileOutputStream(argv[++i]);
                } else if(argv[i].startsWith("-stopOnError")) {
                    stopOnError = parseBoolArg("-stopOnError", argv[i]);
                } else if(argv[i].startsWith("-coordStructure")) {
                    if(argv[i].equals("-coordStructure=oldLTH"))
                        options.coordStructure = LTH_OLD_COORD;
                    else if(argv[i].equals("-coordStructure=prague"))
                        options.coordStructure = PRAGUE_COORD;
                    else if(argv[i].equals("-coordStructure=melchuk"))
                        options.coordStructure = MELCHUK_COORD;
                    else
                        die("Unknown coordination structure: " + argv[i]);
                } else if(argv[i].startsWith("-prepAsHead")) {
                    options.prepAsHead = parseBoolArg("-prepAsHead", argv[i]);
                } else if(argv[i].startsWith("-posAsHead")) {
                    options.posAsHead = parseBoolArg("-posAsHead", argv[i]);
                } else if(argv[i].startsWith("-subAsHead")) {
                    options.subAsHead = parseBoolArg("-subAsHead", argv[i]);
                } else if(argv[i].startsWith("-imAsHead")) {
                    options.imAsHead = parseBoolArg("-imAsHead", argv[i]);
                } else if(argv[i].startsWith("-whAsHead")) {
                    options.whAsHead = parseBoolArg("-whAsHead", argv[i]);
                } else if(argv[i].startsWith("-splitSmallClauses")) {
                    options.splitSmallClauses = parseBoolArg("-splitSmallClauses", argv[i]);
                } else if(argv[i].startsWith("-advFuncs")) {
                    options.advFuncs = parseBoolArg("-advFuncs", argv[i]);
                } else if(argv[i].startsWith("-rootLabels")) {
                    options.rootLabels = parseBoolArg("-rootLabels", argv[i]);
                } else if(argv[i].startsWith("-labelCoords")) {
                    options.labelCoords = parseBoolArg("-labelCoords", argv[i]);
                } else if(argv[i].startsWith("-splitSlash")) {
                    options.splitSlash = parseBoolArg("-splitSlash", argv[i]);
                } else if(argv[i].startsWith("-ddtGapping")) {
                    options.ddtGapping = parseBoolArg("-ddtGapping", argv[i]);
                } else if(argv[i].startsWith("-name")) {
                    options.name = parseBoolArg("-name", argv[i]);
                } else if(argv[i].startsWith("-suffix")) {
                    options.suffix = parseBoolArg("-suffix", argv[i]);
                } else if(argv[i].startsWith("-title")) {
                    options.title = parseBoolArg("-title", argv[i]);
                } else if(argv[i].startsWith("-posthon")) {
                    options.posthon = parseBoolArg("-posthon", argv[i]);
                } else if(argv[i].startsWith("-clr")) {
                    options.clr = parseBoolArg("-clr", argv[i]);
                } else if(argv[i].startsWith("-iobj")) {
                    options.iobj = parseBoolArg("-iobj", argv[i]);
                } else if(argv[i].startsWith("-conll2008clf")) {
                    options.conll2008clf = parseBoolArg("-conll2008clf", argv[i]);
                } else if(argv[i].startsWith("-conll2008exp")) {
                    options.conll2008exp = parseBoolArg("-conll2008exp", argv[i]);
                } else if(argv[i].startsWith("-relinkCyclicPRN")) {
                    options.relinkCyclicPRN = parseBoolArg("-relinkCyclicPRN", argv[i]);
                } else if(argv[i].startsWith("-oldVMOD")) {
                    options.oldVMOD = parseBoolArg("-oldVMOD", argv[i]);
                } else if(argv[i].startsWith("-noPennTags")) {
                    options.noPennTags = parseBoolArg("-noPennTags", argv[i]);
                } else if(argv[i].startsWith("-noSecEdges")) {
                    options.noSecEdges = parseBoolArg("-noSecEdges", argv[i]);
                } else if(argv[i].startsWith("-rightBranching")) {
                    options.rightBranching = parseBoolArg("-rightBranching", argv[i]);
                } else if(argv[i].startsWith("-deepenQP")) {
                    options.deepenQP = parseBoolArg("-deepenQP", argv[i]);
                } else if(argv[i].startsWith("-qmod")) {
                    options.qmod = parseBoolArg("-qmod", argv[i]);
                } else if(argv[i].startsWith("-appo")) {
                    options.appo = parseBoolArg("-appo", argv[i]);
                } else if(argv[i].startsWith("-keepEmpty")) {
                    options.keepEmpty = parseBoolArg("-keepEmpty", argv[i]);
                } else if(argv[i].startsWith("-deepSyntax")) {
                    options.deepSyntax = parseBoolArg("-deepSyntax", argv[i]);
                    if(options.deepSyntax) {
                        options.format = TAB_FORMAT;
                        options.whAsHead = true;
                    }
                }
                else if(argv[i].equals("-conll2007")) {
                    options.coordStructure = PRAGUE_COORD;
                    options.posAsHead = false;
                    options.prepAsHead = true;
                    options.subAsHead = false;
                    options.imAsHead = false;
                    options.splitSmallClauses = false;
                    options.advFuncs = false;
                    options.rootLabels = false;
                    options.splitSlash = false;
                    options.ddtGapping = false;
                    options.name = false;
                    options.labelCoords = false;
                    options.suffix = false;
                    options.title = false;
                    options.posthon = false;
                    options.clr = true;
                    options.iobj = true;
                    options.conll2008clf = false;
                    options.conll2008exp = false;
                    options.relinkCyclicPRN = false;
                    options.oldVMOD = true;
                    options.appo = false;
                    options.noPennTags = false;
                    options.noSecEdges = false;
                    options.rightBranching = false;
                } else if(argv[i].equals("-oldLTH")) {
                    options.coordStructure = LTH_OLD_COORD;
                    options.posAsHead = false;
                    options.prepAsHead = false;
                    options.subAsHead = false;
                    options.imAsHead = false;
                    options.splitSmallClauses = false;
                    options.advFuncs = true;
                    options.rootLabels = true;
                    options.labelCoords = false;
                    options.splitSlash = false;
                    options.ddtGapping = false;
                    options.name = false;
                    options.suffix = false;
                    options.title = false;
                    options.posthon = false;
                    options.clr = true;
                    options.iobj = true;
                    options.conll2008clf = false;
                    options.conll2008exp = false;
                    options.relinkCyclicPRN = false;
                    options.oldVMOD = true;
                    options.appo = false;
                    options.noPennTags = false;
                    options.noSecEdges = false;
                    options.rightBranching = false;
                } else if(argv[i].equals("-raw")) {
                    options.noPennTags = true;
                    options.noSecEdges = true;
                    options.rightBranching = false;
                } else if(argv[i].startsWith("-format")) {
                    if(argv[i].equals("-format=conllx"))
                        options.format = CONLL_X_FORMAT;
                    else if(argv[i].equals("-format=conll2008"))
                        options.format = CONLL_2008_FORMAT;
                    else if(argv[i].equals("-format=tab"))
                        options.format = TAB_FORMAT;
                    else
                        die("Unknown format: " + argv[i]);
                } 
                else if(argv[i].matches("-help|--help|-\\?|--\\?|-usage|--usage")) {
                    usage();
                } else if(i < argv.length - 1 && argv[i].equals("-verbosity")) {
                    try {
                        level = Integer.parseInt(argv[++i]);
                    } catch(Exception e) {
                        die("Verbosity level must be integer.");
                    }
                } else {
                    die("Unknown parameter: " + argv[i]);
                }
            }
            if(options.deepSyntax && options.format != TAB_FORMAT)
                throw new RuntimeException("Deep syntax requires tab format");
            
            logger = new MessageLogger(log, level);
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(out));

            PhraseStructureTree.setLogger(logger);
            DeepeningPennTreeParser.setLogger(logger);
            PennRules.setLogger(logger);            

            HeadFinder headFinder = ModifiedYamadaHeadFinder.instance();            
            //HeadFinder headFinder = YamadaHeadFinder.instance();

            if(headFinder instanceof ModifiedYamadaHeadFinder) {
                ModifiedYamadaHeadFinder hf = (ModifiedYamadaHeadFinder) headFinder;
                hf.setConjAsHead(options.coordStructure == PRAGUE_COORD);
                hf.setPrepAsHead(options.prepAsHead);
                hf.setPosAsHead(options.posAsHead);
                hf.setSubAsHead(options.subAsHead);
                hf.setIMAsHead(options.imAsHead);
                hf.setWhAsHead(options.whAsHead);
                ModifiedYamadaHeadFinder.setLogger(logger);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            DeepeningPennTreeParser tp = new DeepeningPennTreeParser(reader, 
                    headFinder, options);

            int n = 0, nproj = 0;

            while(tp.hasMoreTrees()) {

                logger.message(2, "\n***** Processing sentence " + n + ". *****");

                PhraseStructureTree tree = null;
                try {
                    tree = tp.parseTree();
                }
                catch(Exception e) {
                    logger.error(0, "*** ERROR when reading/deepening: "
                            + e.getMessage() + "***\n"
                            + "(At line " + tp.getLineNbr() + ")\n"
                            + "For this sentence: " + tp.getSentence());
                    logger.message(2, exString(e));
                    if(stopOnError)
                        die("Exiting.");
                    continue;
                }

                if(tree == null) {
                    logger.error(0, "*** ERROR when reading/deepening: Returned null "
                            + "***\n"
                            + "(At line " + tp.getLineNbr() + ")");
                    die("Exiting.");
                }

                n++;
                if(n % 50 == 0) {
                    System.err.print(".");
                    System.err.flush();
                }

                if(n % 1000 == 0) {
                    System.err.println(" (" + n + ")");
                }                    

                logger.setProperty("n", "" + n);

                logger.message(2, "*** After reading/deepening Penn tree: ***\n"
                        + tree.tabbedOutput());

                try {
                    tree.checkConsistency();
                } catch(Exception e) {
                    logger.error(0, "*** ERROR - consistency check failed: " 
                            + e.getMessage() + "***");
                    logger.message(2, exString(e));
                    die("Exiting.");
                    continue;
                }

                try {
                    if(options.conll2008clf)
                        moveCleftSBAR(tree);
                } catch(Exception e) {
                    logger.error(0, "*** ERROR when moving cleft SBARs: " 
                            + e.getMessage() + "***");
                    logger.message(2, exString(e));
                    if(stopOnError)
                        die("Exiting.");
                    continue;
                }

                try {
                    if(options.splitSmallClauses)
                        splitObjPRD(tree);
                } catch(Exception e) {
                    logger.error(0, "*** ERROR when splitting objects/predicatives: " 
                            + e.getMessage() + "***");
                    logger.message(2, exString(e));
                    if(stopOnError)
                        die("Exiting.");
                    continue;
                }

                try {
                    inferNewEdgeLabels(tree, options);
                } catch(Exception e) {
                    logger.error(0, "*** ERROR when inferring edge labels: " 
                            + e.getMessage() + "***");
                    logger.message(2, exString(e));
                    if(stopOnError)
                        die("Exiting.");
                    continue;
                }

                logger.message(2, "*** After inferring edge labels: ***\n"
                        + tree.tabbedOutput());

                try {
                    removeRedundantSecEdges(tree, options);
                } catch(Exception e) {
                    logger.error(0, "*** ERROR when removing sec edges: "
                            + e.getMessage() + "***");
                    logger.message(2, exString(e));
                    if(stopOnError)
                        die("Exiting.");
                    continue;
                }

                try {
                    relinkNodes(tree, options);
                } catch(Exception e) {
                    logger.error(0, "*** ERROR when re-linking sec edges: " 
                            + e.getMessage() + "***");
                    logger.message(2, exString(e));
                    if(stopOnError)
                        die("Exiting.");
                    continue;
                }

                logger.message(2, "*** After re-linking secondary edges: ***\n"
                        + tree.tabbedOutput());

                try {
                    tree.checkConsistency();
                } catch(Exception e) {
                    logger.error(0, "*** ERROR - consistency check failed: " 
                            + e.getMessage() + "***");
                    logger.message(2, exString(e));
                    die("Exiting.");
                    continue;
                }

                try {
                    finalEdgeLabels(tree.getTopNode(), options);
                } catch(Exception e) {
                    logger.error(0, "*** ERROR in finalEdgeLabels: " 
                            + e.getMessage() + "***");
                    logger.message(2, exString(e));
                    if(stopOnError)
                        die("Exiting.");
                    continue;
                }

                try {
                    if(options.ddtGapping)
                        relabelGapping(tree);
                } catch(Exception e) {
                    logger.error(0, "*** When relabeling gapping: " 
                            + e.getMessage() + "***");
                    logger.message(2, exString(e));
                    if(stopOnError)
                        die("Exiting.");
                    continue;
                }

                logger.message(2, "*** After finalEdgeLabels: ***\n"
                        + tree.tabbedOutput());

                try {
                    tree.checkConsistency();
                } catch(Exception e) {
                    logger.error(0, "*** ERROR - consistency check failed: " 
                            + e.getMessage() + "***");
                    logger.message(2, exString(e));
                    die("Exiting.");
                    continue;
                }
                
                try {
                    if(options.deepSyntax)
                        addSecondaryLinks(tree, options);
                    else
                        for(Node node: tree.getNodes())
                            node.unlinkSecChildren();
                            
                } catch(Exception e) {
                    logger.error(0, "*** When creating secondary links: " 
                            + e.getMessage() + "***");
                    logger.message(2, exString(e));
                    if(stopOnError)
                        die("Exiting.");
                    continue;
                }
                
                try {
                    tree.checkConsistency();
                } catch(Exception e) {
                    logger.error(0, "*** ERROR - consistency check failed: " 
                            + e.getMessage() + "***");
                    logger.message(2, exString(e));
                    die("Exiting.");
                    continue;
                }
                
                DepGraph depgraph = null;

                try {
                    depgraph = tree.toDepGraph(options.keepEmpty);                
                } catch(Exception e) {
                    logger.error(0, "*** ERROR when converting to dependencies: " 
                            + e.getMessage() + "***");
                    logger.message(2, exString(e));
                    if(stopOnError)
                        die("Exiting.");
                    continue;
                }

                try {
                    if(options.noPennTags)
                        noPennTags(depgraph);
                    if(!options.advFuncs)
                        removeAdvFuncs(depgraph);
                    if(!options.rootLabels)
                        removeRootLabels(depgraph);

                } catch(Exception e) {
                    logger.error(0, "*** ERROR when post-processing dep tree: " 
                            + e.getMessage() + "***");
                    logger.message(2, exString(e));
                    if(stopOnError)
                        die("Exiting.");
                    continue;
                }

                try {
                    if(options.coordStructure == MELCHUK_COORD) {
                        toMelchukCoords(depgraph);
                    }
                } catch(Exception e) {
                    logger.error(0, "*** ERROR when relinking coordinations: " 
                            + e.getMessage() + "***");
                    logger.message(2, exString(e));
                    if(stopOnError)
                        die("Exiting.");
                    continue;
                }

                logger.message(2, "*** After conversion to dependency tree: ***\n"
                        + depgraph);

                normalizeLabels(depgraph);
                
                try {
                    depgraph.checkConsistency();
                } catch(Exception e) {
                    logger.error(0, "*** ERROR - consistency check failed: " 
                            + e.getMessage() + "***");
                    logger.message(2, exString(e));
                    die("Exiting.");
                }

                if(depgraph.isProjective())
                    nproj++;
                else
                    logger.message(2, "*** This tree is non-projective. ***");

                switch(options.format) {
                case CONLL_X_FORMAT:
                    CoNLLFormat.printGraph(pw, depgraph);
                    break;
                case CONLL_2008_FORMAT:
                    printCoNLL2008Format(pw, depgraph);
                    break;
                case TAB_FORMAT:
                    TabFormat.printGraph(pw, depgraph);
                    break;
                default:
                    throw new RuntimeException("Illegal format option");
                }

            }

            logger.message(0, "Number of projective sentences: " + nproj + " / " + n
                    + " = " + (double) nproj / n);

            logger.close();
            pw.close();

            System.err.println();
            System.err.println("Number of errors: " + logger.getNErrors());
            System.err.println("Number of warnings: " + logger.getNWarnings());            
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    static void normalizeLabels(DepGraph depgraph) {
        for(int i = 1; i < depgraph.nodes.length; i++) {
            String[] ls = depgraph.nodes[i].relations[0].split("-");
            TreeSet<String> sorted = new TreeSet();
            for(String s: ls)
                sorted.add(s);
            StringBuilder sb = new StringBuilder();
            for(String s: sorted) {
                sb.append(s);
                sb.append('-');
            }
            depgraph.nodes[i].relations[0] = sb.substring(0, sb.length() - 1);
        }
    }

    static void printCoNLL2008Format(PrintWriter pw, DepGraph depgraph) {
        for(int i = 1; i < depgraph.nodes.length; i++)
            if(depgraph.nodes[i].parents.length != 1)
                throw new IllegalArgumentException("Every node must have a single parent!");

        for(int i = 1; i < depgraph.nodes.length; i++) {

            pw.print(i + "\t");
            //pw.print("_\t_\t_\t");
            pw.print(depgraph.nodes[i].word);
            pw.print("\t");

            /* Lemma. */
            if(depgraph.nodes[i].lemma == null)
                pw.print("_");
            else
                pw.print(depgraph.nodes[i].lemma);
            pw.print("\t");

            /* Original POS. */
            if(depgraph.nodes[i].pos == null)
                pw.print("_");
            else
                pw.print(depgraph.nodes[i].pos);
            pw.print("\t");                                

            /* Automatic POS. */
            pw.print("_");
            pw.print("\t");

            /* Word. */
            pw.print(depgraph.nodes[i].word);
            pw.print("\t");

            /* Lemma. */
            if(depgraph.nodes[i].lemma == null)
                pw.print("_");
            else
                pw.print(depgraph.nodes[i].lemma);
            pw.print("\t");

            /* POS. */
            if(depgraph.nodes[i].pos == null)
                pw.print("_");
            else
                pw.print(depgraph.nodes[i].pos);
            pw.print("\t");

            pw.print(depgraph.nodes[i].parents[0].position);
            pw.print("\t");
            pw.print(depgraph.nodes[i].relations[0]);

            pw.println();
        }
        pw.println();
        pw.flush();        
    }

    static boolean isPotentialObject(PhraseStructureTree tree, Node n) {
        if(n instanceof TerminalNode)
            return false;
        NonterminalNode nt = (NonterminalNode) n;

        if(nt.getFunction() != null)
            return false;

        if(nt.getSecChildren() != null)
            for(Pair<Node, String> p: nt.getSecChildren())
                if(p.right.matches("\\*ICH\\*|\\*EXP\\*|\\*RNR\\*"))
                    return false;

        if(nt.getLabel().matches("NP|S|SQ|SINV|SBARQ"))
            // ska frag vara med??? problematiskt!
            return true;

        if(nt.getLabel().equals("UCP")) {
            for(Node c: nt)
                if(isPotentialObject(tree, c))
                    return true;
            return false;
        }            

        if(nt.getLabel().equals("INTJ")) {
            /* says `` ouch '', says no */

            //if(true)
            //    throw new RuntimeException("unimplemented");
            
            //Pair<Integer, Integer> sp = nt.span();
            TerminalNode pre = nt.getFirstTokenLinear().getPrecedingTerminal();
            
            //logger.message(2, "INTJ as OBJ: " + nt);
            //return true;
            
            if(pre != null && pre.getLabel().matches("``|VB(.?)"))
                return true;
        }

        if(nt.getLabel().equals("FRAG")) {

            /* " ... ", says X. */
            if(nt.size() == 1
                    && nt.getChild(0).hasSecParent())
                return true;

            /* X says why. */
            if(nt.size() == 1
                    && nt.getChild(0).getLabel().startsWith("W"))
                return true;

            //if(true)
            //    throw new RuntimeException("unimplemented");
            TerminalNode pre = nt.getFirstTokenLinear().getPrecedingTerminal();
                
            /* X says " ... ". */
            //Pair<Integer, Integer> sp = nt.span();
            if(pre != null && pre instanceof TokenNode
                    && ((TokenNode) pre).getWord().equals("``"))
                return true;

            return false;
            //return true;
        }

        if(!nt.getLabel().equals("SBAR"))
            return false;

        //TokenNode tn = tokens.get(nt.span().left);

        TerminalNode h = nt.getHead();
        if(h instanceof TokenNode && ((TokenNode) h).getWord().toLowerCase().matches("as|with|for|since|because"))
            return false;
        
        //if(nt.getFirstToken() instanceof TokenNode
        //        && ((TokenNode) nt.getFirstToken()).getWord().toLowerCase().matches("as|with|for|since|because"))
        //    return false;

        /* rule out so... that constructions. */
        if(nt.getFirstTokenTextual() instanceof EmptyNode
                || ((TokenNode) nt.getFirstTokenTextual()).getWord().equalsIgnoreCase("that")) {
            for(Node s: nt.getParent()) {
            	String fun = s.getFunction();
            	if(s.getLabel().equals("ADVP")
                        && s.getFirstTokenTextual() instanceof TokenNode
                        && ((TokenNode) s.getFirstTokenTextual()).getWord().equalsIgnoreCase("so")
                        && (fun == null || fun.matches("CLR|XADV"))) {
                    return false;
                }
            }
        }

        return true;
    }

    static void inferNewEdgeLabels(PhraseStructureTree tree,
            Options options) {

        for(Node cn: tree.getNodes()) {

            if(cn instanceof TerminalNode)
                continue;

            NonterminalNode child = (NonterminalNode) cn;

            /* Remove buggy BNF labels. */
            if(child.getLabel().equals("NP")
                    && child.getFunction() != null
                    && child.getFunction().equals("BNF")) {
                logger.warning(1, "Removed buggy BNF from object.");
                child.setFunction(null);
            }

            /* Remove labels in UCPs. */
            if(child.getLabel().equals("UCP"))
                for(Node c2: child)
                    c2.setFunction(null);

            /* 1. Clefts. */
            if(child.getFunction() != null
                    && child.getFunction().equals("CLF"))
                moveCleftLabel(child);

            /* 2. Expletives. */
            for(Pair<Node, String> p: child.getSecChildren())
                if(p.right.equals("*EXP*")) {
                    p.left.unlinkSecParent(child);
                    if(options.conll2008exp)
                        child.setFunction("EXTR");
                    else
                        child.setFunction("EXP");
                    break;
                }

            if(child.getFunction() != null)
                continue;

            NonterminalNode parent = child.getParent();

            if(parent == null)
                continue;

            /* 3. Objects. */
            if(parent.getLabel().equals("VP") 
                    && !PennRules.isCopula(parent.getHead())
                    && !(parent.getHead() instanceof TokenNode
                            && ((TokenNode) parent.getHead()).getWord().toLowerCase().equals("going"))
                            && isPotentialObject(tree, child)) {

                boolean saw = false;
                ArrayList<Node> objects = new ArrayList<Node>();
                for(Node c: parent) {
                    if(c == child)
                        saw = true;
                    else if(saw && isPotentialObject(tree, c))
                        objects.add(c);
                }

                if(objects.size() > 0) {
                    if(!child.getLabel().equals("NP")) {
                        logger.message(2, "verb = " + parent.getHead());
                        logger.message(2, "first object = " + child.getHead());
                        for(Node n: objects)
                            logger.message(2, "other object = " + n.getHead());
                        logger.warning(1, "The first of multiple objects was not an NP. "
                                + "Almost certainly an annotation error. "
                                + "Trying to recover by setting the others to ADV.");

                        child.setFunction("OBJ");
                        for(Node obj2: objects) {
                            NonterminalNode o2 = (NonterminalNode) obj2;
                            if(o2.getLabel().equals("NP"))
                                o2.setFunction("OBJ");
                            else
                                o2.setFunction("XADV");
                        }

                    } else {
                        /* We have two or more objects. */

                        if(options.iobj)
                            child.setFunction("IOBJ");
                        else
                            child.setFunction("OBJ");

                        if(objects.size() > 1)
                            logger.warning(1, (objects.size() + 1) + " objects.");

                        for(Node obj2: objects)
                            ((NonterminalNode) obj2).setFunction("OBJ");
                    }

                } else {
                    child.setFunction("OBJ");
                }

            }

            if(parent.getLabel().matches("VP|SQ|SINV") 
                    && parent.getHeadChild() == parent.getHead()
                    && child.getLabel().equals("VP")
                    && child.getHead() instanceof TokenNode
                    && child.getHead().getLabel().matches("VB.*|MD")) {
                //child.functionLabel = "VC";
            }

            /* 4. PRN. */
            if(child.getLabel().equals("PRN"))
                child.setFunction("PRN");

            /* 5. PRT. */
            if(child.getLabel().equals("PRT"))
                child.setFunction("PRT");

            /* 6. Moving LGS. */
            for(Node c: child) {
                if(c instanceof NonterminalNode) {
                    NonterminalNode nnc = (NonterminalNode) c;
                    if(nnc.getFunction() != null
                            && nnc.getFunction().equals("LGS")) {
                        child.setFunction("LGS");
                        nnc.setFunction(null);
                        break;
                    }
                }
            }

            /* 7. Additional ADV. */
            if(parent.getLabel().matches("VP|S|SINV|SQ|SBARQ|FRAG")
                    && child.getLabel().matches("PP|ADVP|SBAR")
                    && child.getFunction() == null
                    && child.getSecChildren().size() == 0)
                child.setFunction("XADV");
            
        }

        for(Node c: tree.getNodes())
            if(c instanceof NonterminalNode) {
                NonterminalNode nt = (NonterminalNode) c;
                if(nt.getFunction() == null)
                    continue;
                if(nt.getFunction().equals("CLFTMP"))
                    nt.setFunction("CLF");
                else if(nt.getFunction().equals("XADV"))
                    nt.setFunction("ADV");
            }
    }

    private static void moveCleftLabel(NonterminalNode n) {
        n.setFunction(null);

        for(Node c: n) {
            if(c.getLabel().equals("VP")) {
                NonterminalNode cnt = (NonterminalNode) c;
                for(Node c2: cnt) {                    
                    if(c2.getLabel().equals("SBAR")
                            && ((NonterminalNode) c2).getFunction() == null) {
                        ((NonterminalNode) c2).setFunction("CLFTMP");
                        return;
                    }
                }
            }
        }
        logger.warning(1, "Found no empty SBAR in cleft!");
    }

    private static void moveCleftSBAR(PhraseStructureTree tree) {

        for(Node n: tree.getNodes()) {
            if(n.getFunction() == null || !n.getFunction().equals("CLF"))
                continue;
            n.setFunction(null);

            for(Node c: (NonterminalNode) n) {
                if(c.getLabel().equals("VP")) {

                    NonterminalNode cnt = (NonterminalNode) c;

                    Node clf = null;
                    NonterminalNode prd = null;

                    for(Node c2: cnt) {                    
                        if(c2.getLabel().equals("SBAR")
                                && c2.getFunction() == null)
                            clf = c2;
                        else if(c2 instanceof NonterminalNode
                                && c2.getFunction() != null
                                && c2.getFunction().equals("PRD"))
                            prd = (NonterminalNode) c2;
                    }

                    if(clf != null && prd != null) {
                        prd.addChild(clf);
                        //if(prd.getHead() instanceof EmptyNode)
                        //    prd.setHeadChild(clf);
                    }
                }
            }
        }
    }

    private static void relabelGapping(PhraseStructureTree tree) {
        for(Node n: tree.getNodes()) {
            //Node sp = n.getSecParent();
            for(Pair<Node, String> p: n.getSecParents()) {
                if(p.right.equals("="))
                    n.unlinkSecParent(p.left); // 090312
                if(n.getFunction() == null || !n.getFunction().equals("GAPTMP"))
                    continue;
                //if(sp != null)
                n.setFunction(p.left.getFunction() + "-GAP");
            }
        }
    }

    private static void splitObjPRD(PhraseStructureTree tree) {
        for(Node n: tree.getNodes()) {
            if(!n.getLabel().equals("VP"))
                continue;
            NonterminalNode vp = (NonterminalNode) n;
            if(vp.getHead() != vp.getHeadChild())
                continue;
            if(PennRules.isCopula(vp.getHead()))
                continue;
            
            String verb = null;
            if(vp.getHead() instanceof TokenNode)
                verb = ((TokenNode) vp.getHead()).getWord().toLowerCase();
            if(verb != null && verb.equals("going"))
                continue;

            for(int i = 0; i < vp.size(); i++) {
                Node c = vp.getChild(i);
                if(!c.getLabel().equals("S"))
                    continue;
                if(c.getFunction() != null)
                    continue;

                if(verb != null && !verb.matches("ha(ve|s|d|ving)|intend(s|ed|ing)?")) {
                    if(c.getFirstTokenTextual().getLabel().equals("``")) {
                        logger.message(2, "Not splitting S starting with quote");
                        logger.message(2, "c.getfirsttoken = " + c.getFirstTokenTextual());
                        logger.message(2, "for c = " + c);
                        // problem with these sentences:
                        // 06: has [S `` a team '' in place]
                        // 07: intends [S `` * to go ...''] 
                        // 07: have [S `` * to spend ...'']
                        continue;
                    }

                    if(i > 0) {
                        Node prev = vp.getChild(i-1);
                        // he said `` they come ''
                        // he said : they come
                        if(prev instanceof TokenNode
                                && ((TokenNode) prev).getPos().matches("``|''|:|!")) // TODO punc
                            continue;
                    }
                }
                
                NonterminalNode nc = (NonterminalNode) c;
                if(nc.getSecChildren().size() > 0)
                    continue;
                if(nc.getHeadChild().isEmpty())
                    continue;
                
                for(Node c2: nc) {
                    if(c2 instanceof TerminalNode)
                        continue;

                    NonterminalNode nc2 = (NonterminalNode) c2;

                    if(nc2.getFunction() != null
                            && nc2.getFunction().equals("SBJ")) {
                        if(nc.getHeadChild() == nc2) {
                            nc.setHeadChild(nc.getChild(nc.size() - 1));
                            logger.warning(1, "Object in Obj+PRD was head!");
                        }
                        if(nc.getHeadChild() != nc2) {
                            /* TODO handle intervening punctuation */

                            /* TODO this empty node has position 0... */
                            EmptyNode trace = new EmptyNode("*XXXCTRL*");
                            int pos = nc.indexOfChild(nc2);
                            
                            nc.addChild(pos, trace);
                            //tree.recomputePositions();
                            
                            nc.bracket(pos, pos + 1, nc2.getLabel());
                            NonterminalNode nc3 = trace.getParent();
                            nc3.setHeadChild(trace);
                            nc3.setFunction("SBJ");
                            nc2.addSecChild(trace, "*XXXCTRL*");
                            
                            nc.removeChild(nc2);
                            vp.addChild(i, nc2);
                            nc2.setFunction("OBJ");
                        } else {
                            nc.setFunction("OBJ");
                            logger.warning(1, "Object in Obj+PRD was the only constituent!");
                        }
                        break;
                    }
                }

                if(nc.getFunction() != null)
                    logger.warning(1, "Probably bug: nc has function");

                String func = nc.getHeadChild().getFunction();
                if(func == null)
                    func = "PRD";
                if(!func.equals("PRD"))
                    logger.message(2, "Main constituent in OPRD has function");
                func = func.replaceAll("PRD", "OPRD");
                nc.setFunction(func);
                if(!func.contains("OPRD"))
                    logger.warning(1, "Function = " + func + ", doesn't contain OPRD");
                
                i++; // skip the newly created S.
            }
        }
    }    

    private static void removeRedundantSecEdges(PhraseStructureTree tree, Options opts) {
        for(Node nn: tree.getNodes()) {
            //String rel = nn.getSecLabel();
            for(Pair<Node, String> p: nn.getSecParents()) {
                if(p.right.matches("\\*PPA\\*|\\*EXP\\*"))
                    nn.unlinkSecParent(p.left);
                if(!opts.deepSyntax && p.right.equals("*"))
                    nn.unlinkSecParent(p.left);
            }
        }
    }

    public static void relinkAux(PhraseStructureTree tree) {
        for(Node nn: tree.getNodes()) {
            if(!nn.getLabel().equals("AUX"))
                continue;
            NonterminalNode node = (NonterminalNode) nn;
            NonterminalNode p = node.getParent();

            NonterminalNode n = node;        
            while(n.getChild(0) instanceof NonterminalNode)
                n = (NonterminalNode) n.getChild(0);

            logger.message(2, "node = " + node);
            logger.message(2, "n = " + n);
            logger.message(2, "p = " + p);

            int i = p.getChildren().indexOf(node);
            while(true) {
                if(p.size() == i + 1)
                    break;
                Node c = p.getChildren().get(i + 1);
                if(c.getLabel().matches("CC|CONJP"))
                    break;
                //p.children.remove(c);
                p.removeChild(c);
                //n.children.add(c);
                //c.parent = n;
                n.addChild(c);
            }
            node.setLabel("VP");
        }
    }


    private static void relinkNodes(PhraseStructureTree tree, Options opt) {

        HashSet<Node> done = new HashSet<Node>();
        /* 1. Re-link traces, discontinous constituents, right node raising. */
        for(Node n: tree.getNodes()) {
            if(!n.hasSecParent())
                continue;
            if(n.getSecParents().size() > 1)
                throw new RuntimeException("Multiple secondary parents not allowed here");

            Pair<Node, String> p = n.getSecParents().iterator().next();
            Node secParent = p.left;
            String secLabel = p.right;
            
            if(secLabel.matches("\\*T\\*|\\*ICH\\*|\\*RNR\\*")) {

                /* Re-link the constituent. */                

                if(n.getParent().size() != 1) {
                    //logger.message(1, "Warning: parent of empty node has multiple children.");
                    //logger.message(2, "n.parent = " + n.getParent());
                    //throw new RuntimeException("Parent of empty seems strange!");
                }

                if(opt.whAsHead && secParent.getLabel().startsWith("W")) {
                    if(!opt.deepSyntax)
                        n.unlinkSecParent(secParent);
                    continue;
                }
                
                if(secParent.getFunction() != null)
                    logger.warning(1, "Secondary parent has function.");

                if(secParent.isSameOrAncestorOf(n)) {

                    Node anc = n;
                    while(anc != secParent
                            && !anc.getLabel().equals("PRN"))
                        anc = anc.getParent();

                    if(!opt.relinkCyclicPRN || !anc.getLabel().equals("PRN")) {
                        logger.warning(1, "Secondary parent is ancestor. Skipping.");
                        n.unlinkSecParent(secParent);
                        continue;
                    } else {

                        if(done.contains(secParent)) {
                            logger.message(2, "*** DONE BEFORE: ***" + secParent);
                            continue;
                        }

                        logger.message(1, "Relinking cyclic parenthetical.");

                        anc.getParent().removeChild(anc);
                        tree.checkConsistency();

                        NonterminalNode newParent = secParent.getParent();
                        newParent.replaceChild(secParent, anc);
                        tree.checkConsistency();

                        ((NonterminalNode) anc).setLabel(n.getParent().getLabel());
                        anc.setFunction(n.getParent().getFunction());                        

                    }

                }

                if(secParent.getParent() != null
                        && secParent.getParent().getHeadChild() == secParent) {
                    logger.warning(1, "Cannot unlink head child....");
                    
                    if(!opt.deepSyntax || !secLabel.equals("*RNR*"))
                        n.unlinkSecParent(secParent);

                    continue;
                }

                n.getParent().addChild(secParent);
                tree.checkConsistency();

                NonterminalNode np = n.getParent();
                Node prev = secParent;
                while(np != null && np.getHead() == n) {
                    np.setHeadChild(prev);
                    prev = np;
                    np = np.getParent();
                }

                tree.checkConsistency();

                done.add(secParent);

                //n.unlinkSecChildren(); // why?
                
                n.unlinkSecParent(secParent); // changed 090312
                
            }
        }

        /* 2. Re-link gapped nodes using the GAP link. */
        for(Node n: tree.getNodes()) {

            if(n instanceof TerminalNode)
                continue;

            NonterminalNode parent = (NonterminalNode) n;

            /* 1. Remove gap links that point to empty nodes
                  or to siblings (siblings are due to annotation errors). */
            for(Node c: parent)
                if(c.getSecLabel() != null && c.getSecLabel().equals("=")
                        && (c.getSecParent().isEmpty()
                                || c.getSecParent().getParent() == parent))
                    c.unlinkSecParent();

            /* Check whether any gap links are present. */
            boolean gap = false;
            for(Node c: parent)
                if(c.getSecLabel() != null && c.getSecLabel().equals("=")) {
                    gap = true;
                    break;
                }

            if(!gap)
                continue;

            if(parent.getParent().getHeadChild() == parent) {
                logger.warning(1, "Gap container is head. Skipping this edge.");
                continue;
            }

            parent.setHeadChild(null);

            /* 2. When there are non-gapped constituents here too,
                  they are linked to the previous gapped constituent,
                  or the first of the following.
             */
            /* 3. Re-link gapped nodes. */

            if(!opt.ddtGapping) {

                ArrayList<Node> toRelink = new ArrayList<Node>();
                for(Node c: parent)
                    if(c.getSecLabel() == null || !c.getSecLabel().equals("="))
                        toRelink.add(c);
                for(Node c: toRelink) {
                    NonterminalNode h = null;
                    boolean sawC = false;
                    for(Node c2: parent)
                        if(c2.getSecLabel() != null && c2.getSecLabel().equals("=")) {
                            h = (NonterminalNode) c2;
                            if(sawC)
                                break;
                        } else if(c2 == c) {
                            if(h != null)
                                break;
                            sawC = true;
                        }
                    h.addChild(c);
                }

                toRelink = new ArrayList<Node>(parent.getChildren());
                for(Node c: toRelink) {
                    ((NonterminalNode) c.getSecParent()).addChild(c);
                    c.setFunction("GAP");
                    c.unlinkSecParent();
                }
            } else {
                NonterminalNode p2 = parent;
                Node secp = null;
                for(Node c: p2)
                    if(c.getSecParent() != null) {
                        secp = c.getSecParent();
                        break;
                    }
                while(!p2.isSameOrAncestorOf(secp))
                    p2 = p2.getParent();

                /* Look for an intervening conjunction. */
                boolean sawSecp = false;
                NonterminalNode gapParent = null;
                Node conj = null;
                for(Node c: p2) {
                    if(c.isSameOrAncestorOf(secp))
                        sawSecp = true;
                    else if(c.isSameOrAncestorOf(parent))
                        break;
                    else if(sawSecp && c.getLabel().matches("CC|CONJP")) {
                        conj = c;
                        break;
                    }
                }

                /* If a single conjunction was found, build a CONJP.
                   This is what we will attach the leftovers to. 
                 */
                if(conj != null) {
                    if(conj instanceof TerminalNode) {
                        int index = p2.getChildren().indexOf(conj);
                        p2.bracket(index, index + 1, "CONJP"); // TODO inte denna tagg!!!
                        NonterminalNode cp = (NonterminalNode) p2.getChild(index);
                        cp.setHeadChild(conj);
                        gapParent = cp;
                    } else
                        gapParent = (NonterminalNode) conj;
                    int index1 = p2.getChildren().indexOf(gapParent);
                    ArrayList<Node> puncs = new ArrayList();
                    for(int i = index1 + 1; i < p2.getChildren().size(); i++) {
                        Node n3 = p2.getChild(i);
                        if(n3.isSameOrAncestorOf(parent))
                            break;
                        if(n3 instanceof TerminalNode
                                && ((TerminalNode) n3).isPunctuation())
                            puncs.add(n3);
                    }
                    for(Node p: puncs)
                        gapParent.addChild(p);
                }


                /* If no conjunction was found, use the common ancestor. */
                if(gapParent == null)
                    gapParent = p2;

                ArrayList<Node> toRelink = new ArrayList<Node>();

                for(Node c: parent)
                    //if(c.getSecLabel() == null || !c.getSecLabel().equals("="))
                    toRelink.add(c);
                for(Node c: toRelink) {
                    if(c.getSecLabel() != null && c.getSecLabel().equals("="))
                        c.setFunction("GAPTMP");
                    gapParent.addChild(c);
                }

            }

            if(parent.size() > 0)
                throw new RuntimeException("Child list should be empty!");

            parent.getParent().removeChild(parent);
        }
    }

    
    private static void addSecondaryLinks(PhraseStructureTree tree, Options opt) {
        
        /* 
             Interesting links: *T*, *, *RNR*        
        */
        
        HashMap<EmptyNode, Node> referents = new HashMap();        
        for(TerminalNode n: tree.getTokens()) 
            if(n instanceof EmptyNode && n.getSecParent() != null) {
                Node sp = n.getSecParent();
                while(sp != null && sp.getHead() instanceof EmptyNode) {                    
                    if(sp.getSecParent() != null)
                        sp = sp.getSecParent();
                    else if(sp.getHead().getSecParent() != null)
                        sp = sp.getHead().getSecParent();
                    else
                        sp = null;                    
                }
                referents.put((EmptyNode) n, sp);
            }
        
        for(TerminalNode n: tree.getTokens()) 
            if(n instanceof EmptyNode) {
                EmptyNode en = (EmptyNode) n;

                logger.message(2, "Empty node: " + en);
                
                if(en.getLabel().matches("\\*T\\*|\\*RNR\\*")) {
                    /* TODO check trace chains */

                    String l = en.getLabel();
                    
                    /* 
                       Uses of *T*:

                       Fronted elements (TPC)
                       wh-movement
                       parentheticals

                     */

                    Node ref = referents.get(en);
                    if(ref == null)
                        continue;

                    Node p = en.getParent();
                    while(p != null && p.getHead() instanceof EmptyNode)
                        p = p.getParent();

                    if(p == null) {
                        logger.message(2, "No concrete parent");
                        //en.unlinkSecParent();
                        continue;
                    } else
                        logger.message(2, "Trace, parent " + p);
                    
                    ref.addSecChild(p, "T1LINK-" + l + "-" + en.getParent().getFunction());
                    //en.unlinkSecParent();
                } else if(en.getLabel().matches("\\*ICH\\*|\\*EXP\\*|\\*PPA\\*")) {
                    //en.unlinkSecParent();
                    //throw new RuntimeException("Unknown trace:" + en);
                    if(en.getSecParent() != null)
                        en.unlinkSecParent();
                } else if(en.getLabel().equals("*") || en.getLabel().equals("*XXXCTRL*")) {
                    
                    /*
                        Uses of *:
                        (with link)
                        - Passive trace
                        
                        - Raising/control: continue to rise, are thought to
                          Special interesting case: "was elected"
                        
                        "have to" (regularly?)

                        (without link)
                        Passive trace without auxiliary

                     */

                    Node ref = referents.get(en);
                    if(ref == null)
                        continue;

                    Node p = en.getParent();
                    while(p != null && p.getHead() instanceof EmptyNode)
                        p = p.getParent();

                    if(p == null) {
                        logger.message(2, "No concrete parent");
                        //en.unlinkSecParent();
                        continue;
                    } else
                        logger.message(2, "Trace, parent " + p);

                    boolean found = true;
                    while(found && p.getHead().getLabel().equals("TO")) {
                        found = false;
                        for(Node c: (NonterminalNode) p)
                            if(c.getLabel().equals("VP")) {
                                p = c;
                                found = true;
                                break;
                            }
                    }
                    
                    if(en.getLabel().equals("*XXXCTRL*"))
                        ref.addSecChild(p, "RCLINK-" + en.getParent().getFunction());
                    else
                        ref.addSecChild(p, "T2LINK-" + en.getParent().getFunction());

                } else if(en.getLabel().matches("0|\\*\\?\\*|\\*U\\*|\\*NOT\\*")){
                    // nothing
                    if(en.getSecParent() != null)
                        throw new RuntimeException("Shouldn't have a secondary parent");
                } else {
                    throw new RuntimeException("Unknown trace:" + en);
                }
                
            }
        
        logger.message(2, "After following trace links");
        logger.message(2, tree.tabbedOutput());
        
        if(true)
        for(Node n: tree.getNodes()) {
            if(n instanceof TerminalNode)
                continue;
            if(!n.getFunction().equals("VC"))
                continue;
            NonterminalNode nt = (NonterminalNode) n;

            //if(PennRules.isCoordinated(nt))
            //    continue;

            //logger.message(2, "XXXX: nt = " + nt);            
            
            NonterminalNode p = nt.getParent();

            while(p != null) {
                for(Node c: p)
                    if(c != p.getHeadChild()
                            && !c.isSameOrAncestorOf(nt)
                            && !c.getHead().isPunctuation()
                            && !c.getFunction().equals("COORD")
                            && !(c.getHead() instanceof EmptyNode)
                            && !c.getLabel().matches("CC|CONJP")) {
                        //c.addSecChild(nt, "VCLINK-" + c.getFunction());
                        c.getHead().addSecChild(nt.getHead(), "VCLINK-" + c.getFunction());
                    }
                if(p.getParent() == null 
                   || p.getFunction().equals("VC") 
                   || p.getParent().getHeadChild() == p)
                    p = p.getParent();
                else
                    break;
            }
        }

        logger.message(2, "After adding VC links");
        logger.message(2, tree.tabbedOutput());
        
        // TODO remove all remaining links here
        for(TerminalNode n: tree.getTokens()) 
            if(n instanceof EmptyNode) {
                EmptyNode en = (EmptyNode) n;
                for(Pair<Node, String> p: en.getSecParents())
                    en.unlinkSecParent(p.left);
                    //if(en.getSecParent() != null)
                   //en.unlinkSecParent();
            }

        logger.message(2, "After removing remaining links from traces");
        logger.message(2, tree.tabbedOutput());
        
        if(true)
        for(Node n: tree.getNodes()) {
            if(n instanceof TerminalNode)
                continue;
            if(n.getLabel().equals("FRAG"))
                continue;
            NonterminalNode nt = (NonterminalNode) n;
            if(!PennRules.isCoordinated(nt))
                continue;
            HashSet<Node> siblings = new HashSet();
            for(Node c: nt)
                if(c.getFunction().equals("COORD"))
                    siblings.add(c);

            TerminalNode head = nt.getHead();
            
            for(Pair<Node, String> p: nt.getSecParents())
                for(Node s: siblings) {
                    p.left.getHead().addSecChild(s.getHead(), "CLINK2-" + p.right);
                }
            /* Special case for control with 'to'. */
            if(opt.imAsHead && nt.getLabel().equals("VP") && head.getLabel().equals("TO")) {
                if(nt.getHeadChild() instanceof NonterminalNode) { // otherwise bug
                    NonterminalNode hc = (NonterminalNode) nt.getHeadChild();
                    Node c = hc.findChildByLabel("VP", true, false);
                    if(c != null)
                        for(Pair<Node, String> p: c.getSecParents())
                            for(Node s: siblings) {
                                if(s instanceof NonterminalNode) {
                                    Node sc = ((NonterminalNode) s).findChildByLabel("VP", true, false);
                                    if(sc != null)
                                        p.left.getHead().addSecChild(sc.getHead(), "CLINK3-" + p.right);
                                }
                            }
                }
            }
            
            int count = 0;
            while(nt != null && nt.getHead() == head) { 
                for(Node c: nt)
                    if(c != nt.getHeadChild()
                            && !c.getHead().isPunctuation()
                            && !(c.getHead() instanceof EmptyNode)
                            && !c.getFunction().equals("COORD")
                            && !c.getLabel().matches("CC|CONJP")
                            && !siblings.contains(c)) {
                        for(Node s: siblings) {
                            c.getHead().addSecChild(s.getHead(), "CLINK1-" + c.getFunction());
                        }
                        //c.addSecChild(s, "ZZZ-COORD-" + c.getFunction());
                    }
                nt = nt.getParent();
                count++;
                if(count == 2)
                    break;
            }
            
        }

        logger.message(2, "After adding coordination links");
        logger.message(2, tree.tabbedOutput());        
        
    }
    
    private static void finalEdgeLabels(Node n, Options options) { 
        if(n instanceof NonterminalNode) {
            NonterminalNode nn = (NonterminalNode) n;
            for(Node c: nn)
                finalEdgeLabels(c, options);
        }
        if(n.getParent() == null) {
            /* Root node. */
            TerminalNode tn = n.getHead();
            Node st = tn.getSubtree();
            if(st.getLabel().matches("ROOT|S0"))
                st = ((NonterminalNode) st).getHeadChild();
            if(st instanceof TerminalNode)
                n.setFunction("ROOT-FRAG");
            else {
                if(st.getLabel().equals("UCP")) {
                    /* Get the first non-punctuation node. */
                    for(Node nn2: (NonterminalNode) st)
                        if(nn2 instanceof NonterminalNode
                                || !((TerminalNode) nn2).isPunctuation()) {
                            st = nn2;
                            break;
                        }
                }
                if(st.getLabel().matches("S|SINV"))
                    n.setFunction("ROOT-S");
                else if(st.getLabel().equals("SBARQ"))
                    n.setFunction("ROOT-SBARQ");
                else if(st.getLabel().equals("SQ"))
                    n.setFunction("ROOT-SQ");
                else
                    n.setFunction("ROOT-FRAG");
            }
        } else if(n.getFunction() == null)
            n.setFunction(findFunction(n.getHead(), options));
    }

    private static String findCoordType(NonterminalNode node) {
        logger.message(2, "In findCoordType: node = " + node);
        if(node.getLabel().matches("S|SINV|SBARQ|SQ"))
            return "SCOORD";
        if(node.getLabel().matches("VP"))
            return "VCOORD";
        //if(node.getLabel().matches("NP|NX|NML"))
        //    return "NCOORD";
        if(node.getLabel().equals("UCP")) {
            for(Node c: node)
                if(c.getLabel().matches("S|SINV|SBARQ|SQ"))
                    return "SCOORD";
                else if(c.getLabel().matches("VP"))
                    return "VCOORD";
            //else if(c.getLabel().startsWith("N"))
            //    return "NCOORD";
        }

        return "COORD";
    }

    private static String findFunction(TerminalNode child,
            Options options) {

        Node cst = child.getSubtree();

        NonterminalNode parent = cst.getParent();

        /* 1. If cst has a function label, return that. */
        if(cst instanceof NonterminalNode) {
            NonterminalNode ncst = (NonterminalNode) cst;
            if(ncst.getFunction() != null)
                return ncst.getFunction();
        }

        /* 2. If cst is a conjunction, return CC. */
        if(cst.getLabel().matches("CC|CONJP"))
            return "CC";

        /* 3. If the child token is punctuation, return P. */
        if(child instanceof TokenNode
                && child.getLabel().matches("\\.|\\,|\\)|\\(|\\'\\'|\\`\\`|\\:"))
            return "P";

        if(parent == null)
            return "___DUMMY___";

        /* 4a. If we analyze coordinations with the conjunction as head,
               and the parent is a conjunction, return COORD. */
        if(parent.getHeadChild().getLabel().matches("CC|CONJP|\\,|\\:|\\;")) {
            if(options.labelCoords)
                return findCoordType(parent);
            else
                return "COORD";
        }

        /* 4b. If parent is a coordination, and cst
              is not the first term, return COORD. */
        if(PennRules.isCoordinated(parent)
                && parent.getChildren().indexOf(cst) > 0) {
            if(options.labelCoords)
                return findCoordType(parent);
            else
                return "COORD";
        }        

        /* 5. If cst is a VP under a VP, SQ or SINV, return VC. */
        if(parent.getLabel().matches("VP|SQ|SINV")
                && cst.getLabel().equals("VP")) {
            if(options.imAsHead && parent.getHead().getLabel().equals("TO"))
                return "IM";
            return "VC";
        }

        /* 5b. going to -> VC. */
        if(parent.getHead() instanceof TokenNode
                && ((TokenNode) parent.getHead()).getWord().toLowerCase().equals("going")
                && cst.getLabel().equals("S"))
            return "VC";

        /* 6b. is to be done etc */
        if(cst.getLabel().matches("S")
                && PennRules.isCopula(parent.getHead()))
            return "VC";

        /* 6. If parent is a VP etc, return VMOD. */
        if(parent.getLabel().matches("VP|S|SQ|SINV|SBAR")) {

            if(!options.subAsHead && cst.getLabel().equals("PREP_AUX"))
                if(options.oldVMOD)
                    return "VMOD";
                else
                    return "SUB";
            
            if(child.getLabel().matches("RP|RB.*"))
                return "ADV";

            if(PennRules.isCopula(parent.getHead())
                    && cst.getLabel().matches("ADJP|NP")
                    && child.getPosition() > parent.getHead().getPosition())
                return "PRD";

            if(!options.oldVMOD) {
                if(!options.imAsHead) {
                    if(child.getLabel().equals("TO"))
                        return "IM";
                } else {
                    // TODO
                }

                if(options.whAsHead
                   && parent.getHeadChild().getLabel().startsWith("W"))
                         return "SUB";
                
                if(!options.subAsHead) {
                    //if(cst.getLabel().equals("PREP_AUX"))
                    //    return "SUB";
                    // redundant
                } else {
                    if(parent.getHead().getLabel().matches("IN|RB|DT"))
                        return "SUB";
                }

                if(options.noPennTags)
                    return "VMOD";
                return "DEP";
            } else
                return "VMOD";
        }        

        /* 7. If parent is an NP etc, return NMOD. */
        if(parent.getLabel().matches("NP|NAC|NX|WHNP|NML")) {
            if(options.suffix
                    && child.getLabel().equals("POS"))
                return "SUFFIX";
            if(options.name
                    && parent.getHead().getLabel().startsWith("NNP")
                    && child.getLabel().startsWith("NNP")
                    && child.getSubtree() == child)
                return "NAME";
            return "NMOD";
        }        
        
        /* 8. If ppt is an AP etc, return AMOD. */
        // 070705: removed QP and CONJP
        if(parent.getLabel().matches("ADJP|ADVP|WHADJP|WHADVP|JJP"))
            return "AMOD";

        /* 9. If ppt is a PP etc, return PMOD. */
        if(parent.getLabel().matches("PP|WHPP"))
            return "PMOD";

        if(options.qmod && parent.getLabel().matches("QP"))
            return "QMOD";
        
        /* 10. Else return DEP. */
        return "DEP";
    }

    /* Dependency post-processing. */

    private static void removeAdvFuncs(DepGraph depgraph) {
        for(DepNode n: depgraph.nodes)
            for(int i = 0; i < n.relations.length; i++) {
                if(n.relations[i].equals("TMP") && n.parents[0].pos.startsWith("NN"))
                    n.relations[i] = "NMOD";
                else {
                    n.relations[i] = n.relations[i].replaceAll("(BNF|CLR|DIR|DTV|EXT|LOC|MNR|TMP|PRP|PUT|VOC)", "");
                    n.relations[i] = n.relations[i].replaceAll("\\-+", "-");
                    n.relations[i] = n.relations[i].replaceAll("^\\-|\\-$", "");
                    if(n.relations[i].equals(""))
                        n.relations[i] = "ADV";
                }
            }
    }
    
    private static void noPennTags(DepGraph depgraph) {
        for(DepNode n: depgraph.nodes)
            for(int i = 0; i < n.relations.length; i++) {
                if(n.relations[i].matches("TMP|LOC") && n.parents[0].pos.startsWith("NN"))
                    n.relations[i] = "NMOD";
                else {
                    n.relations[i] = n.relations[i].replaceAll("(ADV|BNF|CLR|DIR|DTV|EXT|LOC|MNR|TMP|PRP|PUT|VOC|OBJ|SBJ|LGS|PRD|OPRD)", "");
                    n.relations[i] = n.relations[i].replaceAll("\\-+", "-");
                    n.relations[i] = n.relations[i].replaceAll("^\\-|\\-$", "");
                    if(n.relations[i].equals(""))
                        n.relations[i] = "VMOD";
                }
            }
    }

    private static void removeRootLabels(DepGraph depgraph) {
        for(DepNode n: depgraph.nodes)
            for(int i = 0; i < n.relations.length; i++)
                if(n.relations[i].startsWith("ROOT"))
                    n.relations[i] = "ROOT";
    }


    private static void toMelchukCoords(DepGraph g) {
        for(DepNode n: g.nodes)
            for(DepNode c: n.children)
                if(c.relations[0].endsWith("COORD")) {
                    relinkCoord(n);
                    break;
                }
        for(DepNode n: g.nodes) {
            for(DepNode c: n.children) {
                if(c.word.matches("[\\!\\?\\-\\_\\/\\&\\+\\.\\,\\`\\'\\(\\)\\[\\]\\{\\}\\\"\\:\\;]+"))
                    continue;
                if(!c.relations[0].equals("CC"))
                    break;

                // TODO relation for introducing rhetorical and, or
                c.relations[0] = "DEP";
            }
        }

        for(DepNode n: g.nodes) {
            for(DepNode c: n.children)
                if(c.word.toLowerCase().matches("either|neither")
                        && c.relations[0].equals("CC"))
                    c.relations[0] = "DEP";
                else if(c.relations[0].equals("CC"))
                    c.relations[0] = "COORD";
            if(n.relations.length > 0 && n.relations[0].equals("___DUMMY___-GAP"))
                n.relations[0] = "CONJ";
        }
    }

    private static void relinkCoord(DepNode coordTop) {

        ArrayList<DepNode> coords = new ArrayList<DepNode>();
        coords.add(coordTop);

        String coordType = null;
        for(DepNode c: coordTop.children)
            if(c.position > coordTop.position && c.relations[0].endsWith("COORD")) {
                coords.add(c);
                if(coordType == null)
                    coordType = c.relations[0];
                else
                    if(!coordType.equals(c.relations[0]))
                        logger.warning(1, "Coordination types: " + coordType + " or " + c.relations[0] + "?");
            }

        for(int i = 1; i < coords.size(); i++) {
            DepNode c1 = coords.get(i-1);
            DepNode c2 = coords.get(i);

            DepNode clink = null;
            for(DepNode c: coordTop.children) {
                if(c.position > c1.position
                        && c.position < c2.position
                        && c.relations[0].equals("CC"))
                    clink = c;
            }

            if(c1 != coordTop) {
                if(clink != null) {
                    coordTop.children = remove(coordTop.children, clink);
                    c1.children = addFirst(c1.children, clink);
                    clink.relations[0] = coordType;
                    clink.parents[0] = c1;
                    relinkPunctuation(coordTop, c1, clink);
                    coordTop.children = remove(coordTop.children, c2);
                    clink.children = addFirst(clink.children, c2);
                    c2.relations[0] = "CONJ";
                    c2.parents[0] = clink;
                    relinkPunctuation(coordTop, clink, c2);                    
                } else {
                    coordTop.children = remove(coordTop.children, c2);
                    c1.children = addFirst(c1.children, c2);
                    c2.relations[0] = coordType;
                    c2.parents[0] = c1;
                    relinkPunctuation(coordTop, c1, c2);
                }
            } else {
                if(clink != null) {
                    clink.relations[0] = coordType;
                    coordTop.children = remove(coordTop.children, c2);
                    clink.children = addFirst(clink.children, c2);
                    c2.relations[0] = "CONJ";
                    c2.parents[0] = clink;
                    relinkPunctuation(coordTop, clink, c2);
                } else {
                    c2.relations[0] = coordType;
                }
            }
        }

    }    

    private static void relinkPunctuation(DepNode coordTop, DepNode n1, DepNode n2) {
        if(n1 == coordTop)
            return;
        ArrayList<DepNode> ps = new ArrayList<DepNode>();
        for(DepNode c: coordTop.children)
            if(c.position > n1.position
                    && c.position < n2.position
                    && c.relations[0].equals("P"))
                ps.add(c);
        for(DepNode p: ps) {
            coordTop.children = remove(coordTop.children, p);                        
            n1.children = addFirst(n1.children, p);
            p.parents[0] = n1;
        }        
    }

    private static DepNode[] remove(DepNode[] ns, DepNode n) {
        ArrayList<DepNode> l = new ArrayList<DepNode>();
        for(DepNode d: ns)
            if(d != n)
                l.add(d);
        return (DepNode[]) l.toArray(new DepNode[0]);
    }

    private static DepNode[] addFirst(DepNode[] ns, DepNode n) {
        ArrayList<DepNode> l = new ArrayList<DepNode>();
        l.add(n);
        for(DepNode d: ns)
            l.add(d);
        return (DepNode[]) l.toArray(new DepNode[0]);
    }

}
