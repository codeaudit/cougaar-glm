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
 
package org.cougaar.mlm.ui.psp.transportation.data;

import java.util.*;

public class UITTNode 
    implements org.cougaar.core.util.SelfPrinter, java.io.Serializable {

    public UITTNode(String u) {
       url = u;
    }

    public String url;        // "source URL" : where the "return value" is obtained
    private Object value;      // "return value" : can be null, UItinerary, UIString
    public Vector children = new Vector();
    public UITTNode parent = null;

    public String getUrl() {return url;}
    public void setUrl(String s) {url = s;}

    public Object getValue() {return value;}
    public void setValue(Object o){value = o;}

    public Vector getChildren() {return children;}
    public void setChildren(Vector v){children = v;}

    public UITTNode getParent() {return parent;}
    public void setParent(UITTNode o){parent = o;}

    /** Side-effect, wire-up children to reference parent **/
    public void copyIntoChildren(Vector v)
    {
         Enumeration en = v.elements();
         while( en.hasMoreElements() ) {
            Object obj = en.nextElement();
            if( obj instanceof UITTNode ) {
                ((UITTNode)obj).parent = this;
            }
            children.addElement(obj);
         }
    }

  public void printContent(org.cougaar.core.util.AsciiPrinter pr) {
    pr.print(url, "Url");
    pr.print(value, "Value");
    pr.print(children, "Children");
    pr.print(parent, "Parent");
  }

  public String toString() {
    return org.cougaar.core.util.PrettyStringPrinter.toString(this);
  }

  /** set 3/29/00 **/
  static final long serialVersionUID = 2823954007101393331L;

}
