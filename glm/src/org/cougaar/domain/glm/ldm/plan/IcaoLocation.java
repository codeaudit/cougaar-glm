/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.glm.ldm.plan;
 
public interface IcaoLocation extends NamedPosition {
	
	/** @return String - the string Icao code representing this position */
	String getIcaoCode();
	
	
}