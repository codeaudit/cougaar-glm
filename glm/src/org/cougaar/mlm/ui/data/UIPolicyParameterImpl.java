/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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

package org.cougaar.mlm.ui.data;

import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

import org.cougaar.planning.ldm.policy.RuleParameter;

public class UIPolicyParameterImpl 
  implements UIPolicyParameter, XMLUIPlanObject {

  protected String myName;
  protected int myType;
  protected Object myValue;

  public UIPolicyParameterImpl(String name, int type, Object value) {
    myName = name;
    myType = type;
    myValue = value;
  }

  /**
   * @return String - the name of the policy parameter
   */
  public String getName() {
    return myName;
  }

  /**
   * @return int - the type of the policy parameter
   */
  public int getType() {
    return myType;
  }

  /**
   * @return String - the value of the policy parameter
   */
  public Object getValue() {
    return myValue;
  }

  //  XMLPlanObject method for UI
  
  public Element getXML(Document doc, Vector requestedFields) {
    return XMLUIPlanObjectConverter.getPlanObjectXML(this, doc, requestedFields);
  }

}


