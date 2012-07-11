/*
 DepGraph.java
 
 Created on Aug 19, 2005 by Richard Johansson.
 
 $Log: DepGraph.java,v $
 Revision 1.3  2010-06-07 11:02:07  johansson
 Major update.

 Revision 1.2  2009/04/16 12:06:04  johansson
 Error msg.

 Revision 1.1  2009/01/16 10:05:25  johansson
 Added to brenta repository.

 Revision 1.7  2008/08/14 08:13:36  richard
 Added collectChildren.

 Revision 1.6  2008/05/29 12:17:31  richard
 Added clone in DepGraph.

 Revision 1.5  2008/03/18 08:33:35  richard
 Constructors.

 Revision 1.4  2008/01/11 07:53:01  richard
 More lenient checkConsistency.

 Revision 1.3  2007/12/18 15:13:46  richard
 Generalized consistency check.

 Revision 1.2  2007/04/05 13:25:04  richard
 Prints lemma.

 Revision 1.1  2007/03/08 13:04:34  richard
 Created new package.

 Revision 1.9  2006/12/15 08:30:55  richard
 Added toString.

 Revision 1.8  2006/12/08 17:00:03  richard
 Added graphical output.

 Revision 1.7  2006/10/18 13:03:12  richard
 Added checkConsistency and isProjective.

 Revision 1.6  2006/10/13 06:52:56  richard
 Prints lemma.

 Revision 1.5  2006/10/11 13:41:11  richard
 Prints only non-null features and posFine.

 Revision 1.4  2006/09/07 17:51:19  richard
 New revision.

 Revision 1.3  2006/04/11 13:28:07  richard
 Added documentation.
 
 Revision 1.2  2006/04/11 13:25:35  richard
 New revision.
 
 Revision 1.2  2006/01/17 09:41:11  richard
 Added a dummy root node.
 
 Revision 1.1  2005/11/02 13:56:23  richard
 Added the file.
 
 
 */
package se.lth.cs.nlp.nlputils.depgraph;


//import gnu.trove.TObjectIntHashMap;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.*;

/**
 * A dependency graph consisting of an array of nodes.
 * 
 * The first node should be a dummy root node.
 * 
 * @author Richard Johansson (richard@cs.lth.se)
 */
public class DepGraph {
    
    /**
     * The array of nodes.
     */
    public DepNode[] nodes;

    public DepGraph() {
    }
    
    public DepGraph(int n) {
        nodes = new DepNode[n + 1];
    }
    
    public DepGraph(String[] tokens) {
    	this(tokens.length);
    	for(int i = 0; i < tokens.length; i++)
    		nodes[i+1] = new DepNode(i + 1, tokens[i], null, null);
    }

    public DepGraph clone() {
        DepGraph copy = new DepGraph(nodes.length - 1);
        for(int i = 0; i < nodes.length; i++) {
            copy.nodes[i] = nodes[i].shallowCopy();
        }
        for(int i = 0; i < nodes.length; i++) {
            // Can't use in 1.5.
            //copy.nodes[i].relations = Arrays.copyOf(nodes[i].relations, 
            //        nodes[i].relations.length);

            copy.nodes[i].relations = new String[nodes[i].relations.length];
            for(int j = 0; j < nodes[i].relations.length; j++)
                copy.nodes[i].relations[j] = nodes[i].relations[j];
            
            copy.nodes[i].parents = new DepNode[nodes[i].parents.length];
            for(int j = 0; j < nodes[i].parents.length; j++)
                copy.nodes[i].parents[j] = copy.nodes[nodes[i].parents[j].position];
            copy.nodes[i].children = new DepNode[nodes[i].children.length];
            for(int j = 0; j < nodes[i].children.length; j++)
                copy.nodes[i].children[j] = copy.nodes[nodes[i].children[j].position];            
        }
        return copy;
    }
    
    public boolean equals(Object other) {
    	if(other == this)
    		return true;
    	DepGraph dg2 = (DepGraph) other;
    	if(dg2.nodes.length != this.nodes.length)
    		return false;
    	for(int i = 0; i < nodes.length; i++) {
    		DepNode n1 = nodes[i];
    		DepNode n2 = dg2.nodes[i];
    		if(n1.parents.length != n2.parents.length) {
    			//System.out.println("1");
    			return false;
    		}
    		for(int j = 0; j < n1.parents.length; j++) {
    			if(n1.parents[j].position != n2.parents[j].position) {
        			//System.out.println("2: (" + i + ", " + j + ")");
    				return false;
    			}
    			if(!n1.relations[j].equals(n2.relations[j])) {
        			//System.out.println("3");
    				return false;
    			}
    		}
    	}
    	return true;
    }
    
    /**
     * Prints the graph to stdout.
     * @deprecated
     */
    public void print() {
        System.out.println(toString());
    }
    
    /**
     * Prints the graph to a writer.
     * 
     * @param out the writer.
     * @deprecated
     */
    public void print(java.io.PrintWriter out) {
        out.println(toString());
    }  

    public String toString() {
        StringBuilder sb = new StringBuilder();
        java.util.HashMap<DepNode, Integer> m = new java.util.HashMap<DepNode,Integer>();
        
        for(int i = 0; i < nodes.length; i++) {
            m.put(nodes[i], new Integer(i));
        }
        
        for(int i = 0; i < nodes.length; i++) {
        	DepNode n = nodes[i];        	
        	if(n != null) {
        		sb.append("" + (i) + ": " + n.word  
        				+ ", pos = " + n.pos
        				+ (n.lemma != null? ", lemma = " + n.lemma: "")
        				+ ", links = ");
                if(n.parents == null)
                    sb.append("(null)");
                else
                    for(int j = 0; j < n.relations.length; j++) {
                        sb.append("(" + m.get(n.parents[j])
                                + ", " + n.relations[j] + ") ");
                    }
        	} else
        		sb.append("" + i + ": <null>");
        	
            sb.append("\n");
        }
        return sb.toString();
    }
    
    public String toTokenString() {
    	StringBuilder sb = new StringBuilder();
    	for(int i = 1; i < nodes.length; i++) {
    		if(i > 1)
    			sb.append(" ");
    		if(nodes[i] == null)
    			sb.append("<null>");
    		else
    			sb.append(nodes[i].word);
    	}
    	return sb.toString();
    }
    
    public void checkConsistency() {
        /* Kolla att varje nod utom dummy-root har ett huvud. */
        
        /*
        for(int i = 1; i < this.nodes.length; i++)
            if(this.nodes[i].parents == null
               || this.nodes[i].parents.length == 0)
                throw new RuntimeException("Has no head!");
        
        for(int i = 1; i < this.nodes.length; i++)
            if(this.nodes[i].relations == null
               || this.nodes[i].relations.length == 0)
                throw new RuntimeException("Has no relation!");
                */
        
        /* Kolla att varje f�r�lder k�nner till alla sina barn. */
        for(int i = 1; i < this.nodes.length; i++) {
            if(nodes[i].parents.length != nodes[i].relations.length)
                throw new RuntimeException("Parents vector length not equal to relations vector length");
            for(DepNode p: nodes[i].parents) {
                if(p.children == null)
                    throw new RuntimeException("Parent has no children!");
                if(!Arrays.asList(p.children).contains(nodes[i])) {
                    System.out.println("node = " + nodes[i]);
                    System.out.println("parent = " + p);
                    System.out.println("children = " + Arrays.toString(p.children));               
                    throw new RuntimeException("Child is not among parent's children: p = " + p + ", c = " + nodes[i]);
                }
            }
        }
        
        for(int i = 1; i < this.nodes.length; i++) {
            if(nodes[i].children == null)
                throw new RuntimeException("Node has no children!");
            for(DepNode c: this.nodes[i].children)
                if(!Arrays.asList(c.parents).contains(nodes[i])) {
                    System.out.println("child = " + c);
                    System.out.println("parents = " + Arrays.asList(c.parents));
                    System.out.println("node = " + this.nodes[i]);                    
                    throw new RuntimeException("Child has wrong parent!");
                }
        }
        
        /* Kolla att det inte finns en rot. */
        /*
        boolean foundRoot = false;
        for(int i = 1; !foundRoot && i < nodes.length; i++) {
            for(DepNode p: nodes[i].parents)
                if(p.position == 0) {
                    foundRoot = true;
                    break;
                }
        }
        if(!foundRoot)  {
            throw new RuntimeException("No root!");
        }*/
    }

    public void collectChildren() {
        ArrayList[] children = new ArrayList[nodes.length];
        for(int i = 0; i < nodes.length; i++)
            children[i] = new ArrayList();
        for(int i = 0; i < nodes.length; i++)
            for(int j = 0; j < nodes[i].parents.length; j++)
                children[nodes[i].parents[j].position].add(nodes[i]);
        for(int i = 0; i < nodes.length; i++)
            nodes[i].children = (DepNode[]) children[i].toArray(new DepNode[0]);
    }
    
    public boolean isProjective() {
        for(int j = 1; j < nodes.length; j++) {
            if(nodes[j].parents.length > 1)
                return false;
            int i = nodes[j].parents[0].position;
            if(i < j) {
                for(int k = i + 1; k < j; k++)
                    if(!nodes[i].isSameOrAncestorOf(nodes[k]))
                        return false;
            } else {
                for(int k = j + 1; k < i; k++)
                    if(!nodes[i].isSameOrAncestorOf(nodes[k]))
                        return false;
            }
        }
        return true;
    }
    
    public void draw(Graphics2D graphics) {

        int[] middles = new int[this.nodes.length];
        int[] starts = new int[this.nodes.length];

        FontMetrics fm = graphics.getFontMetrics();
        int dh = fm.getMaxAscent();
        int start = 2*dh;
        
        for(DepNode n: this.nodes) {
            if(n.position == 0)
                continue;
            String w = n.word;
            int len = fm.charsWidth(w.toCharArray(), 0, w.length());
            starts[n.position] = start;
            middles[n.position] = start + len / 2;
            len += 2*dh;
            start += len;
        }

        int[] heights = new int[this.nodes.length];
        setHeights(this.nodes, heights, 0);

        //BasicStroke bs = new BasicStroke(2);
        //graphics.setStroke(bs);

        int maxheight = 0;
        for(DepNode n: this.nodes)
            if(n.position > 0 && heights[n.position] > maxheight)
                maxheight = heights[n.position]; 

        int y = 2*dh*(maxheight + 2);
            
        for(DepNode n: this.nodes) {
            if(n.position == 0)
                continue;

            graphics.drawString(n.word, starts[n.position], y);

            if(n.parents[0].position == 0)
                continue;
            
            int from = middles[n.position];
            int to = middles[n.parents[0].position];
        
            int y2 = y - dh;
            int d = from < to? 1: -1;
            
            int c1 = from + d*dh;
            int c2 = to - d*dh;
            int half = (from + to) / 2;
            int height = 2*dh*heights[n.position];
            
            GeneralPath gp = new GeneralPath();                        
            gp.moveTo(from, y2);
            gp.lineTo(c1 - d*dh/2, y2 - height + dh/2);
            gp.lineTo(c1, y2 - height);
            gp.lineTo(c2, y2 - height);
            gp.lineTo(c2 + d*dh/2, y2 - height + dh/2);
            gp.lineTo(to, y2);                        

            GeneralPath arr = new GeneralPath();
            arr.moveTo(from, y2);

            double ang = Math.atan2(-height - 0.5*dh, 0.5*d*dh);

            final double DANG = 0.3;
            final double ARR_LEN = 1.0*dh;
            int xx1 = from + (int) (ARR_LEN*Math.cos(ang + DANG));
            int yy1 = y2 + (int) (ARR_LEN*Math.sin(ang + DANG));
            int xx2 = from + (int) (ARR_LEN*Math.cos(ang - DANG));
            int yy2 = y2 + (int) (ARR_LEN*Math.sin(ang - DANG));
            
            arr.lineTo(xx1, yy1);
            arr.lineTo(xx2, yy2);
            arr.lineTo(from, y2);
            
            graphics.setColor(Color.BLACK);
            graphics.draw(gp);
            graphics.fill(arr);
            
            int len = fm.charsWidth(n.relations[0].toCharArray(), 0, n.relations[0].length());

            Rectangle rect = new Rectangle(half - len/2 - dh/2, y2 - height - dh/2, len + dh, dh + dh/4);

            graphics.setColor(Color.WHITE);
            graphics.fill(rect);
            graphics.setColor(Color.BLACK);
            graphics.draw(rect);
            
            graphics.drawString(n.relations[0], half - len/2, y2 - height + dh/2);
            
        }        
    }

    private void setHeights(DepNode[] nodes, int[] heights, int i) {
        if(nodes[i].children == null || nodes[i].children.length == 0)
            return;
        TreeSet<Integer> lefts = new TreeSet<Integer>();
        TreeSet<Integer> rights = new TreeSet<Integer>();
        for(DepNode n: nodes[i].children) {
            setHeights(nodes, heights, n.position);
            if(n.position < i)
                lefts.add(n.position);
            else
                rights.add(n.position);
        }
        while(!lefts.isEmpty()) {
            int j = lefts.last();
            lefts.remove(j);
            int max = 0;
            for(int k = j; k <= i; k++)
                if(heights[k] > max)
                    max = heights[k];
            heights[j] = max + 1;
        }
        while(!rights.isEmpty()) {
            int j = rights.first();
            rights.remove(j);
            int max = 0;
            for(int k = j; k >= i; k--)
                if(heights[k] > max)
                    max = heights[k];
            heights[j] = max + 1;
        }
    }
    
    public int getMaxBranching() {
    	return nodes[0].getMaxBranching();
    }

    public static void diff(DepGraph dg1, DepGraph dg2, ArrayList<DepNode> out1, ArrayList<DepNode> out2) {
    	TreeSet<DepNode> s1 = new TreeSet();
    	TreeSet<DepNode> s2 = new TreeSet();
    	if(dg1.nodes.length != dg2.nodes.length)
    		throw new IllegalArgumentException("sentences not equal");
    	for(int i = 1; i < dg1.nodes.length; i++) {
    		if(dg1.nodes[i].parents.length > 1)
        		throw new IllegalArgumentException("only single-parent tree supported");
    		if(dg2.nodes[i].parents.length > 1)
        		throw new IllegalArgumentException("only single-parent tree supported");
    		int h1 = dg1.nodes[i].parents[0].position;
    		int h2 = dg2.nodes[i].parents[0].position;
    		if(h1 != h2 
    		   || !dg1.nodes[i].relations[0].equals(dg2.nodes[i].relations[0])
    		   || !dg1.nodes[i].pos.equals(dg2.nodes[i].pos)) {
    			s1.add(dg1.nodes[h1]);
    			s1.add(dg1.nodes[h2]);
    			s1.add(dg1.nodes[i]);
    			s2.add(dg2.nodes[h1]);
    			s2.add(dg2.nodes[h2]);
    			s2.add(dg2.nodes[i]);
    		}    			
    	}
    	out1.addAll(s1);
    	out2.addAll(s2);
    }

    public static final Comparator<DepNode> PRECEDENCE_ORDER = new Comparator<DepNode>() {
        public int compare(DepNode d1, DepNode d2) {
            return Double.compare(d1.position, d2.position);
        }
    };

}
