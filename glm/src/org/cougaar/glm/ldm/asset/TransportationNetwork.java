/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
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
