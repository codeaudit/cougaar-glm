/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.glm.plan;
 
public interface NewIcaoLocation extends IcaoLocation, NewNamedPosition {
	
	/** @param anIcaoCode - set the string Icao code representing this position */
	void setIcaoCode(String anIcaoCode);
	
	
}