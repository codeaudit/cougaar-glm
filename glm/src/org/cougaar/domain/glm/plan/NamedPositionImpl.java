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
 
import org.cougaar.domain.glm.plan.NamedPosition;
import org.cougaar.domain.glm.plan.NewNamedPosition;
import org.cougaar.domain.planning.ldm.measure.Longitude;
import org.cougaar.domain.planning.ldm.measure.Latitude;
 
 
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
