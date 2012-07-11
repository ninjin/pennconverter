package se.lth.cs.nlp.nlputils.depgraph;

import java.io.PrintWriter;
import java.util.*;
import java.util.regex.*;

public class TigerDepFormat {

	public static void printPreamble(PrintWriter pw, List<DepGraph> dg) {
		
	}

	public static void printEnd(PrintWriter pw) {
		pw.println("</body>");
		pw.println("</corpus>");
	}
	
	private static int sentenceCounter = 0; 
	
	public static void printGraph(PrintWriter pw, DepGraph dg) {		
		sentenceCounter++;
		String wpfx = "w" + sentenceCounter + "_";
		String npfx = "p" + sentenceCounter + "_";		
		int n = dg.nodes.length;		
		pw.println("<s id=\"s" + sentenceCounter + "\">");		
		pw.println("  <graph root=\"" + npfx + "0\">");		
		pw.println("    <terminals>");
		for(int i = 1; i < n; i++) {
			String w = escapeForXMLAttribute(dg.nodes[i].word);
			String p = escapeForXMLAttribute(dg.nodes[i].pos);
			String pf = dg.nodes[i].posFine == null? null: escapeForXMLAttribute(dg.nodes[i].posFine);
			String l = dg.nodes[i].lemma == null? null: escapeForXMLAttribute(dg.nodes[i].lemma);
			pw.print("        <t id=\"" + wpfx + i + "\" form=\"" + w);
			pw.print(" pos=\"" + p + "\"");
			if(pf != null)
				pw.print(" posFine=\"" + pf + "\"");
			if(l != null)
				pw.print(" lemma=\"" + l + "\"");
			if(dg.nodes[i].features != null)
				pw.print(" lemma=\"" + dg.nodes[i].features + "\"");
			pw.println("/>");
		}
		pw.println("    </terminals>");
		pw.println("    <nonterminals>");
		for(int i = 0; i < n; i++) {
			DepNode node = dg.nodes[i];
			String nn = node.pos == null? "ROOT": escapeForXMLAttribute(dg.nodes[i].pos);
			pw.println("        <nt id=\"" + npfx + i + "\" pos=\"" + nn + ">");
			pw.println("          <edge idref=\"" + wpfx + i + "\" label=\"--\"/>");
			for(DepNode c: node.children) {
				String l = escapeForXMLAttribute(c.relations[0]);
				pw.println("          <edge idref=\"" + npfx + c.position + "\" label=\"" + l + "\"/>");
			}
			pw.println("        </nt>");			
		}		
		pw.println("    </nonterminals>");
		pw.println("  </graph>");
		pw.println("</s>");		
	}

	private static Pattern ESC_PAT = Pattern.compile("[&\"<>]");
	private static String AMP_ESC = "&amp;";
	private static String QUO_ESC = "&quot;";
	private static String LT_ESC = "&lt;";
	private static String GT_ESC = "&gt;";
    
    public static String escapeForXMLAttribute(String s) {
    	StringBuffer sb = new StringBuffer();
    	Matcher m = ESC_PAT.matcher(s);
    	while(m.find()) {
    		char c = m.group().charAt(0);
    		switch(c) {
    		case '&':
    			m.appendReplacement(sb, AMP_ESC);
    			break;
    		case '"':
    			m.appendReplacement(sb, QUO_ESC);
    			break;
    		case '<':
    			m.appendReplacement(sb, LT_ESC);
    			break;
    		case '>':
    			m.appendReplacement(sb, GT_ESC);
    			break;
    		}
    	}
    	m.appendTail(sb);
    	return sb.toString();
    }
    
}
