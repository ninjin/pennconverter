/*
 ImportMalt.java
 
 Created on Aug 19, 2005 by Richard Johansson.
 
 $Log: ImportMalt.java,v $
 Revision 1.1  2009/01/16 10:05:25  johansson
 Added to brenta repository.

 Revision 1.1  2007/03/08 13:04:34  richard
 Created new package.

 Revision 1.6  2006/09/07 17:51:19  richard
 New revision.

 Revision 1.5  2006/04/11 14:39:39  richard
 Added a dummy constructor.
 
 Revision 1.4  2006/04/11 14:38:14  richard
 Fixed typo in the documentation.
 
 Revision 1.3  2006/04/11 14:33:06  richard
 Uses SAX instead of DOM. Added documentation.
 
 Revision 1.2  2006/04/11 13:24:35  richard
 New revision.
 
 Revision 1.3  2006/01/17 09:41:11  richard
 Added a dummy root node.
 
 Revision 1.2  2006/01/11 14:18:40  richard
 Made the method public.
 
 Revision 1.1  2005/11/02 13:56:23  richard
 Added the file.
 
 
 */
package se.lth.cs.nlp.nlputils.depgraph;

import java.util.*;
import java.io.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import se.lth.cs.nlp.nlputils.core.Procedure;

/**
 * Factory class that reads a dependency graph encoded in the MALT-XML
 * format.
 *
 * @author Richard Johansson (richard@cs.lth.se)
 */
public class ImportMalt {
    
    private ImportMalt() {}
    
    private static final class MaltCallback extends DefaultHandler 
    implements ContentHandler {
        
        Procedure<DepGraph> callback;
        
        HashMap<String, DepNode> nodeMap;
        HashMap<String, ArrayList<DepNode>> childMap;
        
        ArrayList<DepNode> nodes;
        
        MaltCallback(Procedure<DepGraph> callback) {
            this.callback = callback;
        }
        
        public void startElement(String namespace, String localname,  
                String type, Attributes attributes) {
            if(type.equals("sentence")) {
                
                nodeMap = new HashMap<String, DepNode>();
                childMap = new HashMap<String, ArrayList<DepNode>>();
                
                DepNode root = new DepNode();
                nodes = new ArrayList<DepNode>();
                
                nodeMap.put("0", root);
                childMap.put("0", new ArrayList<DepNode>());
                
                nodes.add(root);
                
            } else if(type.equals("word")) {
                String id = attributes.getValue("id");
                String form = attributes.getValue("form");
                String postag = attributes.getValue("postag");
                String head = attributes.getValue("head");
                String deprel = attributes.getValue("deprel");
                
                DepNode node = nodeMap.get(id);
                if(node == null) {
                    node = new DepNode();
                    nodeMap.put(id, node);
                    childMap.put(id, new ArrayList<DepNode>());
                }
                
                node.word = form;
                node.pos = postag;
                node.position = nodes.size();
                
                nodes.add(node);
                
                node.relations = new String[1];
                node.relations[0] = deprel;
                
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
            }
        }
        
        public void endElement(String namespace, String localname,  
                String type) {
            
            if(type.equals("sentence")) {
                
                DepGraph graph = new DepGraph();
                
                for(Map.Entry<String, ArrayList<DepNode>> e: childMap.entrySet()) {
                    String id = e.getKey();
                    ArrayList<DepNode> children = e.getValue();
                    DepNode node = nodeMap.get(id);				
                    node.children = (DepNode[]) children.toArray(new DepNode[0]);
                }
                
                graph.nodes = (DepNode[]) nodes.toArray(new DepNode[0]);
                
                callback.apply(graph);
                
                nodeMap = null;
                childMap = null;
                nodes = null;
            }
        }
        
        public void characters(char[] ch, int start, int len) {
            /* Not needed. */
        }
        
    }
    
    /**
     * Returns a list of dependency graphs given an XML string.
     * WARNING: XML shouldn't be put into a Java string -- preferably use 
     * importGraphsFromFile instead.
     * 
     * @param s the XML string.
     * @return the list of dependency graphs.
     * @throws SAXException if the XML is invalid.
     */
    public static List<DepGraph> importGraphsFromString(String s) throws SAXException {
        InputSource is = new InputSource(new StringReader(s));
        try {
            
            ArrayList<DepGraph> out = new ArrayList<DepGraph>();
            
            Procedure<DepGraph> pr = new Procedure<DepGraph>(out) {
                public void apply(DepGraph g) {
                    ((ArrayList) in).add(g);
                }
            };
            
            handleGraphs(is, pr);
            
            return out;
            
        } catch(IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Got IOException.");
        }
    }
    
    /**
     * Returns a list of dependency graphs read from a file.
     * 
     * @param fn the file name.
     * @return the list of dependency graphs.
     * @throws SAXException if the XML is invalid.
     * @throws IOException if there was an IO problem.
     */
    public static List<DepGraph> importGraphsFromFile(String fn) 
    throws SAXException, IOException {
        try {
            InputSource is = new InputSource(new FileInputStream(fn));
            
            ArrayList<DepGraph> out = new ArrayList<DepGraph>();
            
            Procedure<DepGraph> pr = new Procedure<DepGraph>(out) {
                public void apply(DepGraph g) {
                    ((ArrayList) in).add(g);
                }
            };
            
            handleGraphs(is, pr);
            
            return out;	
        } catch(IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Got IOException.");
        }
    }
    
    private static org.xml.sax.XMLReader makeXMLReader() { 
        javax.xml.parsers.SAXParserFactory saxParserFactory   =  
            javax.xml.parsers.SAXParserFactory.newInstance(); 
        try {
            javax.xml.parsers.SAXParser saxParser 
            = saxParserFactory.newSAXParser();
            org.xml.sax.XMLReader parser
            = saxParser.getXMLReader();
            return parser; 
        } catch(Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Could not instantiate parser!");
        }
    }
    
    /**
     * Reads dependency graphs from an InputSource and calls a callback for each
     * graph.
     * 
     * @param is the input source.
     * @param callback the callback.
     * @throws SAXException if the XML was invalid.
     * @throws IOException if there was an IO problem.
     */
    public static void handleGraphs(InputSource is, Procedure<DepGraph> callback)
    throws SAXException, IOException {
        org.xml.sax.XMLReader reader = makeXMLReader(); 
        reader.setContentHandler(new MaltCallback(callback));
        reader.parse(is);
    }
    
    /**
     * Reads dependency graphs from a file and calls a callback for each
     * graph.
     * 
     * @param fn the file.
     * @param callback the callback.
     * @throws SAXException if the XML was invalid.
     * @throws IOException if there was an IO problem.
     */
    public static void handleGraphsFromFile(String fn, 
            Procedure<DepGraph> callback) throws IOException, SAXException {
        handleGraphs(new InputSource(new FileInputStream(fn)), callback);
    }
    
    public static void main(String[] argv) {
        
        try {
            
            Procedure<DepGraph> pr = new Procedure<DepGraph>() {
                public void apply(DepGraph g) {
                    g.print();
                    System.out.println();
                }
            };
            
            handleGraphsFromFile(argv[0], pr);
            
//          List<DepGraph> l = importGraphFromFile(argv[0]);
//          
//          for(DepGraph g: l)
//          g.print();
            
        } catch(Exception e) {
            e.printStackTrace();
        }
        
    }
}
