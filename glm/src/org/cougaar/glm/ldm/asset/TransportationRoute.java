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
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.Vector;

import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.measure.Distance;

// Lots of stuff from TransportationGraph won't work with this because 
// of the way links are handled. Links were removed and then readded.

public class TransportationRoute extends TransportationGraph {
  // Instance Variables
  private TransportationNode mySource;
  private TransportationNode myDestination;
  private Vector myLinks;

  // Constructors
  public TransportationRoute() {
    mySource = null;
    myDestination = null;
    myLinks = new Vector();
  }

  public TransportationRoute(TransportationRoute prototype) {
    super(prototype);
    mySource = null;
    myDestination = null;
    myLinks = new Vector();
  }

  public TransportationRoute(TransportationNode source, TransportationNode destination,
			     Vector nodes) {
    myLinks = new Vector();
    addGraphByNodes(nodes);
    mySource = source;
    myDestination = destination;
  }

  /** For infrastructure only - use LdmFactory.copyInstance instead. **/
  public Object clone() throws CloneNotSupportedException {
    TransportationRoute _thing = (TransportationRoute) super.clone();
    if (mySource!=null) _thing.setSource(mySource);
    if (myDestination!=null) _thing.setDestination(myDestination);
    if (myLinks!=null) _thing.setLinks(myLinks);
    return _thing;
  }

  /** create an instance of the right class for copy operations **/
  public Asset instanceForCopy() {
    return new TransportationRoute();
  }

  /** create an instance of this prototype **/
  public Asset createInstance() {
    return new TransportationRoute(this);
  }

  // Get Accessors
  public TransportationNode getSource() { return mySource; }
  public TransportationNode getDestination() { return myDestination; }
  public Vector getLinks () { return myLinks; }
  public void setLinks (Vector arg_Links) {
    myLinks = new Vector (arg_Links);
  }

  public Distance getLength() {
    double total_length = 0.0;
    for (int i = 0; i < myLinks.size(); i++) {
      TransportationLink current_link = (TransportationLink)myLinks.elementAt(i);
      if (current_link instanceof TransportationRoadLink) {
        total_length += ((TransportationRoadLink)current_link).getRoadLinkPG().getLinkLength().getMiles();
      } else if (current_link instanceof TransportationRailLink) {
	total_length += ((TransportationRailLink)current_link).getRailLinkPG().getLinkLength().getMiles();
      } else if (current_link instanceof TransportationSeaLink) {
	total_length += ((TransportationSeaLink)current_link).getSeaLinkPG().getLinkLength().getMiles();
      } else if (current_link instanceof TransportationAirLink) {
	total_length += ((TransportationAirLink)current_link).getAirLinkPG().getLinkLength().getMiles();
      }
    }
    return Distance.newMiles(total_length);
  }

  // Set Accessors

  /**
   * Sets the route to the nodes in the vector <code>newnodes</code>.
   * Also sets the source and destination to be the first and last
   * nodes in the vector, respectively.
   * @param newnodes Vector of nodes that mark the route
   */
  public void addGraphByNodes(Vector newnodes) {
    // precondition
    TransportationNode lastNode = null;
    TransportationNode thisNode = null;
    if (newnodes.size () < 2)
      throw new RuntimeException ("Route expects at least 2 nodes. " + 
				  "Only got " + newnodes.size () + ".");

    for (int i=0; i<newnodes.size(); i++) {
      try {
	thisNode = (TransportationNode)newnodes.elementAt(i);
	addNode(thisNode);
	if (lastNode != null) {
	  Vector listLinks = thisNode.getLinks(); 
	  for (int j=0; j<listLinks.size(); j++) {
	    if (((TransportationLink)listLinks.elementAt(j)).otherEnd(thisNode) == lastNode) {
	      myLinks.addElement(listLinks.elementAt(j));
	    }
	  }
	}
	lastNode = thisNode;
      } catch (Exception e) {
        e.printStackTrace();
	System.err.println("!!!error: got invalid node "+
			   "while setting nodes in TransportationGraph");
      }
    }
    setSource      ((TransportationNode)newnodes.firstElement());
    setDestination ((TransportationNode)newnodes.lastElement ());
  }

  /**
   * DANGER- this allows the use of the underlying Graph methods; 
   * then call recomputeRoute() to compute the route from 
   * the available graph.
   * We avoid cycles and other nastiness by assuming that the route
   * is to be followed in the order that the NODES are stored in the VECTOR.
   */
  public void recomputeRoute() {
    Vector currentNodes = this.getNodes();
    if (currentNodes.size() < 2)
      return;

    // Copy the nodes safely so we don't lose them when we removeNode
    // (I think this is a problem; i.e. that currentNodes is actually the
    // same reference as the vector that removeNode is removing from)
    // and don't use an Enumeration; we want to preserve ordering
    Vector copiedNodes = new Vector(currentNodes.size());
    for (int i = 0; i < currentNodes.size(); i++)
      copiedNodes.addElement(currentNodes.elementAt(i));

    Enumeration n_enum = currentNodes.elements();
    while (n_enum.hasMoreElements()) {
      TransportationNode next_node = (TransportationNode)n_enum.nextElement();
      this.removeNode(next_node);
    }

    myLinks = new Vector();
    addGraphByNodes(copiedNodes);
  }

  public void setSource(TransportationNode source) { mySource = source; }
  public void setDestination(TransportationNode destination) { myDestination = destination; }

  // Methods

  public boolean CheckRoute() {
    if (mySource == myDestination) return false;
    TransportationNode current_node = mySource;
    TransportationLink current_link;
    Vector current_node_links;
    Vector myNodes = getNodes ();
    int link_count;
    while (current_node != myDestination) {
      current_link = null;
      current_node_links = current_node.getLinks();
      link_count = 0;
      for (int i = 0; i < current_node_links.size(); i++) {
	if (myNodes.contains(((TransportationLink)current_node_links.elementAt(i)).otherEnd(current_node)) &&
	    myLinks.contains((TransportationLink)current_node_links.elementAt(i))) {
	  current_link = (TransportationLink)current_node_links.elementAt(i);
	  link_count++;
	}
      }
      if (current_link == null || link_count != 1) return false;
      current_node = current_link.otherEnd(current_node);
    }
    return true;
  }

  public boolean useLink(TransportationLink link) {
    return myLinks.contains(link);
  }

  // Read/Write
  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    out.writeObject(mySource);
    out.writeObject(myDestination);
    out.writeObject(myLinks);
  }

  private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
    in.defaultReadObject();
    mySource=(TransportationNode)in.readObject();
    myDestination=(TransportationNode)in.readObject();
    myLinks=(Vector)in.readObject();
  }
  // beaninfo support
  private static PropertyDescriptor properties[];
  static {
    try {
      properties = new PropertyDescriptor[3];
      properties[0] = new PropertyDescriptor("Source", TransportationRoute.class, "getSource", null);
      properties[1] = new PropertyDescriptor("Destination", TransportationRoute.class, "getDestination", null);
      properties[2] = new PropertyDescriptor("Links", TransportationRoute.class, "getLinks", null);
    } catch (IntrospectionException ie) {}
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    PropertyDescriptor[] pds = super.getPropertyDescriptors();
    PropertyDescriptor[] ps = new PropertyDescriptor[pds.length+3];
    System.arraycopy(pds, 0, ps, 0, pds.length);
    System.arraycopy(properties, 0, ps, pds.length, 3);
    return ps;
  }
}

