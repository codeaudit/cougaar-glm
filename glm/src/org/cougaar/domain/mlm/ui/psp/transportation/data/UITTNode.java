/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.psp.transportation.data;

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
