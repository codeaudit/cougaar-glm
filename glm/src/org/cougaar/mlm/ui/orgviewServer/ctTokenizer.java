/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */
 
package org.cougaar.mlm.ui.orgviewServer;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Java CT tokenizer from jmapReader.java   9/1/99
 * Somewhat stripped down for Map usage
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

