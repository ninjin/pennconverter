/*
 CoNLLFormat.java
 
 Created on Jan 11, 2006 by Richard Johansson (richard@cs.lth.se).
 
 $Log: CoNLLFormat.java,v $
 Revision 1.1  2009/01/16 10:05:25  johansson
 Added to brenta repository.

 Revision 1.2  2008/01/11 07:52:35  richard
 Added printGraph.

 Revision 1.1  2007/12/18 15:13:56  richard
 Renamed.

 Revision 1.1  2007/03/08 13:04:34  richard
 Created new package.

 Revision 1.4  2006/09/07 17:51:19  richard
 New revision.

 Revision 1.3  2006/04/11 14:39:28  richard
 Added a dummy constructor.
 
 Revision 1.2  2006/04/11 14:33:41  richard
 Added documentation.
 
 Revision 1.1  2006/04/11 13:24:58  richard
 Added the file.
 
 Revision 1.9  2006/02/23 13:54:35  richard
 New revision.
 
 Revision 1.8  2006/01/17 15:46:32  richard
 Tab as separator.
 
 Revision 1.7  2006/01/17 09:44:28  richard
 Removed the debug printout.
 
 Revision 1.6  2006/01/17 09:41:11  richard
 Added a dummy root node.
 
 Revision 1.5  2006/01/13 11:51:15  richard
 Added some consistency checks.
 
 Revision 1.4  2006/01/11 15:08:09  richard
 Removed the main method.
 
 Revision 1.3  2006/01/11 15:07:45  richard
 Changed an input parameter.
 
 Revision 1.2  2006/01/11 14:19:00  richard
 Made the method public.
 
 Revision 1.1  2006/01/11 09:59:45  richard
 Added the file.
 
 
 */
package se.lth.cs.nlp.nlputils.depgraph;

import java.io.*;

import java.util.*;

/**
 * Factory class that reads a dependency graph encoded in the CoNLL-X
 * format.
 * 
 * @author Richard Johansson (richard@cs.lth.se)
 */
public class CoNLLFormat {
    
    private CoNLLFormat() {}
    
    static int lineCount = 0;
    
    /**
     * Reads a dependency graph from a reader.
     * 
     * @param r the reader.
     * @return the next dependency graph.
     */
    public static DepGraph readNextGraph(BufferedReader r) {
        
        try {
            DepGraph graph = new DepGraph();
            
            //ArrayList<Integer> roots = new ArrayList<Integer>();
            HashMap<String, DepNode> nodeMap = new HashMap<String, DepNode>();
            HashMap<String, ArrayList<DepNode>> childMap = new HashMap<String, ArrayList<DepNode>>();
            
            DepNode root = new DepNode();
            
            ArrayList<DepNode> nodes = new ArrayList<DepNode>();
            nodes.add(root);
            nodeMap.put("0", root);
            childMap.put("0", new ArrayList<DepNode>());
            
            String line = r.readLine();
            
            lineCount++;
            
            //System.out.println("line = |" + line + "|");
            
            if(line == null)
                return null;
            
            while(line != null && line.trim().length() > 0) {
                String[] tokens = line.split("\\t");
                
                if(tokens.length != 10 && tokens.length != 6 && tokens.length != 8) {
                    PrintWriter out = new PrintWriter(new OutputStreamWriter(System.err, "UTF-8"));
                    
                    out.println("Line:");
                    out.println("|" + line + "|");
                    for(int i = 0; i < tokens.length; i++)
                        out.println("|" + tokens[i] + "|");
                    
                    out.flush();
                    
                    throw new RuntimeException("Illegal data format.");
                }
                
                String id = tokens[0];
                
                DepNode node = nodeMap.get(id);
                if(node == null) {
                    node = new DepNode();
                    nodeMap.put(id, node);
                    childMap.put(id, new ArrayList<DepNode>());
                }
                
                nodes.add(node);
                
                node.word = tokens[1];
                node.lemma = tokens[2];
                node.pos = tokens[3];
                if(!tokens[4].equals("_"))
                    node.posFine = tokens[4];
                if(!tokens[5].equals("_"))
                    node.features = tokens[5];
                node.position = Integer.parseInt(id);
                
                String head;
                if(tokens.length > 6) {
                    head = tokens[6];				
                    node.relations = new String[1];
                    node.relations[0] = tokens[7];
                    
//                  if(tokens.)
//                  try {
//                  String phead = tokens[8];
//                  String prel = tokens[9];
//                  }catch(Exception e) {
//                  System.out.println(line);
//                  System.out.println(lineCount);
//                  System.exit(1);
//                  }
                    
                } else {
                    head = "0";
                    node.relations = new String[1];
                    node.relations[0] = "NONE";
                }
                
                DepNode parent = nodeMap.get(head);
                ArrayList<DepNode> children;
                if(parent == null) {
                    parent = new DepNode();
                    nodeMap.put(head, parent);
                    children = new ArrayList<DepNode>();
                    childMap.put(head, children);
                } else
                    children = childMap.get(head);
                node.parents = new DepNode[1];
                node.parents[0] = parent;
                children.add(node);
                
                line = r.readLine();
                
                lineCount++;
                //System.out.println("line = |" + line + "|");
            }
            
            for(Map.Entry<String, ArrayList<DepNode>> e: childMap.entrySet()) {
                //Map.Entry e = it.next();
                String id = e.getKey();
                ArrayList<DepNode> children = e.getValue();
                DepNode node = nodeMap.get(id);				
                node.children = (DepNode[]) children.toArray(new DepNode[0]);
            }
            
            graph.nodes = (DepNode[]) nodes.toArray(new DepNode[0]);
            
            return graph;
            
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
    
    public static void printGraph(PrintWriter pw, DepGraph depgraph) {

        for(int i = 1; i < depgraph.nodes.length; i++)
            if(depgraph.nodes[i].parents.length != 1)
                throw new IllegalArgumentException("Every node must have a single parent!");
        
        for(int i = 1; i < depgraph.nodes.length; i++) {
            pw.print(depgraph.nodes[i].position);
            pw.print("\t");
            pw.print(depgraph.nodes[i].word);
            pw.print("\t");

            /* Lemma. */
            if(depgraph.nodes[i].lemma == null)
                pw.print("_");
            else
                pw.print(depgraph.nodes[i].lemma);
            pw.print("\t");

            /* Coarse POS. */
            if(depgraph.nodes[i].pos == null)
                pw.print("_");
            else
                pw.print(depgraph.nodes[i].pos);
            pw.print("\t");

            /* Fine POS. */
            if(depgraph.nodes[i].posFine == null)
                pw.print("_");
            else
                pw.print(depgraph.nodes[i].posFine);
            pw.print("\t");

            /* Features. */
            if(depgraph.nodes[i].features == null)
                pw.print("_");
            else
                pw.print(depgraph.nodes[i].features);
            pw.print("\t");

            pw.print(depgraph.nodes[i].parents[0].position);
            pw.print("\t");
            pw.print(depgraph.nodes[i].relations[0]);

            /* "Projectivized." */
            pw.print("\t_\t_");

            pw.println();
        }
        pw.println();
        pw.flush();
    }
    
    /*	public static void main(String[] argv) {
     DepGraph g = readNextGraph(System.in);
     g.print();
     }*/
    
}
