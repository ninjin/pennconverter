package pennconverter;

import java.io.*;

import se.lth.cs.nlp.nlputils.pstree.*;

public class StripTrees {

	public static void main(String[] argv) {
        InputStream in = System.in;
        OutputStream out = System.out;

        if(argv.length == 0) {
        	System.err.println("No mode!");
        	System.exit(1);
        }
        
        int mode = 0;
        if(argv[0].equals("-trees"))
        	mode = 1;
        else if(argv[0].equals("-text"))
        	mode = 2;
        if(mode == 0) {
        	System.err.println("Unknown mode!");
        	System.exit(1);
        }
        	
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        PrintWriter pw = new PrintWriter(out);

        PennTreeParser tp = new PennTreeParser(reader); 

        while(tp.hasMoreTrees()) {
            PhraseStructureTree t = tp.parseTree();
            if(mode == 1)
            	pw.println(t.toStrippedBracketing());
            else if(mode == 2)
            	pw.println("<s> " + t.tokenString() + " </s>");
        }
        
        pw.close();
        
	}
	
}
