/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

/* hand generated! */

package org.cougaar.domain.glm.ldm.asset;

import org.cougaar.domain.glm.ldm.plan.*;
import org.cougaar.domain.planning.ldm.asset.*;

import org.cougaar.domain.planning.ldm.measure.*;
import org.cougaar.domain.planning.ldm.plan.*;

import java.beans.PropertyDescriptor;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

import java.util.Vector;

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
