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

/* hand generated! */

package org.cougaar.glm.ldm.asset;

import java.util.Vector;

import org.cougaar.glm.ldm.plan.GeolocLocation;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.measure.Latitude;
import org.cougaar.planning.ldm.measure.Longitude;

public class TransportationNetwork extends TransportationGraph {
  public TransportationNetwork() {
    super();
  }

  public TransportationNetwork(TransportationNetwork prototype) {
    super(prototype);
  }

  public TransportationNetwork(Vector nodes, 
			       Longitude minlong, Longitude maxlong,
			       Latitude minlat, Latitude maxlat) {
    super(nodes,minlong,maxlong,minlat,maxlat);
  }
  
  /** create an instance of this prototype **/
  public Asset createInstance() {
    return new TransportationNetwork(this);
  }

  /** For infrastructure only - use LdmFactory.copyInstance instead. **/
  public Object clone() throws CloneNotSupportedException {
    TransportationNetwork _thing = (TransportationNetwork) super.clone();
    return _thing;
  }

  /** create an instance of the right class for copy operations **/
  public Asset instanceForCopy() {
    return new TransportationNetwork();
  }

  protected void fillAllPropertyGroups(Vector v) {
    super.fillAllPropertyGroups(v);
  }

  // Get Accessors
  public TransportationNetwork getMap(Longitude minlong, Longitude maxlong,
				      Latitude minlat, Latitude maxlat) {
    TransportationNetwork retval = new TransportationNetwork(new Vector(),
							     minlong, maxlong, 
							     minlat, maxlat);
    GeolocLocation target;
    Vector nodes = getNodes ();
    for (int i = 0; i < getNumNodes(); i++) {
      target = ((TransportationNode)nodes.elementAt(i)).getGeolocLocation();
      if (target.getLongitude().getValue(Longitude.DEGREES) <= 
	  retval.getMaxLong().getValue(Longitude.DEGREES) &&
	  target.getLongitude().getValue(Longitude.DEGREES) >= 
	  retval.getMinLong().getValue(Longitude.DEGREES) &&
	  target.getLatitude().getValue(Latitude.DEGREES) <= 
	  retval.getMaxLat().getValue(Latitude.DEGREES) &&
	  target.getLatitude().getValue(Latitude.DEGREES) >= 
	  retval.getMinLat().getValue(Latitude.DEGREES)) {
	retval.addNode((TransportationNode)nodes.elementAt(i));
      }
    }
    return retval;
  }
}
