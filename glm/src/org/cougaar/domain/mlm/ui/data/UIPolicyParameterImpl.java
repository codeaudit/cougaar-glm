/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.ui.data;

import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

import org.cougaar.domain.planning.ldm.policy.RuleParameter;

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


