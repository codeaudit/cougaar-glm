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
 
package org.cougaar.glm.ldm.plan;
 
import org.cougaar.glm.ldm.plan.NamedPosition;
import org.cougaar.glm.ldm.plan.NewNamedPosition;
import org.cougaar.planning.ldm.measure.Longitude;
import org.cougaar.planning.ldm.measure.Latitude;
 
 
public class NamedPositionImpl extends PositionImpl
  implements NamedPosition, NewNamedPosition {
	
  String name;
	
  public NamedPositionImpl() {
    super();
  }
	
  public NamedPositionImpl(Latitude la, Longitude lo, String aname) {
    super(la,lo);
    setName(aname);
  } 
	
  /** @return String - the string name representing this position */
  public String getName() {
    return name;
  }
	
  /** @param aName - set the string name representing this position */
  public void setName(String aName) {
    if (aName != null) aName = aName.intern();
    name = aName;
  }

  public Object clone() {
    return new NamedPositionImpl(lat, lon, name);
  }
}
