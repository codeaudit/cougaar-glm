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
 
import org.cougaar.domain.planning.ldm.measure.Longitude;
import org.cougaar.domain.planning.ldm.measure.Latitude;
import org.cougaar.domain.planning.ldm.plan.*;
 
 
public interface Position extends Location, Cloneable {
	
  /** @return Latitude - the Latitude representing this position */
  Latitude getLatitude();
	
  /** @return Longitude - the Longitude representing this position */
  Longitude getLongitude();
}
