/*
 PhraseStructureTree.java
 
 Copyright (C) 2005, Richard Johansson (richard@cs.lth.se).
 
 Created in June, 2004 by Richard Johansson (richard@cs.lth.se).
 
 $Log: PhraseStructureTree.java,v $
 Revision 1.5  2011-06-09 08:39:58  johansson
 Removed LGPL preamble.

 Revision 1.4  2011-02-10 11:48:09  johansson
 Added retainRange.

 Revision 1.3  2010-06-07 11:01:12  johansson
 Major update.

 Revision 1.2  2009/03/20 11:49:01  johansson
 Secondary edges when converting to dependency graph.

 Revision 1.1  2009/01/16 10:09:50  johansson
 Added to brenta repository.

 Revision 1.20  2008/02/14 14:38:14  richard
 Added nNonEmptyTokens.

 Revision 1.19  2008/01/11 07:54:40  richard
 Added replaceToken and findCommonAncestor.

 Revision 1.18  2007/08/14 07:05:11  richard
 Set NO_RELATION in dependency trees if no function is available.

 Revision 1.17  2007/08/10 13:25:13  richard
 Message

 Revision 1.16  2007/08/07 12:38:56  richard
 Complete refactoring.

 Revision 1.15  2007/07/04 14:58:26  richard
 More consistency checks.

 Revision 1.14  2007/07/03 07:10:42  richard
 Removed redundant imports.

 Revision 1.13  2007/07/03 07:08:02  richard
 Removed lot of Penn- and English-specific functionality. Removed factory methods.

 Revision 1.12  2007/03/08 15:09:03  richard
 Uses new depgraph package.

 Revision 1.11  2007/03/01 09:25:04  richard
 VC under SINV etc. AUX.

 Revision 1.10  2006/12/15 08:29:47  richard
 Object predicatives.

 Revision 1.9  2006/12/14 15:29:36  richard
 Logs messages.

 Revision 1.8  2006/12/12 08:32:43  richard
 Comments.

 Revision 1.7  2006/12/08 17:00:53  richard
 Created new package. Implemented new rules.

 Revision 1.6  2006/11/16 21:18:03  richard
 New revision.

 Revision 1.5  2006/10/18 13:05:43  richard
 New dependency rules.

 Revision 1.4  2006/10/17 12:15:37  richard
 Head and dependency rules.

 Revision 1.3  2006/10/17 10:45:24  richard
 Handles empty nodes and secondary edges.

 Revision 1.2  2006/09/25 11:34:51  richard
 Replaces bracket tags for POSs.

 Revision 1.1  2006/09/07 17:55:16  richard
 Added.

 Revision 1.5  2006/05/11 12:25:05  richard
 Dependency tree export.
 
 Revision 1.4  2006/04/12 11:24:05  richard
 Added documentation.
 
 Revision 1.3  2006/04/05 12:12:58  richard
 Removed some deprecated code.
 
 Revision 1.2  2005/11/30 09:34:48  richard
 Made some attributes public.
 
 Revision 1.1  2005/11/28 15:41:00  richard
 Added the file.
 
 */
package se.lth.cs.nlp.nlputils.pstree;

import java.util.*;

import se.lth.cs.nlp.nlputils.depgraph.*;
import se.lth.cs.nlp.nlputils.core.*;

/**
 * Phrase structure tree consisting of nonterminal nodes inside of
 * the tree, and terminal nodes (token nodes) at the leaves.
 * 
 * The class contains three factory methods: collinsTree, pennTree, and
 * pennTreeWithLemmas.
 * 
 * @author Richard Johansson (richard@cs.lth.se)
 */
public class PhraseStructureTree {

    private static MessageLogger logger;
    public static void setLogger(MessageLogger logger) {
        PhraseStructureTree.logger = logger;
    }
    
    /**
     * The top node.
     */
    private Node topnode;
    
    private List<TerminalNode> tokens;
    private List<Node> nodes;
    
    public Node getTopNode() {
        return topnode;
    }

    public PhraseStructureTree clone() {
    	Node ctop = topnode.clone();
    	PhraseStructureTree out = new PhraseStructureTree(ctop);
    	// TODO nodes in clone have no reference to the tree
    	return out;
    }
    
    public List<TerminalNode> getTokens() {
        if(tokens != null)
            return tokens;
        tokens = new ArrayList<TerminalNode>();
        collectTokens(topnode, tokens);
        Collections.sort(tokens);
        tokens = Collections.unmodifiableList(tokens);
        return tokens;
    }

    private static void collectTokens(Node node, Collection<TerminalNode> tokens) {
        if(node instanceof TerminalNode) {
            TerminalNode tn = (TerminalNode) node;
            tokens.add(tn);
        } else {
            NonterminalNode n = (NonterminalNode) node;
            for(Node child: n)
                collectTokens(child, tokens);
        }
    }

    public List<Node> getNodes() {
        if(nodes != null)
            return nodes;
        nodes = new ArrayList<Node>();
	collectNodes(topnode, nodes);
	nodes = Collections.unmodifiableList(nodes);
        return nodes;
    }

    void invalidateCache() {
        this.tokens = null;
        this.nodes = null;
    }
    
    /* Preorder */
    private static void collectNodes(Node n, Collection<Node> l) {
        if(n instanceof TerminalNode)
            l.add(n);
        else if(n instanceof NonterminalNode) {
            l.add(n);
            for(Node child: (NonterminalNode) n)
                collectNodes(child, l);
        }
    }
    
    PhraseStructureTree(Node topnode) {
        this.topnode = topnode;
    }
    
    
//    /**
//     * Creates a parse tree from a string containing a tree in Collins' format,
//     * that is a phrase structure tree with heads.
//     * 
//     * @param line the string.
//     * @return the constructed tree.
//     */
//    public static PhraseStructureTree collinsTree(String line) {        
//        line = line.replaceAll("\\s+", " ");
//        PhraseStructureTree out = new PhraseStructureTree();
//        out.topnode = new CollinsTreeParser(line).parseNode();        
//        if(out.topnode == null)
//            return null;        
//
//        collectTokens(out.topnode, out.tokens);        
//
//        collectHeads(out.tokens, out.heads);
//        out.findNodes();        
//        for(int i = 0; i < out.tokens.size(); i++) {
//            TokenNode n = (TokenNode) out.tokens.get(i);
//            n.position = i;
//        }        
//
//        for(int i = 0; i < out.nodes.size(); i++) {
//            if(out.nodes.get(i) instanceof NonterminalNode) {
//                NonterminalNode nt = (NonterminalNode) out.nodes.get(i);
//                for(Node child: nt.children) {
//                    if(child.parent != nt)
//                        throw new RuntimeException("Inconsistent tree!");
//                }
//            }
//            NonterminalNode parent = out.nodes.get(i).parent;
//            if(parent != null && !parent.children.contains(out.nodes.get(i)))
//                throw new RuntimeException("Inconsistent tree!");
//        }
//        
//        return out;
//    }
    
    public void findHeads(HeadFinder headFinder) {
        if(headFinder != null) {
            topnode.setHeads(headFinder);
            //heads = new ArrayList<Node>();
            //collectHeads(tokens, heads);
        }
    }
    
    public String toString() {
        return "ParseTree: " + topnode;
    }
    
//    static void collectHeads(Collection<TerminalNode> tokens, Collection<Node> heads) {
//        for(TerminalNode t: tokens) {
//            NonterminalNode n = t.getParent();
//            if(n.getHead() != t) {
//                heads.add(t);
//                t.setSubtree(t);
//            } else {
//                while(n.hasParent() && n.getParent().getHead() == t)
//                    n = n.getParent();
//                heads.add(n);
//                t.setSubtree(n);
//            }
//        }
//    }
    
    /**
     * Returns the number of punctuation tokens that the node <code>n</code> ends
     * with.
     *
     * @param n the node.
     * @return the number of punctuation tokens.
     */
    public int countEndPunc(Node n) {
        Pair<Integer, Integer> span = n.span();
        int np = 0;
        for(int i = span.right - 1; i >= span.left; i--) {
            TokenNode tn = (TokenNode) getTokens().get(i);
            if(tn.isPunctuation())
                np++;
            else
                break;
        }
        return np;
    }
        
    /**
     * Returns an indented string representing this tree. 
     * 
     * @return the indented string.
     */
    public String tabbedOutput() {
        StringBuffer buf = new StringBuffer();
        HashSet<Node> refd = new HashSet<Node>();
        for(Node n: getNodes())
            for(Pair<Node, ?> p: n.getSecParents())
                refd.add(p.left);
        
        HashMap<Node, Integer> refnbrs = new HashMap<Node, Integer>();
        for(Node n: getNodes())
            if(refd.contains(n))
                refnbrs.put(n, refnbrs.size() + 1);
        
        tabbedOutput(topnode, 0, buf, new int[] {0}, refnbrs);
        
        return buf.toString();
    }
    
    private void tabbedOutput(Node n, int indent, StringBuffer buf,
            int[] row, HashMap<Node, Integer> refnbrs) {
        if(n instanceof TerminalNode) {
            buf.append("" + row[0]);
            row[0]++;
        }

        for(int i = 0; i < indent; i++)
            buf.append("   ");
        if(n instanceof TerminalNode) {
            buf.append(n);
            if(n.getFunction() != null)
                buf.append("[" + n.getFunction() + "]");
            
            if(refnbrs.containsKey(n))
                buf.append(" (" + refnbrs.get(n) + ")");

            for(Pair<Node, String> p: n.getSecParents())
                buf.append(" <" + p.right + " (" + refnbrs.get(p.left) + ")>");
                
            //if(n.getSecParent() != null) {
                //    buf.append(" <" + n.getSecLabel() + " (" + refnbrs.get(n.getSecParent()) + ")>");
            //}
            
            buf.append('\n');
        } else {
            NonterminalNode nn = (NonterminalNode) n;
            buf.append("(" + nn.getLabel());
            if(nn.getFunction() != null)
                buf.append("[" + nn.getFunction() + "]");
            buf.append("/ " + nn.span() + " [" + nn.getHead() + "]");

            if(refnbrs.containsKey(n))
                buf.append(" (" + refnbrs.get(n) + ")");

            for(Pair<Node, String> p: n.getSecParents())
                buf.append(" <" + p.right + " (" + refnbrs.get(p.left) + ")>");
            
            //if(n.getSecParent() != null) {
            //    buf.append(" <" + n.getSecLabel() + " (" + refnbrs.get(n.getSecParent()) + ")>");
            //}
            
            buf.append('\n');
            indent++;
            for(Node child: nn)
                tabbedOutput(child, indent, buf, row, refnbrs);
        }
    }
    
    private TerminalNode findDepHead(TerminalNode tn) {
        TerminalNode parent = tn.depParent();
        while(parent != null && parent.isEmpty())
            parent = parent.depParent();
        return parent;
    }

    static final String NO_RELATION = "__NONE__";
    
    public DepGraph toDepGraph() {
    	return toDepGraph(false);
    }
    
    /**
     * @return the dependency graph.
     */
    public DepGraph toDepGraph(boolean keepEmpty) {

        DepGraph g = new DepGraph();        

        ArrayList<DepNode> nodes = new ArrayList<DepNode>();
        DepNode root = new DepNode();
        nodes.add(root);
        
        HashMap<TerminalNode, DepNode> m = new HashMap();

        logger.message(2, "tokens = " + tokens);
        
        /* 1. Add all non-empty tokens. */
        int n = 1;
        for(TerminalNode ten: getTokens())
            if(ten instanceof TokenNode) {
                TokenNode tn = (TokenNode) ten; 
                DepNode node = new DepNode();
                node.word = tn.getWord();
                node.pos = tn.getLabel();
                node.position = n;
                m.put(tn, node);
                nodes.add(node);
                n++;
            } else if(keepEmpty && !ten.getLabel().equals("*XXXCTRL*")) {
            	EmptyNode en = (EmptyNode) ten;
                DepNode node = new DepNode();
                node.word = en.getLabel();
                node.pos = "-NONE-";
                node.position = n;
                m.put(en, node);
                nodes.add(node);
                n++;                
            }

        int i = 0;
        for(TerminalNode tn: getTokens()) {

        	//if(!keepEmpty && tn instanceof EmptyNode)
            //    continue;
            
            DepNode node = m.get(tn); 
            if(node == null)
            	continue;            
            i++;
            if(node.position != i)
            	throw new RuntimeException("!!!");
            
            TerminalNode parent = findDepHead(tn);

            if(tn == parent)
                throw new RuntimeException("Parent is node!");
            
            /* The parent phrase is headed by an empty node. */
            if(parent != null && parent instanceof EmptyNode) {
                logger.message(2, node);
                logger.message(2, parent);

                throw new RuntimeException("Parent is empty!");
            }                

            node.parents = new DepNode[1];
            node.relations = new String[1];

            node.relations[0] = tn.getSubtree().getFunction();
            if(node.relations[0] == null)
                node.relations[0] = NO_RELATION;
            
            if(parent == null)
                node.parents[0] = root;
            else
                node.parents[0] = m.get(parent);
        }

        for(Node node: this.nodes) 
            for(Pair<Node, String> p: node.getSecParents()) {
                //if(node.getSecParent() != null) {
                DepNode sec1 = m.get(node.getHead());
                if(sec1 == null) {
                    break;
                    //throw new RuntimeException("sec1 == null: node = " + node);
                }
                DepNode sec2 = m.get(p.left.getHead());
                if(sec2 == null)
                    throw new RuntimeException("sec2 == null");
                
                int l = sec2.parents.length;
                sec2.parents = CollectionUtils.resize(sec2.parents, l + 1);
                sec2.relations = CollectionUtils.resize(sec2.relations, l + 1);
                sec2.parents[l] = sec1;
                sec2.relations[l] = p.right;
            }
        
        ArrayList<DepNode>[] childLists = new ArrayList[n];
        for(i = 0; i < n; i++) {
            childLists[i] = new ArrayList<DepNode>();
        }
        for(i = 1; i < n; i++) {
            DepNode node = nodes.get(i);
            for(DepNode p: node.parents)
                childLists[p.position].add(node);
        }
        
        for(i = 0; i < n; i++)
            nodes.get(i).children = childLists[i].toArray(new DepNode[0]);
        
        g.nodes = nodes.toArray(new DepNode[0]);
        return g;
    }

    public String getWordString() {
        StringBuilder sb = new StringBuilder();
        for(TerminalNode ten: getTokens())
            if(ten instanceof TokenNode)
                sb.append(" " + ((TokenNode) ten).getWord());
        if(sb.length() == 0)
            return "";
        return sb.substring(1);
    }

    public void checkConsistency() {

        ArrayList<Node> test = new ArrayList<Node>();
        collectNodes(topnode, test);
        HashSet s1 = new HashSet(test);
        HashSet s2 = new HashSet(getNodes());
        if(!s1.equals(s2))
            throw new RuntimeException("Node list is inconsistent!");
        
        for(Node n: getNodes()) {
            if(n == getTopNode()) {
                if(n.getParent() != null)
                    throw new RuntimeException("Top node's parent should be null!");
            } else {
                if(n.getParent() == null) {
                    logger.message(2, "n = " + n);
                    throw new RuntimeException("Node has no parent!");
                }
            }
        }
        
        for(Node n: getNodes())
            if(n.tree != this)
                throw new RuntimeException("The tree should be this: " + n);
                
        if(!getNodes().contains(topnode))
            throw new RuntimeException("Node list doesn't contain top node!");
        for(Node n: getNodes()) {
            NonterminalNode p = n.getParent();
            if(p != null)
                if(!p.getChildren().contains(n))
                    throw new RuntimeException("Parent's child list does not contain node!");
            if(p != null && !getNodes().contains(p))
                throw new RuntimeException("Node list doesn't contain parent!");
            
            if(n instanceof NonterminalNode) {
                NonterminalNode nt = (NonterminalNode) n;
                for(Node c: nt) {
                    if(c.getParent() != nt)
                        throw new RuntimeException("Child's parent isn't node!");
                    if(!getNodes().contains(c))
                        throw new RuntimeException("Node list doesn't contain child!");
                }
                TerminalNode head = nt.getHead();
                if(head == null)
                    throw new RuntimeException("Head is null");
                if(!getNodes().contains(head))
                    throw new RuntimeException("Node list doesn't contain head!");
                if(!getNodes().contains(head))
                    throw new RuntimeException("Token list doesn't contain head!");
                if(head.getSubtree() == null)
                    throw new RuntimeException("Subtree is null for " + head + ", nt = " + nt);
                if(!head.getSubtree().isSameOrAncestorOf(nt)) {
                    logger.message(2, "node = " + nt);
                    logger.message(2, "head = " + head);
                    logger.message(2, "subtree = " + head.getSubtree());
                    throw new RuntimeException("Head's subtree does not contain node!");
                }
            } else {
                TerminalNode tn = (TerminalNode) n;
                if(!getTokens().contains(tn))
                    throw new RuntimeException("Token list doesn't contain token!");
                Node st = tn.getSubtree();
                if(st == null) {
                    logger.message(2, "node = " + tn);
                    throw new RuntimeException("Subtree is null for this node: " + tn);
                }
                if(st.getHead() != tn) {
                    logger.message(2, "node = " + tn);
                    logger.message(2, "subtree = " + st);
                    logger.message(2, "subtree.head = " + st.getHead());
                    throw new RuntimeException("Node's subtree is not headed by node!");
                }                
            }
        }
    }
    
    public void replaceToken(TerminalNode tn, TerminalNode[] repl) {
        if(tn.tree != this)
            throw new IllegalArgumentException("Token must be attached to this tree!");
        for(int i = 0; i < repl.length; i++) {
            if(repl[i].tree != null)
                throw new IllegalArgumentException("Replacement token must not be attached!");
            if(repl[i].getParent() != null)
                throw new IllegalArgumentException("Replacement token must not be attached!");
        }

        NonterminalNode p = tn.getParent();
        int pos = p.indexOfChild(tn);
        p.removeChild(pos);
        
        for(int i = repl.length - 1; i >= 0; i--)
            p.addChild(pos, repl[i]);
    }
    
    public Node findCommonAncestor(Node n1, Node n2) {
        if(n1.tree != this || n2.tree != this)
            throw new IllegalArgumentException("Nodes must belong to this tree.");
        HashSet<Node> ns = new HashSet();
        while(n1 != null) {
            ns.add(n1);
            n1 = n1.getParent();
        }
        while(n2 != null) {
            if(ns.contains(n2))
                return n2;
            n2 = n2.getParent();
        }
        return null;
    }
    
    public int nNonEmptyTokens() {
        int count = 0;
        for(TerminalNode t: tokens)
            if(t instanceof TokenNode)
                count++;
        return count;
    }
    
    public String toStrippedBracketing() {
    	StringBuilder sb = new StringBuilder();
    	topnode.toStrippedBracketing(sb);
    	String out = sb.toString();
    	if(out.contains("\n"))
    		throw new RuntimeException("!!!");
    	return out;
    }
    
    public String tokenString() {
    	StringBuilder sb = new StringBuilder();
    	boolean first = true;
    	for(TerminalNode t: this.getTokens()) {
    		if(t instanceof EmptyNode)
    			continue;
    		TokenNode tn = (TokenNode) t;
    		String w = TokenNode.escapeBracket(tn.getWord());
    		if(!first)
    			sb.append(" ");
    		else
    			first = false;
    		sb.append(w);
    	}
    	return sb.toString();
    }
    
    //public void recomputePositions() {
    //    recomputePositions(getTopNode(), new int[1]);
    //}
        
//    private void recomputePositions(Node n, int[] current) {
//        if(n instanceof TerminalNode)
//            ((TerminalNode) n).setPosition(current[0]++);
//        else
//            for(Node c: (NonterminalNode) n)
//                recomputePositions(c, current);
//    }
 
    /* end is exclusive */
    public ArrayList<Node> getHighestCovering(int start, int end) {
    	ArrayList<Node> s = new ArrayList();
    	List<TerminalNode> l = getTokens(); 
    	for(int i = 0; i < l.size(); i++) {
    		TerminalNode tn = l.get(i);
    		if(tn.getPosition() >= start && tn.getPosition() < end) {
    			Node prev = null;
    			Node n = tn;
    			Pair<Integer, Integer> psp = null;
    			Pair<Integer, Integer> sp = n.span();
    			while(sp.left >= start && sp.right <= end) {
    				prev = n;
    				psp = sp;
    				n = n.getParent();
    				if(n == null)
    					break;
    				sp = n.span();
    			}
    			s.add(prev);
    			i = psp.right - 1;
    		}
    	}
    	return s;
    }
    
    public static Node lowestCommonAncestor(Collection<Node> nodes) {
    	if(nodes.size() == 0)
    		throw new IllegalArgumentException("no nodes");
    	if(nodes.size() == 1)
    		return nodes.iterator().next();    		
    	
    	HashMap<Node, Integer> ancMap = new HashMap();
    	ArrayList<Node> ancList = new ArrayList();
    	int dist = 0;
    	
    	Iterator<Node> it = nodes.iterator();
    	Node n1 = it.next();
    	while(n1 != null) {
    		ancMap.put(n1, dist);
    		ancList.add(n1);
    		n1 = n1.getParent();
    		dist++;
    	}
    	
    	int maxDist = 0;    	
    	while(it.hasNext()) {
    		Node n = it.next();    		
    		Integer d = ancMap.get(n);
    		while(d == null) {
    			n = n.getParent();
    			d = ancMap.get(n);
    		}
    		if(maxDist < d)
    			maxDist = d;    		
    	}
    	return ancList.get(maxDist);    	
    }
    
    public void retainRange(int start, int end) {
    	// Assumes we have set the heads
    	
    	/* We remove a node if
           1) it is a terminal outside the range
    	   2) it is a nonterminal dominating only tokens outside the range
		   3) it strictly dominates the node that dominates all nodes in the highest covering
    	 */

    	/* 1. Find the node dominating all nodes in the highest covering. */
    	ArrayList<Node> cover = getHighestCovering(start, end);    	
    	Node domNode = null;
    	
    	try {
    		domNode = lowestCommonAncestor(cover);
    	} catch(Exception e) {
    		e.printStackTrace();
    		System.err.println(tabbedOutput());
    		System.err.println("start = " + start + ", end = " + end);
    		System.exit(1);
    	}

    	//System.out.println("cover = " + cover);
    	//System.out.println("domNode = " + domNode);    
    	
    	/* 2. Remove all nodes outside the span. */
    	HashSet<Node> removed = new HashSet();    	
    	ArrayList<Node> allNodes = new ArrayList(getNodes());    	
    	for(Node nn: allNodes) {
    		//System.out.println("Checking: nn=" + nn);
    		
    		if(removed.contains(nn)) {
    			//System.out.println("Already removed, skipping");
    			continue;
    		}
    		Pair<Integer, Integer> sp = nn.span();
    		if(sp.right <= start || sp.left >= end) {
    			collectNodes(nn, removed);
    			NonterminalNode p = nn.getParent();

    			// hmmm...
    			/*if(p.getHeadChild() == nn) {
    				p.setHeadChild(null);
    			}*/
    			if(p.getHeadChild() != nn)
    				p.removeChild(nn);
    		}
    	}

    	if(topnode.getHead() != domNode.getHead()) {
    		NonterminalNode st = (NonterminalNode) domNode.getHead().getSubtree();
    		st.getParent().removeChild(st);    		
    		NonterminalNode t = (NonterminalNode) topnode;
    		t.addChild(st);
    		t.setHeadChild(st);    		
    		for(Node c: new ArrayList<Node>(t.getChildren()))
    			if(c != st)
    				t.removeChild(c);
    	}
    }
    
}
