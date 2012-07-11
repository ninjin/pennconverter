package se.lth.cs.nlp.nlputils.depgraph;

import java.io.BufferedReader;
import java.io.PrintWriter;

public class TabFormat {

    public static DepGraph readNextGraph(BufferedReader r) {
        throw new RuntimeException("Unimplemented");
    }

    public static void printGraph(PrintWriter pw, DepGraph depgraph) {
        for(int i = 1; i < depgraph.nodes.length; i++) {
            pw.print(i);
            pw.print("\t");
            
            pw.print(depgraph.nodes[i].word);
            pw.print("\t");

            if(depgraph.nodes[i].pos == null)
                pw.print("_");
            else
                pw.print(depgraph.nodes[i].pos);
            pw.print("\t");

            for(int j = 0; j < depgraph.nodes[i].parents.length; j++) {
                if(j > 0)
                    pw.print(" ");
                pw.print(depgraph.nodes[i].parents[j].position);
                pw.print("/");
                pw.print(depgraph.nodes[i].relations[j]);
            }

            pw.println();
        }
        pw.println();
        pw.flush();
        
    }
    
}
