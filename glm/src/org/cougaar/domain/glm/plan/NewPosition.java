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
 
import org.cougaar.domain.planning.ldm.measure.Longitude;
import org.cougaar.domain.planning.ldm.measure.Latitude;
 
 
public interface NewPosition extends Position {
	
	/** @param latitude - set the Latitude representing this position */
	void setLatitude(Latitude latitude);
	
	/** @param longitude - set the Longitude representing this position */
	void setLongitude(Longitude longitude);
}