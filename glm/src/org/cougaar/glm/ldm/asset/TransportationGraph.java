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

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;

import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.measure.Latitude;
import org.cougaar.planning.ldm.measure.Longitude;
public class TransportationGraph extends Facility {
  public TransportationGraph() {
    this(new Vector(), Longitude.newLongitude(179.0d), 
	 Longitude.newLongitude(-180.0d),
	 Latitude.newLatitude(90.0d), 
	 Latitude.newLatitude(-90.0d));
  }
  public TransportationGraph(TransportationGraph prototype) {
    super(prototype);
    myNodes = new Vector();
    myMinLong = Longitude.newLongitude(179.0d);
    myMaxLong = Longitude.newLongitude(-180.0d);
    myMinLat  = Latitude.newLatitude(90.0d);
    myMaxLat  = Latitude.newLatitude(-90.0d);
  }

  public TransportationGraph(Vector nodes, 
			       Longitude minlong, Longitude maxlong,
			       Latitude minlat, Latitude maxlat) {
    myNodes = nodes;
    myMinLong = minlong;
    myMaxLong = maxlong;
    myMinLat = minlat;
    myMaxLat = maxlat;
  }
  
  /** For infrastructure only - use LdmFactory.copyInstance instead. **/
  public Object clone() throws CloneNotSupportedException {
    TransportationGraph _thing = (TransportationGraph) super.clone();
    if (myNodes!=null) _thing.setNodes(myNodes);
    if (myMinLong!=null) _thing.setMinLong(myMinLong);
    if (myMaxLong!=null) _thing.setMaxLong(myMaxLong);
    if (myMinLat!=null) _thing.setMinLat(myMinLat);
    if (myMaxLat!=null) _thing.setMaxLat(myMaxLat);
    return _thing;
  }

  /** create an instance of the right class for copy operations **/
  public Asset instanceForCopy() {
    return new TransportationGraph();
  }

  /** create an instance of this prototype **/
  public Asset createInstance() {
    return new TransportationGraph(this);
  }
  
  private transient Longitude myMinLong;

  public Longitude getMinLong() {
    if (myMinLong==null) {
      if (myPrototype != null) {
         return ((TransportationGraph)myPrototype).getMinLong();
      } else {
         myMinLong = null;
      }
    }
    return myMinLong;
  }

  public void setMinLong(Longitude arg_MinLong) {
    myMinLong= arg_MinLong;
  }

  private transient Longitude myMaxLong;

  public Longitude getMaxLong() {
    if (myMaxLong==null) {
      if (myPrototype != null) {
         return ((TransportationGraph)myPrototype).getMaxLong();
      } else {
         myMaxLong = null;
      }
    }
    return myMaxLong;
  }

  public void setMaxLong(Longitude arg_MaxLong) {
    myMaxLong= arg_MaxLong;
  }

  private transient Latitude myMinLat;

  public Latitude getMinLat() {
    if (myMinLat==null) {
      if (myPrototype != null) {
         return ((TransportationGraph)myPrototype).getMinLat();
      } else {
         myMinLat = null;
      }
    }
    return myMinLat;
  }

  public void setMinLat(Latitude arg_MinLat) {
    myMinLat= arg_MinLat;
  }

  private transient Latitude myMaxLat;

  public Latitude getMaxLat() {
    if (myMaxLat==null) {
      if (myPrototype != null) {
         return ((TransportationGraph)myPrototype).getMaxLat();
      } else {
         myMaxLat = null;
      }
    }
    return myMaxLat;
  }

  public void setMaxLat(Latitude arg_MaxLat) {
    myMaxLat= arg_MaxLat;
  }

  private transient Vector myNodes;

  public Vector getNodes() {
    if (myNodes==null) {
      if (myPrototype != null) {
         return ((TransportationGraph)myPrototype).getNodes();
      } else {
         myNodes = null;
      }
    }
    return myNodes;
  }

  public void setNodes(Vector nodes) { 
    myNodes = nodes; 
  }

  public int getNumNodes() { return myNodes.size(); }

  public void addNode(TransportationNode node) {
    myNodes.addElement(node);
    ExpandBounds(node);
  }

  public void removeNode(TransportationNode node) {
    myNodes.removeElement(node);
    AdjustBounds(node);
  }

  public void addGraphByNodes(Vector newnodes) {
    for (int i=0; i<newnodes.size(); i++) {
      try {
	addNode((TransportationNode)newnodes.elementAt(i));
      } catch (Exception e) {
        e.printStackTrace();
	System.err.println("!!!error: got invalid node "+
			   "while setting nodes in TransportationGraph");
      }
    }
  }

  public void setGraphByNodes(Vector newnodes) {
    myNodes = new Vector();
    myMaxLat = Latitude.newLatitude(Double.MIN_VALUE);
    myMinLat = Latitude.newLatitude(Double.MAX_VALUE);
    myMaxLong = Longitude.newLongitude(Double.MIN_VALUE);
    myMinLong = Longitude.newLongitude(Double.MAX_VALUE);
    addGraphByNodes(newnodes);
  }

  public void addGraph (TransportationGraph otherGraph) {
    Vector otherNodes = otherGraph.getNodes ();
    for (int i=0; i<otherGraph.getNumNodes(); i++) {
      try {
	if (!myNodes.contains (otherNodes.elementAt(i)))
	  addNode((TransportationNode)otherNodes.elementAt(i));
      } catch (Exception e) {
        e.printStackTrace();
	System.err.println("!!!error: got invalid node "+
			   "while setting nodes in TransportationGraph");
      }
    }
  }

  private void ExpandBounds(TransportationNode node) {
    Latitude nodelat = node.getGeolocLocation().getLatitude();
    Longitude nodelong = node.getGeolocLocation().getLongitude();
    
    if (nodelat.getValue(Latitude.DEGREES) < myMinLat.getValue(Latitude.DEGREES))
      myMinLat = Latitude.newLatitude(nodelat.getValue(Latitude.DEGREES));
    if (nodelat.getValue(Latitude.DEGREES) > myMaxLat.getValue(Latitude.DEGREES))
      myMaxLat = Latitude.newLatitude(nodelat.getValue(Latitude.DEGREES));
    if (nodelong.getValue(Longitude.DEGREES) < myMinLong.getValue(Longitude.DEGREES))
      myMinLong = Longitude.newLongitude(nodelong.getValue(Longitude.DEGREES));
    if (nodelong.getValue(Longitude.DEGREES) > myMaxLong.getValue(Longitude.DEGREES))
      myMaxLong = Longitude.newLongitude(nodelong.getValue(Longitude.DEGREES));
  }
  private void AdjustBounds(TransportationNode node) {
    Latitude nodelat = node.getGeolocLocation().getLatitude();
    Longitude nodelong = node.getGeolocLocation().getLongitude();

    double mindegrees = Double.MAX_VALUE;
    double maxdegrees = Double.MIN_VALUE;
    double target;
    if (nodelat.equals(myMinLat) || nodelat.equals(myMaxLat)) {
      for (int i = 0; i < myNodes.size(); i++) {
	target = ((TransportationNode)myNodes.elementAt(i)).getGeolocLocation().
	  getLatitude().getValue(Latitude.DEGREES);
	if (target < mindegrees) {
	  mindegrees = target;
	  myMinLat = ((TransportationNode)myNodes.elementAt(i)).getGeolocLocation().getLatitude();
	}
	if (target > maxdegrees) {
	  maxdegrees = target;
	  myMaxLat = ((TransportationNode)myNodes.elementAt(i)).getGeolocLocation().getLatitude();
	}
      }
    }
    mindegrees = Double.MAX_VALUE;
    maxdegrees = Double.MIN_VALUE;
    if (nodelong.equals(myMinLong) || nodelong.equals(myMaxLong)) {
      for (int i = 0; i < myNodes.size(); i++) {
	target = ((TransportationNode)myNodes.elementAt(i)).getGeolocLocation().
	  getLongitude().getValue(Longitude.DEGREES);
	if (target < mindegrees) {
	  mindegrees = target;
	  myMinLong = ((TransportationNode)myNodes.elementAt(i)).getGeolocLocation().getLongitude();
	}
	if (target > maxdegrees) {
	  maxdegrees = target;
	  myMaxLong = ((TransportationNode)myNodes.elementAt(i)).getGeolocLocation().getLongitude();
	}
      }
    }
  }
  // Read/Write Methods
  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    out.writeObject(myNodes);
    out.writeObject(myMinLong);
    out.writeObject(myMaxLong);
    out.writeObject(myMinLat);
    out.writeObject(myMaxLat);
  }

  private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
    in.defaultReadObject();
    myNodes=(Vector)in.readObject();
    myMinLong=(Longitude)in.readObject();
    myMaxLong=(Longitude)in.readObject();
    myMinLat=(Latitude)in.readObject();
    myMaxLat=(Latitude)in.readObject(); 
  }
  // beaninfo support
  private static PropertyDescriptor properties[];
  static {
    try {
      properties = new PropertyDescriptor[6];
      properties[0] = new PropertyDescriptor("FacilityPG", TransportationGraph.class, "getFacilityPG", null);
      properties[1] = new PropertyDescriptor("Nodes", TransportationGraph.class, "getNodes", null);
      properties[2] = new PropertyDescriptor("MinLong", TransportationGraph.class, "getMinLong", null);
      properties[3] = new PropertyDescriptor("MaxLong", TransportationGraph.class, "getMaxLong", null);
      properties[4] = new PropertyDescriptor("MinLat", TransportationGraph.class, "getMinLat", null);
      properties[5] = new PropertyDescriptor("MaxLat", TransportationGraph.class, "getMaxLat", null);
    } catch (IntrospectionException ie) {}
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    PropertyDescriptor[] pds = super.getPropertyDescriptors();
    PropertyDescriptor[] ps = new PropertyDescriptor[pds.length+6];
    System.arraycopy(pds, 0, ps, 0, pds.length);
    System.arraycopy(properties, 0, ps, pds.length, 6);
    return ps;
  }
}
