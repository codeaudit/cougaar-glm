/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.glm.ldm.plan;
 
import org.cougaar.domain.planning.ldm.measure.Longitude;
import org.cougaar.domain.planning.ldm.measure.Latitude;
import org.cougaar.domain.planning.ldm.plan.*;
import org.cougaar.domain.glm.ldm.plan.Position;
import org.cougaar.domain.glm.ldm.plan.NewPosition;
 
 
public class PositionImpl 
  implements Position, NewPosition, java.io.Serializable
{
	
  protected Latitude lat = null;
  protected Longitude lon = null;
	
  public PositionImpl() {
    super();
  }
	
  public PositionImpl(Latitude la, Longitude lo) {
    lat = la;
    lon = lo;
  } 
	
  /** @return Latitude - the Latitude representing this position */
  public Latitude getLatitude() {
    return lat;
  }	
	
  /** @return Longitude - the Longitude representing this position */
  public Longitude getLongitude() {
    return lon;
  }
	
  /** @param latitude - set the Latitude representing this position */
  public void setLatitude(Latitude latitude) {
    lat = latitude;
  }
	
  /** @param longitude - set the Longitude representing this position */
  public void setLongitude(Longitude longitude) {
    lon = longitude;
  }
	
  public Object clone() {
    return new PositionImpl(lat, lon);
  }

}
