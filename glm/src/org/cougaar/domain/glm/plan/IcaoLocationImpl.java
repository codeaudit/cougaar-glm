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
 
import org.cougaar.domain.glm.plan.IcaoLocation;
import org.cougaar.domain.glm.plan.NewIcaoLocation;
import org.cougaar.domain.planning.ldm.measure.Latitude;
import org.cougaar.domain.planning.ldm.measure.Longitude;
 
 
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
