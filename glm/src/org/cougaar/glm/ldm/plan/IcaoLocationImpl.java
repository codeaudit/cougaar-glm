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
 
import org.cougaar.planning.ldm.measure.Latitude;
import org.cougaar.planning.ldm.measure.Longitude;
 
 
public class IcaoLocationImpl extends NamedPositionImpl
  implements IcaoLocation, NewIcaoLocation {
	
  String icao;
	
  public IcaoLocationImpl() {
    super();
  }
	
  public IcaoLocationImpl(Latitude la, Longitude lo, String aname, String anIcao) {
    super(la,lo, aname);
    setIcaoCode(anIcao);
  } 
	
  /** @return String - the string Icao code representing this position */
  public String getIcaoCode() {
    return icao;
  }

	
  /** @param anIcaoCode - set the string Icao code representing this position */
  public void setIcaoCode(String anIcaoCode) {
    if (anIcaoCode != null) anIcaoCode = anIcaoCode.intern();
    icao = anIcaoCode;
  }
	
}
