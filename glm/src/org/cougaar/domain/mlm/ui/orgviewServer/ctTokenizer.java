/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.orgviewServer;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Java CT tokenizer from jmapReader.java   9/1/99
 * Somewhat stripped down for AlpMap usage
 */

public class ctTokenizer {

  Vector v;
  StreamTokenizer st;

  static Object EOF = new Object();
  static Object LP = new Object();
  static Object RP = new Object();

  public  ctTokenizer(String line) {
  Object ob;
  Number n;
  v = new Vector(12, 10);

    unitinit(new StringReader(line));
    while ((ob=unit()) != EOF) {
      v.addElement(ob);
      }
    }


  private  void unitinit(StringReader rdr) {
    st = new StreamTokenizer(rdr); 
    st.commentChar('%');
    st.slashSlashComments(true);
    st.slashStarComments(true);
    st.ordinaryChar('/');  // disable default special handling
    }

  public Object unit() {
  Object p,r;
  Vector l;

    p=next(); 
    if (p==EOF) return EOF;
    if (p==LP) {
      l=new Vector(2, 4);
      while (true) {
        r=unit();
        if (r==RP) return l;
//      if (r==EOF) return EOF;
        if (r==EOF) return l;
        l.addElement(r);
        }
      }
    return p;
    }

  public Object next() {
  int i=0;
  char[] c;

    try {
      i=st.nextToken(); 
      }
    catch (IOException e) {
      System.err.print(e.toString()+" in toktest\n");
      }
    if (i==st.TT_EOF || i==0) return EOF;
//    if (i==st.TT_WORD) return new Symbol(st.sval, 1);
    if (i==st.TT_WORD) return st.sval;
    if (i=='\'' || i=='\"') return st.sval;
//    if (i==st.TT_NUMBER) return new Double(st.nval);
//    if (i==st.TT_NUMBER) return Double.toString(st.nval);
    if (i==st.TT_NUMBER) return 
        Integer.toString(new Double(st.nval).intValue());  // Yuk!!
    if (i=='(' || i=='[' || i=='{')  return LP;
    if (i==')' || i==']' || i=='}')  return RP;
    c= new char[1];  c[0]=(char)i;
//    return new Symbol(new String(c), 2);
    return new String(c);
    }



  }  //  end of  class 

