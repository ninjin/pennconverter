/*
 MessageLogger.java
  
 Created on Dec 14, 2006 by Richard Johansson (richard@cs.lth.se).

 $Log: MessageLogger.java,v $
 Revision 1.1  2009/01/16 10:05:11  johansson
 Added to brenta repository.

 Revision 1.3  2008/02/14 14:36:43  richard
 Prefix.

 Revision 1.2  2008/01/11 07:52:11  richard
 Flushes.

 Revision 1.1  2006/12/15 08:31:19  richard
 Added.

   
 */
package se.lth.cs.nlp.nlputils.core;

import java.io.*;
import java.util.*;

/**
 * @author Richard Johansson (richard@cs.lth.se)
 */
public class MessageLogger {

    private PrintWriter pw;
    private int level;

    private int nMessages, nWarnings, nErrors;
    
    private HashMap<String, Object> properties = new HashMap<String, Object>();
    
    private String prefix = "";
    
    public MessageLogger(PrintWriter pw, int level) {
        this.pw = pw;
        this.level = level;
    }
    
    public MessageLogger(OutputStream os, int level) {
        if(os != null)
            this.pw = new PrintWriter(new OutputStreamWriter(os));
        this.level = level;
    }

    public MessageLogger() {        
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void message(int level, Object message) {
        if(pw != null && level <= this.level) {
            pw.println(prefix + message);
            pw.flush();
        }
        nMessages++;
    }

    public void warning(int level, Object message) {
        if(pw != null && level <= this.level) {
            pw.println(prefix + "WARNING: " + message);
            pw.flush();
        }
        nWarnings++;
    }
    
    public void error(int level, Object message) {
        if(pw != null && level <= this.level) {
            pw.println(prefix + "ERROR: " + message);
            pw.flush();
        }
        nErrors++;
    }
    
    public void setProperty(String key, Object property) {
        properties.put(key, property);
    }
    
    public void setPrefix(String prefix) {
        prefix = prefix.trim();
        if(!prefix.endsWith(":"))
            prefix += ": ";
        this.prefix = prefix;
    }
    
    public Object getProperty(String key) {
        return properties.get(key);
    }
    
    public int getNErrors() {
        return nErrors;
    }
    
    public int getNWarnings() {
        return nWarnings;
    }
    
    public void close() {
        if(pw != null)
            pw.close();
    }
    
}
