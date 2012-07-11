/*
 CollectionUtils.java
 
 Created on Jan 11, 2006 by Richard Johansson (richard@cs.lth.se).
 
 $Log: CollectionUtils.java,v $
 Revision 1.7  2010-06-07 11:26:09  johansson
 Added randomIntArray.

 Revision 1.6  2010-06-07 11:12:40  johansson
 Major update.

 Revision 1.5  2009/11/16 11:37:26  johansson
 minIndex maxIndex transpose normalizeByAvg

 Revision 1.4  2009/04/21 11:38:11  johansson
 Added heapTopK.

 Revision 1.3  2009/03/04 11:54:38  johansson
 More methods.

 Revision 1.2  2009/02/23 11:45:34  johansson
 Added int versions of max and min. Changed iterator to check for null.

 Revision 1.1  2009/01/16 10:05:11  johansson
 Added to brenta repository.

 Revision 1.11  2008/08/14 08:12:38  richard
 Added toSet and getIndexInTriangle.

 Revision 1.10  2008/04/23 09:04:16  richard
 Added min and minIndex, maxIndex.

 Revision 1.9  2008/04/21 08:38:49  richard
 Added max.

 Revision 1.8  2008/03/19 15:52:19  richard
 Added inverseMap.

 Revision 1.7  2008/01/11 07:51:38  richard
 Added sum.

 Revision 1.6  2007/12/18 15:15:42  richard
 Changed enlarge to resize. Added indexOf.

 Revision 1.5  2007/10/23 12:55:27  richard
 Added iterator and listIterator.

 Revision 1.4  2007/08/17 10:19:12  richard
 toOrderMap, toMap.

 Revision 1.3  2007/05/11 14:57:31  richard
 Added swap, sum, enlarge, and contains.

 Revision 1.2  2006/09/07 17:51:19  richard
 New revision.

 Revision 1.1  2006/05/16 13:38:13  richard
 Re-added because of CVS problems.
 
 Revision 1.3  2006/04/07 14:46:08  richard
 Added documentation.
 
 Revision 1.2  2006/02/23 10:20:07  richard
 Implemented filter.
 
 Revision 1.1  2006/02/23 09:46:33  richard
 Added the file.
 
 
 */
package se.lth.cs.nlp.nlputils.core;

import java.util.*;
import java.lang.reflect.*;
//import gnu.trove.*;

/**
 * Some auxiliary methods for collections.
 * 
 * @author Richard Johansson (richard@cs.lth.se)
 */
public class CollectionUtils {
    
    private CollectionUtils() {}
    
    /**
     * Destructively removes the elements for which <code>p</code> returns 
     * false.
     * 
     * @param coll the collection to modify.
     * @param p the predicate to filter by.
     */

    /*public static <T> void doFilter(Collection<T> coll, Predicate<T> p) {
        for(Iterator<T> iter = coll.iterator(); iter.hasNext(); ) {
            T obj = iter.next();
            if(!p.apply(obj))
                iter.remove();
        }
	}*/
    
    /**
     * Returns a filtered copy of the collection <code>coll</code>.
     *
     * @param coll the collection to filter.
     * @param p the predicate to filter by.
     * @return a new collection.
     */

    /*public static <T> Collection<T> filter(Collection<T> coll, Predicate<T> p){
        Collection<T> c2 = newCollection(coll);
        for(T obj: coll)
            if(p.apply(obj))
                c2.add(obj);
        return c2;
	}*/
    
    private static <T> Collection<T> cloneCollection(Collection<T> coll) {
        try {
            Class cl = coll.getClass();
            Constructor con = cl.getConstructor(new Class[] { Collection.class });
            return (Collection<T>) con.newInstance(new Object[] { coll });
        } catch(Exception e) {			
            if(coll instanceof List)
                return new LinkedList<T>(coll);
            if(coll instanceof Set)
                return new HashSet<T>(coll);
            throw new RuntimeException("Cannot handle this collection");
        }
    }
    
    private static <T> Collection<T> newCollection(Collection<T> coll) {
        try {
            Class cl = coll.getClass();
            Constructor con = cl.getConstructor(new Class[0]);
            return (Collection<T>) con.newInstance(new Object[0]);
        } catch(Exception e) {			
            if(coll instanceof List)
                return new LinkedList<T>();
            if(coll instanceof Set)
                return new HashSet<T>();
            throw new RuntimeException("Cannot handle this collection");
        }
    }
    
    public static void swap(double[] x, int i, int j) {
        double tmp = x[i];
        x[i] = x[j];
        x[j] = tmp;
    }
    
    public static void swap(int[] x, int i, int j) {
        int tmp = x[i];
        x[i] = x[j];
        x[j] = tmp;
    }

    public static void swap(Object[] x, int i, int j) {
        Object tmp = x[i];
        x[i] = x[j];
        x[j] = tmp;
    }

    public static double sum(double[] x, int length) {
        double s = 0;
        for(int i = 0; i < length; i++)
            s += x[i];
        return s;
    }

    public static double sum(double[] x) {
        return sum(x, x.length);
    }
    
    public static int sum(int[] x, int length) {
        int s = 0;
        for(int i = 0; i < length; i++)
            s += x[i];
        return s;
    }

    public static double sum(int[] x) {
        return sum(x, x.length);
    }
    
    public static int max(int[] x, int length) {
        int m = Integer.MIN_VALUE;
        for(int i = 0; i < length; i++)
            if(x[i] > m)
                m = x[i];
        return m;
    }

    public static int max(int[] x) {
        return max(x, x.length);
    }
    
    public static int min(int[] x, int length) {
        int m = Integer.MAX_VALUE;
        for(int i = 0; i < length; i++)
            if(x[i] < m)
                m = x[i];
        return m;
    }

    public static int min(int[] x) {
        return min(x, x.length);
    }
    
    public static double max(double[] x, int length) {
        double m = Double.NEGATIVE_INFINITY;
        for(int i = 0; i < length; i++)
            if(x[i] > m)
                m = x[i];
        return m;
    }
    
    public static double min(double[] x, int length) {
        double m = Double.POSITIVE_INFINITY;
        for(int i = 0; i < length; i++)
            if(x[i] < m)
                m = x[i];
        return m;
    }

    public static int maxIndex(double[] x, int length) {
        double m = Double.NEGATIVE_INFINITY;
        int mi = -1;
        for(int i = 0; i < length; i++)
            if(x[i] > m) {
                m = x[i];
                mi = i;
            }
        return mi;
    }
    
	public static int maxIndex(double[] x) {
		return maxIndex(x, x.length);
	}	    

    
    public static int minIndex(double[] x, int length) {
        double m = Double.POSITIVE_INFINITY;
        int mi = -1;
        for(int i = 0; i < length; i++)
            if(x[i] < m) {
                m = x[i];
                mi = i;
            }
        return mi;
    }
    
    public static int maxIndex(int[] x, int length) {
        int m = Integer.MIN_VALUE;
        int mi = -1;
        for(int i = 0; i < length; i++)
            if(x[i] > m) {
                m = x[i];
                mi = i;
            }
        return mi;
    }
    
    public static int minIndex(int[] x, int length) {
    	int m = Integer.MAX_VALUE;
        int mi = -1;
        for(int i = 0; i < length; i++)
            if(x[i] < m) {
                m = x[i];
                mi = i;
            }
        return mi;
    }
    
    public static int[] enlarge(int[] x, int size) {
        int[] out = new int[size];
        System.arraycopy(x, 0, out, 0, x.length);
        return out;
    }
    
    public static double[] enlarge(double[] x, int size) {
        double[] out = new double[size];
        System.arraycopy(x, 0, out, 0, x.length);
        return out;
    }

    /**
     * @deprecated
     * @param <T>
     * @param x
     * @param size
     * @return
     */
    public static <T> T[] enlarge(T[] x, int size) {
        T[] out = null;
        try {
            out = (T[]) Array.newInstance(x.getClass().getComponentType(), size);
        } catch(Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        System.arraycopy(x, 0, out, 0, x.length);
        return out;
    }
    
    public static <T> T[] resize(T[] x, int size) {
        T[] out = null;
        try {
            out = (T[]) Array.newInstance(x.getClass().getComponentType(), size);
        } catch(Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        int n = Math.min(x.length, size);
        System.arraycopy(x, 0, out, 0, n);
        return out;        
    }

    public static <T> T[] subArray(T[] x, int start, int len) {
    	if(start + len > x.length)
    		throw new IllegalArgumentException("length + start > x.length");
    	T[] out = null;
        try {
            out = (T[]) Array.newInstance(x.getClass().getComponentType(), len);
        } catch(Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        System.arraycopy(x, start, out, 0, len);
        return out;        
    }

    
    public static <T> boolean contains(T[] ts, T t) {
        for(int i = 0; i < ts.length; i++)
            if(t.equals(ts[i]))
                return true;
        return false;
    }
 
    public static <T> boolean containsSome(HashSet<T> s1, Collection<T> other) {
    	for(T t: other)
    		if(s1.contains(t))
    			return true;
    	return false;
    }
    
    /*public static <T> TObjectIntHashMap<T> toOrderMap(T[] ts) {
        TObjectIntHashMap<T> out = new TObjectIntHashMap<T>(ts.length);
        for(int i = 0; i < ts.length; i++)
            out.put(ts[i], i + 1);
        return out;
	}*/
    
    public static <T> HashMap<T, T> toMap(T[][] ts) {
        HashMap<T, T> out = new HashMap<T, T>(ts.length);
        for(int i = 0; i < ts.length; i++)
            out.put(ts[i][0], ts[i][1]);
        return out;
    }

    public static <T> HashSet<T> toSet(T[] ts) {
        HashSet<T> out = new HashSet();
        for(T t: ts)
            out.add(t);
        return out;
    }
    
    private static class ArrayIterator<T> implements ListIterator<T> {
        private T[] arr;
        private int position;
        public ArrayIterator(T[] arr) {
            this.arr = arr;
            position = 0;
        }
        
        public boolean hasNext() {
            //return position < arr.length;
            return position < arr.length
                && arr[position] != null;
        }
        
        public boolean hasPrevious() {
            //return position > 0;
            return position > 0
                && arr[position-1] != null;
        }

        public T next() {
            return arr[position++];
        }

        public T previous() {
            return arr[--position];
        }

        public int nextIndex() {
            return position;
        }

        public int previousIndex() {
            return position - 1;
        }
        
        public void add(T t) {
            throw new UnsupportedOperationException("Unsupported!");
        }
        
        public void remove() {
            throw new UnsupportedOperationException("Unsupported!");
        }
        
        public void set(T t) {
            arr[position] = t;
        }
        
    }
    
    public static <T> ListIterator<T> listIterator(T[] arr) {
        return new ArrayIterator(arr);
    }
    
    public static <T> Iterator<T> iterator(T[] arr) {
        return new ArrayIterator(arr);
    }

    public static int indexOf(Object[] ts, Object t) {
        for(int i = 0; i < ts.length; i++)
            if(ts[i].equals(t))
                return i;
        return -1;
    }
    
    public static int indexOf(int[] is, int i) {
        for(int j = 0; j < is.length; j++)
            if(is[j] == i)
                return j;
        return -1;
    }
    
    /*public static <V> TObjectIntHashMap<V> inverseMap(V[] vs) {
        TObjectIntHashMap<V> out = new TObjectIntHashMap<V>(vs.length);
        for(int i = 0; i < vs.length; i++)
            out.put(vs[i], i+1);
        return out;
	}*/
    
    public static int getIndexInTriangle(int n, int i, int j) {
        if(n < 0)
            throw new IllegalArgumentException("Triangle width negative");
        if(i < 0 || i >= n)
            throw new IllegalArgumentException("i = " + i + ", n = " + n);
        if(j < i || j >= n)
            throw new IllegalArgumentException("j = " + j + ", n = " + n);
        return n*i - ((i*i + i)>>>1) + j;
    }
    
    public static <T extends Comparable<? super T>> void heapTopK(List<T> list, int k) {
    	PriorityQueue<T> q = new PriorityQueue(list);
    	int i = 0;
    	for(ListIterator<T> it = list.listIterator(); it.hasNext(); ) {
    		it.set(q.poll());
    		i++;
    		if(i == k)
    			return;
    	}
    }
    
    public static <T> void heapTopK(List<T> list, int k, Comparator<T> comp) {
    	PriorityQueue<T> q = new PriorityQueue(list.size(), comp);
    	q.addAll(list);
    	int i = 0;
    	for(ListIterator<T> it = list.listIterator(); it.hasNext(); ) {
    		it.set(q.poll());
    		i++;
    		if(i == k)
    			return;
    	}
    }
    
    public static double[][] transpose(double[][] A) {
    	int m = A.length;
    	if(m == 0)
    		return new double[0][0];
    	int n = A[0].length;
    	double[][] out = new double[n][m];
    	for(int i = 0; i < m; i++)
    		for(int j = 0; j < n; j++)
    			out[j][i] = A[i][j];
    	return out;
    }
    
    public static void normalizeToLogProbs(double[] x) {
    	double max = max(x, x.length);
    	double sumExp = 0;
    	for(int i = 0; i < x.length; i++) {
    		if(!Double.isNaN(x[i]))
    			sumExp += Math.exp(x[i] - max);
    	}
    	double logSumExp = max + Math.log(sumExp);
    	for(int i = 0; i < x.length; i++)
    		if(!Double.isNaN(x[i]))
    			x[i] -= logSumExp;
    }
    
    public static void normalize2(double[] x) {
    	double sum = 0;
    	for(int i = 0; i < x.length; i++)
    		if(!Double.isNaN(x[i]))
    			sum += x[i]*x[i];
    	if(sum == 0)
    		return;
    	double f = 1.0 / Math.sqrt(sum);
    	for(int i = 0; i < x.length; i++)
    		if(!Double.isNaN(x[i]))
    			x[i] *= f;
    }
    
    public static void normalizeByAvg(double[] x) {
    	double sum = 0;
    	int n = 0;
    	for(int i = 0; i < x.length; i++)
    		if(!Double.isNaN(x[i])) {
    			sum += x[i];
    			n++;
    		}
    	if(sum == 0)
    		return;
    	sum /= n;
    	for(int i = 0; i < x.length; i++)
    		if(!Double.isNaN(x[i]))
    			x[i] -= sum;
    }
    
    public static <T> void cvSplit(Collection<T> full, Collection<T> tr, 
    		Collection<T> te, int nfolds, int fold) {
    	if(!tr.isEmpty())
    		throw new IllegalStateException("tr is not empty");
    	if(!te.isEmpty())
    		throw new IllegalStateException("te is not empty");
    	if(fold < 0 || fold >= nfolds)
    		throw new IllegalArgumentException("illegal fold");
    	int foldSize = full.size() / nfolds;    	
    	int i = 0;
    	int currentFold = -1;
    	for(T t: full) {
    		if(i % foldSize == 0 && currentFold < nfolds - 1)
    			currentFold++;
    		if(currentFold == fold)
    			te.add(t);
    		else
    			tr.add(t);
    		i++;
    	}    	
    }
    


	public static int[] range(int from, int to) {
		if(to < from)
			throw new IllegalArgumentException("to must be larger than from");
		int[] out = new int[to - from];
		for(int i = 0; i < out.length; i++)
			out[i] = from + i;
		return out;
	}
	
	public static <T> int count(Collection<T> ts, T toCount) {
		int out = 0;
		for(T t: ts)
			if(t.equals(toCount))
				out++;
		return out;
	}
	
	public static <T> int count(T[] ts, T toCount) {
		int out = 0;
		for(T t: ts)
			if(t.equals(toCount))
				out++;
		return out;
	}
	
	public static int count(int[] is, int toCount) {
		int out = 0;
		for(int i: is)
			if(i == toCount)
				out++;
		return out;
	}

	public static int count(long[] ls, long toCount) {
		int out = 0;
		for(long l: ls)
			if(l == toCount)
				out++;
		return out;
	}
	
	public static double[] toDoubleArray(Collection<Double> ds) {
		double[] out = new double[ds.size()];
		int index = 0;
		for(Double d: ds)
			out[index++] = d;
		return out;
	}
	
	public static int[] toIntArray(Collection<Integer> is) {
		int[] out = new int[is.size()];
		int index = 0;
		for(Integer i: is)
			out[index++] = i;
		return out;
	}
	
	public static long[] toLongArray(Collection<Long> is) {
		long[] out = new long[is.size()];
		int index = 0;
		for(Long i: is)
			out[index++] = i;
		return out;	
	}
	
	public static int[] randomIntArray(int len, Random rand) {
		int[] out = new int[len];
		for(int i = 0; i < len; i++) {
			out[i] = rand.nextInt();
			if(out[i] < 0)
				out[i] = -out[i];
		}
		return out;
	}
	
    public static void main(String[] argv) {	
    }

}

