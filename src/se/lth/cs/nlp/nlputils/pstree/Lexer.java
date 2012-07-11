/*
 Lexer.java
  
 Created on Aug 7, 2007 by Richard Johansson (richard@cs.lth.se).

 $Log: Lexer.java,v $
 Revision 1.1  2009/01/16 10:09:50  johansson
 Added to brenta repository.

 Revision 1.6  2008/06/02 12:24:34  richard
 Debug.

 Revision 1.5  2008/02/14 14:37:15  richard
 Brown comments.

 Revision 1.4  2007/09/13 14:50:16  richard
 Backwards-compatibility.

 Revision 1.3  2007/08/21 10:54:27  richard
 Debug info

 Revision 1.2  2007/08/20 15:04:20  richard
 Charniak bug.

 Revision 1.1  2007/08/07 12:38:56  richard
 Complete refactoring.

   
 */
package se.lth.cs.nlp.nlputils.pstree;

import java.util.StringTokenizer;
import java.io.*;

/**
 * @author Richard Johansson (richard@cs.lth.se)
 */
class Lexer {

    private String currentToken;
    private StringTokenizer tokenizer;
    private BufferedReader reader;    
    
    private int lineNbr;
    private int sentenceLineNbr;
    private String currentSentence;
    
    private boolean skipComments;
    private String rest;
    private String nextSentence;
    
    Lexer(BufferedReader reader, boolean skipComments) {
        this.reader = reader;
        this.skipComments = skipComments;
    }

    private String nextSentence() {
        StringBuilder sentence = new StringBuilder();
        int depth = 0;
        sentenceLineNbr = -1;
        while(true) {
            StringBuilder line;
            if(rest != null) {
                line = new StringBuilder(rest);
                sentenceLineNbr = lineNbr;
                rest = null;
            } else {
                try {
                    String l = reader.readLine();
                    if(l == null)
                        line = null;
                    else {
                        if(l.startsWith("*x"))
                            l = "";
                        l = l.trim();
                        line = new StringBuilder(l);
                        lineNbr++;
                        if(l.length() > 0 && sentenceLineNbr == -1)
                            sentenceLineNbr = lineNbr;
                    }
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
            }
            if(line == null) {
                if(depth > 0)
                    throw new RuntimeException("Stream ends before sentence: "
                            + sentence + ", which started on line " + sentenceLineNbr);
                return null;
            }

            for(int i = 0; i < line.length(); i++)
                if(line.charAt(i) == '(')
                    depth++;
                else if(line.charAt(i) == ')') {
                    depth--;
                    if(depth < 0) 
                        throw new RuntimeException("Negative depth, line = " + sentenceLineNbr);
                    if(depth == 0) {
                        sentence.append(line.substring(0, i+1));
                        rest = line.substring(i+1).trim();
                        if(rest.length() == 0)
                            rest = null;
                        
                        //System.err.println("Returns |" + sentence.toString().trim() + "|");
                        
                        return sentence.toString().trim();
                    }
                } else if(depth == 0 && !Character.isWhitespace(line.charAt(i))) {
                    if(skipComments)
                        line.setCharAt(i, ' ');
                    else
                        throw new RuntimeException("Illegal character: " + line.charAt(i) + ": line = " + line);
                }
            sentence.append(line);
            sentence.append('\n');
        }
    }
    
    boolean hasMoreSentences() {
        if(nextSentence != null)
            return true;
        nextSentence = nextSentence();
        return nextSentence != null;        
    }

    void advanceSentence() {
        if(nextSentence == null)
            nextSentence = nextSentence();
        if(nextSentence == null)
            throw new IllegalStateException("No more sentences!");
        
        nextSentence = nextSentence.replaceAll("\\(([^\\s\\(\\)]+)\\)", "_-LRB-_$1_-RRB-_");

        tokenizer = new StringTokenizer(nextSentence, "() \t\n\r", true);
        currentSentence = nextSentence;
        nextSentence = null;
        currentToken = nextToken();
    }
    
    String nextToken() {
        if(!tokenizer.hasMoreTokens())
            return null;
        String token = tokenizer.nextToken();
        while(Character.isWhitespace(token.charAt(0))) {
            if(tokenizer.hasMoreTokens())
                token = tokenizer.nextToken();
            else
                return null;
        }
        return token;
    }

    void eat(String token) {
        if(currentToken == null || !currentToken.equals(token)) {
            throw new RuntimeException("eat " + token + ", found " 
                    + currentToken);
        }
        currentToken = nextToken();
    }
    
    void advance() {        
        currentToken = nextToken();
    }
    
    String current() {
        return currentToken;
    }
    
    String getSentence() {
        return currentSentence;
    }

    int getSentenceLineNbr() {
        return sentenceLineNbr;
    }
    
    static final String TEST = " \n  \n  (abc \tdef ghi)  ( (abc\nabc)\na) ( ) \n \n \n  ";
    
    public static void main(String[] argv) {
        try {
            BufferedReader br = new BufferedReader(new StringReader(TEST));
            Lexer lexer = new Lexer(br, true);
            while(lexer.hasMoreSentences()) {
                lexer.advanceSentence();
                System.out.print(lexer.getSentenceLineNbr() + ": ");
                int count = 0;
                String token = lexer.nextToken();
                while(token != null) {
                    count++;
                    System.out.print("\"" + token + "\"");                    
                    token = lexer.nextToken();
                    if(token != null)
                        System.out.print(", ");
                    if(count == 3)
                        break;
                }
                System.out.println();
            }
            
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
}
