/*
 Pair.java
 
 Copyright (C) 2005, Richard Johansson (richard@cs.lth.se).
 
 Created in 2003 by Richard Johansson.
 
 $Log: Pair.java,v $
 Revision 1.1  2009/01/16 10:05:11  johansson
 Added to brenta repository.

 Revision 1.5  2007/08/17 10:20:47  richard
 Changed toString.

 Revision 1.4  2006/11/06 08:15:04  richard
 Added clone and comparators.

 Revision 1.3  2006/09/23 11:51:16  richard
 Equals and hashCode work when left or right is null.

 Revision 1.2  2006/09/07 17:51:19  richard
 New revision.

 Revision 1.1  2006/05/16 13:38:13  richard
 Re-added because of CVS problems.
 
 Revision 1.2  2006/04/07 14:46:08  richard
 Added documentation.
 
 Revision 1.1  2005/11/28 15:41:00  richard
 Added the file.
 
 
 */

package se.lth.cs.nlp.nlputils.core;

import java.util.Comparator;

/**
 * A pair consisting of a left and a right element.
 * 
 * @author Richard Johansson (richard@cs.lth.se)
 */
public class Pair<T1, T2> implements java.io.Serializable {
    
    private static final long serialVersionUID = 0;
    
    /**
     * The left element.
     */
    public T1 left;
    
    /**
     * The right element.
     */
    public T2 right;
    
    /**
     * Constructs a Pair of the two given arguments.
     */
    public Pair(T1 left, T2 right) {
        this.left = left;
        this.right = right;
    }
    
    public String toString() {
        StringBuilder out = new StringBuilder("(");
        if(left == this)
            out.append("<this>");
        else
            out.append(left);
        out.append(", ");
        if(right == this)
            out.append("<this>");
        else
            out.append(right);
        out.append(")");
        return out.toString();
    }
    
    public boolean equals(Object o) {
        if(!(o instanceof Pair))
            return false;
        Pair p = (Pair) o;

        boolean b;
        if(left == null)
        	b = p.left == null;
        else
        	b = p.left != null && left.equals(p.left);
        if(!b)
        	return false;

        if(right == null)
        	return p.right == null;
        else
        	return p.right != null && right.equals(p.right);
    }
    
    public int hashCode() {
    	int h1 = left == null? 0: left.hashCode();
    	int h2 = right == null? 0: right.hashCode();
    	return 31*h1 + h2;
    }
 
    public Pair<T1, T2> clone() {
        return new Pair<T1, T2>(left, right);
    }
    
    public static final Comparator<Pair> BY_LEFT = new Comparator<Pair>() {
        public int compare(Pair p1, Pair p2) {
            return ((Comparable) p1.left).compareTo(p2.left);
        }
    };
    
    public static final Comparator<Pair> BY_RIGHT = new Comparator<Pair>() {
        public int compare(Pair p1, Pair p2) {
            return ((Comparable) p1.right).compareTo(p2.right);
        }
    };
    
    public static final Comparator<Pair> BY_LEFT_FIRST = new Comparator<Pair>() {
        public int compare(Pair p1, Pair p2) {
            int c1 = ((Comparable) p1.left).compareTo(p2.left);
            if(c1 == 0)
                return ((Comparable) p1.right).compareTo(p2.right);
            else
                return c1;
        }
    };
    
    public static final Comparator<Pair> BY_RIGHT_FIRST = new Comparator<Pair>() {
        public int compare(Pair p1, Pair p2) {
            int c1 = ((Comparable) p1.right).compareTo(p2.right);
            if(c1 == 0)
                return ((Comparable) p1.left).compareTo(p2.left);
            else
                return c1;
        }
    };

}

