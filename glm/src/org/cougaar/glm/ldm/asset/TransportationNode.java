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

import org.cougaar.glm.ldm.plan.*;
import org.cougaar.planning.ldm.measure.*;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.planning.ldm.asset.*;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.util.Vector;

import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

public class TransportationNode extends Facility {
  // Instance Variables
  private transient String myDescription = "";
  private transient GeolocLocation myGeolocLocation;
  private transient Vector myLinks;
  private transient AirportPG myAirportPG;
  private transient RailTerminalPG myRailTerminalPG;
  private transient RepairDepotPG myRepairDepotPG;
  private transient SeaportPG mySeaportPG;
  private transient SupplyDepotPG mySupplyDepotPG;

  /** 
   * Constructor with no arguments.
   */
  public TransportationNode() {
    this(null, null, new Vector(), null, null, null, null, null);
  }

  /** 
   * A constructor which clones an input node.
   */
  public TransportationNode(TransportationNode newnode) {
    super(newnode);
    myDescription = "";
    myGeolocLocation = null;
    myLinks = new Vector();
    myAirportPG = null;
    myRailTerminalPG = null;
    myRepairDepotPG = null;
    mySeaportPG = null;
    mySupplyDepotPG = null; 
  }

  /** 
   * A constructor taking all the arguments.
   */
  public TransportationNode(String description, GeolocLocation position, Vector links,
			    AirportPG airport, RailTerminalPG railterminal,
			    RepairDepotPG repairdepot, SeaportPG seaport,
			    SupplyDepotPG supplydepot) {
    myDescription = description;
    myGeolocLocation = position;
    myLinks = links;
    myAirportPG = airport;
    myRailTerminalPG = railterminal;
    myRepairDepotPG = repairdepot;
    mySeaportPG = seaport;
    mySupplyDepotPG = supplydepot;
  }

  /** For infrastructure only - use LdmFactory.copyInstance instead. **/
  public Object clone() throws CloneNotSupportedException {
    TransportationNode _thing = (TransportationNode) super.clone();
    if (myDescription!=null) _thing.setDescription(myDescription);
    if (myGeolocLocation!=null) _thing.setGeolocLocation(myGeolocLocation);
    if (myLinks!=null) _thing.setLinks(myLinks);
    if (myAirportPG!=null) _thing.setAirportPG(myAirportPG);
    if (myRailTerminalPG!=null) _thing.setRailTerminalPG(myRailTerminalPG);
    if (myRepairDepotPG!=null) _thing.setRepairDepotPG(myRepairDepotPG);
    if (mySeaportPG!=null) _thing.setSeaportPG(mySeaportPG);
    if (mySupplyDepotPG!=null) _thing.setSupplyDepotPG(mySupplyDepotPG);
    return _thing;
  }

  /** create an instance of the right class for copy operations **/
  public Asset instanceForCopy() {
    return new TransportationNode();
  }

  /** create an instance of this prototype **/
  public Asset createInstance() {
    return new TransportationNode(this);
  }

  protected void fillAllPropertyGroups(Vector v) {
    super.fillAllPropertyGroups(v);
    v.addElement(myAirportPG);
    v.addElement(myRailTerminalPG);
    v.addElement(myRepairDepotPG);
    v.addElement(mySeaportPG);
    v.addElement(mySupplyDepotPG);
  }

  /** 
   * Get the descriptive string for this node.
   * @return String
   */
  public String getDescription() { return myDescription; }

  public GeolocLocation getGeolocLocation() { return myGeolocLocation; }
  public Vector getLinks() { return myLinks; }
  public int getNumLinks() { return myLinks.size(); }
  public AirportPG getAirport() { return myAirportPG; }
  public RailTerminalPG getRailTerminal() { return myRailTerminalPG; }
  public RepairDepotPG getRepairDepot() { return myRepairDepotPG; }
  public SeaportPG getSeaport() { return mySeaportPG; }
  public SupplyDepotPG getSupplyDepot() { return mySupplyDepotPG; }

  // Set
  public void setDescription(String description) { 
    myDescription = description; 
  }
  public void setGeolocLocation(GeolocLocation position) {
    myGeolocLocation = position;
  }
  public void setLinks(Vector links) { 
    myLinks = links; 
  }
  public void addLink(TransportationLink link) {
    myLinks.addElement(link);
  }
  public boolean removeLink(TransportationLink link) {
    return myLinks.removeElement(link);
  }
  public void setAirport(AirportPG airport) { 
    myAirportPG = airport; 
  }
  public void setRailTerminal(RailTerminalPG railterminal) { 
    myRailTerminalPG = railterminal; 
  }
  public void setRepairDepot(RepairDepotPG repairdepot) { 
    myRepairDepotPG = repairdepot; 
  }
  public void setSeaport(SeaportPG seaport) { 
    mySeaportPG = seaport; }
  public void setSupplyDepot(SupplyDepotPG supplydepot) { 
    mySupplyDepotPG = supplydepot; 
  }

  // Methods
  public boolean inside(Latitude maxlat, Latitude minlat, Longitude maxlong, Longitude minlong) {
    return (myGeolocLocation.getLatitude().getDegrees() <= maxlat.getDegrees() &&
	    myGeolocLocation.getLatitude().getDegrees() >= minlat.getDegrees() &&
	    myGeolocLocation.getLongitude().getDegrees() <= maxlong.getDegrees() &&
	    myGeolocLocation.getLongitude().getDegrees() >= minlong.getDegrees());
  }

  // Reading/Writing 
  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    out.writeObject(myAirportPG);
    out.writeObject(myGeolocLocation);
    out.writeObject(myRailTerminalPG);
    out.writeObject(myRepairDepotPG);
    out.writeObject(mySeaportPG);
    out.writeObject(mySupplyDepotPG);
    out.writeObject(myLinks);
    out.writeObject(myDescription);
  }

  private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
    in.defaultReadObject();
    myAirportPG=(AirportPG)in.readObject();
    myGeolocLocation=(GeolocLocation)in.readObject();
    myRailTerminalPG=(RailTerminalPG)in.readObject();
    myRepairDepotPG=(RepairDepotPG)in.readObject();
    mySeaportPG=(SeaportPG)in.readObject();
    mySupplyDepotPG=(SupplyDepotPG)in.readObject();
    myLinks=(Vector)in.readObject();
    myDescription=(String)in.readObject();
  }
  // beaninfo support
  private static PropertyDescriptor properties[];
  static {
    try {
      properties = new PropertyDescriptor[9];
      properties[0] = new PropertyDescriptor("FacilityPG", TransportationNode.class, "getFacilityPG", null);
      properties[1] = new PropertyDescriptor("AirportPG", TransportationNode.class, "getAirportPG", null);
      properties[2] = new PropertyDescriptor("GeolocLocation", TransportationNode.class, "getGeolocLocation", null);
      properties[3] = new PropertyDescriptor("RailTerminalPG", TransportationNode.class, "getRailTerminalPG", null);
      properties[4] = new PropertyDescriptor("RepairDepotPG", TransportationNode.class, "getRepairDepotPG", null);
      properties[5] = new PropertyDescriptor("SeaportPG", TransportationNode.class, "getSeaportPG", null);
      properties[6] = new PropertyDescriptor("SupplyDepotPG", TransportationNode.class, "getSupplyDepotPG", null);
      properties[7] = new PropertyDescriptor("Links", TransportationNode.class, "getLinks", null);
      properties[8] = new PropertyDescriptor("Description", TransportationNode.class, "getDescription", null);
    } catch (IntrospectionException ie) {}
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    PropertyDescriptor[] pds = super.getPropertyDescriptors();
    PropertyDescriptor[] ps = new PropertyDescriptor[pds.length+9];
    System.arraycopy(pds, 0, ps, 0, pds.length);
    System.arraycopy(properties, 0, ps, pds.length, 9);
    return ps;
  }
}
