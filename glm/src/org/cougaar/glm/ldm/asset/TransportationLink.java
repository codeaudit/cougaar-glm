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
import java.util.Collection;
import java.util.Date;
import java.util.Vector;

import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.Perturbator;
import org.cougaar.planning.ldm.plan.RoleSchedule;
import org.cougaar.planning.ldm.plan.RoleScheduleImpl;
import org.cougaar.planning.ldm.plan.Schedule;
import org.cougaar.planning.ldm.plan.ScheduleElement;
import org.cougaar.planning.ldm.plan.ScheduleElementImpl;
import org.cougaar.planning.ldm.plan.ScheduleImpl;
import org.cougaar.util.TimeSpan;

public class TransportationLink extends Facility {

  public TransportationLink() {
    this(null,null,null,"");
  }

  public TransportationLink(TransportationLink prototype) {
    super(prototype);
    myNetwork = null;
    myOrigin = null;
    myDestination = null;
    myName = "";
    RoleSchedule myrolesched = getRoleSchedule();
    ((RoleScheduleImpl)myrolesched).setAvailableSchedule(new ScheduleImpl());
    Schedule mysched = getRoleSchedule().getAvailableSchedule();
    ((ScheduleImpl)mysched).setScheduleElement((ScheduleElement)new ScheduleElementImpl(new Date(0), new Date(TimeSpan.MAX_VALUE)));
  }

  public TransportationLink(TransportationNetwork network,
			    TransportationNode origin,
			    TransportationNode destination,
			    String name) {
    myOrigin = origin;
    myDestination = destination;
    myNetwork = network;
    myName = name;
    RoleSchedule myrolesched = getRoleSchedule();
    ((RoleScheduleImpl)myrolesched).setAvailableSchedule(new ScheduleImpl());
    Schedule mysched = getRoleSchedule().getAvailableSchedule();
    ((ScheduleImpl)mysched).setScheduleElement((ScheduleElement)new ScheduleElementImpl(new Date(0), new Date(TimeSpan.MAX_VALUE)));
  }

  /** For infrastructure only - use LdmFactory.copyInstance instead. **/
  public Object clone() throws CloneNotSupportedException {
    TransportationLink _thing = (TransportationLink) super.clone();
    if (myOrigin!=null) _thing.setOrigin(myOrigin);
    if (myDestination!=null) _thing.setDestination(myDestination);
    if (myName!=null) _thing.setName(myName);
    if (myNetwork!=null) _thing.setNetwork(myNetwork);
    return _thing;
  }

  /** create an instance of the right class for copy operations **/
  public Asset instanceForCopy() {
    return new TransportationLink();
  }

  /** create an instance of this prototype **/
  public Asset createInstance() {
    return new TransportationLink(this);
  }

  protected void fillAllPropertyGroups(Vector v) {
    super.fillAllPropertyGroups(v);
  }

  protected transient TransportationNetwork myNetwork;

  public TransportationNetwork getNetwork() {
    if (myNetwork==null) {
      if (myPrototype != null) {
         return ((TransportationLink)myPrototype).getNetwork();
      } else {
         myNetwork = null;
      }
    }
    return myNetwork;
  }

  public void setNetwork(TransportationNetwork arg_Network) {
    myNetwork= arg_Network;
  }

  protected transient TransportationNode myOrigin;

  public TransportationNode getOrigin() {
    if (myOrigin==null) {
      if (myPrototype != null) {
         return ((TransportationLink)myPrototype).getOrigin();
      } else {
         myOrigin = null;
      }
    }
    return myOrigin;
  }

  public void setOrigin(TransportationNode arg_Origin) {
    myOrigin= arg_Origin;
  }

  protected transient TransportationNode myDestination;

  public TransportationNode getDestination() {
    if (myDestination==null) {
      if (myPrototype != null) {
         return ((TransportationLink)myPrototype).getDestination();
      } else {
         myDestination = null;
      }
    }
    return myDestination;
  }

  public void setDestination(TransportationNode arg_Destination) {
    myDestination= arg_Destination;
  }

  protected transient String myName;

  public String getName() {
    if (myName==null) {
      if (myPrototype != null) {
         return ((TransportationLink)myPrototype).getName();
      } else {
         myName = null;
      }
    }
    return myName;
  }

  public void setName(String arg_Name) {
    myName= arg_Name;
  }

  // get

  public Schedule getSchedule() { return getRoleSchedule().getAvailableSchedule(); }

    //  Perturbator myPerturbator = new Perturbator();
  Perturbator myPerturbator = null;

  public void setPerturbator(Perturbator p) {
    myPerturbator = p;
  }

  /**
   * The get Perturbator call as a "side-effect" changes
   * the availability of the link to be only available
   * between the java begining of time and 20 seconds
   * after that, making the link effectively unavailable.
   *
   * This is necessary because the perturbation mechanism 
   * currently doesn't allow changing availabilities directly.
   */
  public Perturbator getPerturbator() {
    Schedule sched = getSchedule();
    ScheduleElement y = (ScheduleElement)new ScheduleElementImpl(new Date(0), new Date(20000));
    ((ScheduleImpl)sched).setScheduleElement(y);
    return myPerturbator;
  }

  // methods

  public boolean available(Date start, Date end) {
    Date tstart = start; 
    Schedule avail = getRoleSchedule().getAvailableSchedule();

    Collection c = avail.getOverlappingScheduleElements(start.getTime(), end.getTime());
    return c.isEmpty();
  }
  
  public TransportationNode otherEnd(TransportationNode end) {
    if (end == myOrigin) return myDestination;
    if (end == myDestination) return myOrigin;
    // Error Condition
    return null;
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    out.writeObject(myOrigin);
    out.writeObject(myDestination);
    out.writeObject(myName);
    out.writeObject(myNetwork);
  }

  private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
    in.defaultReadObject();
    myOrigin=(TransportationNode)in.readObject();
    myDestination=(TransportationNode)in.readObject();
    myName=(String)in.readObject();
    myNetwork=(TransportationNetwork)in.readObject();
  }
  // beaninfo support
  private static PropertyDescriptor properties[];
  static {
    try {
      properties = new PropertyDescriptor[5];
      properties[0] = new PropertyDescriptor("FacilityPG", TransportationLink.class, "getFacilityPG", null);
      properties[1] = new PropertyDescriptor("Origin", TransportationLink.class, "getOrigin", null);
      properties[2] = new PropertyDescriptor("Destination", TransportationLink.class, "getDestination", null);
      properties[3] = new PropertyDescriptor("Name", TransportationLink.class, "getName", null);
      properties[4] = new PropertyDescriptor("Network", TransportationLink.class, "getNetwork", null);
    } catch (IntrospectionException ie) {}
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    PropertyDescriptor[] pds = super.getPropertyDescriptors();
    PropertyDescriptor[] ps = new PropertyDescriptor[pds.length+5];
    System.arraycopy(pds, 0, ps, 0, pds.length);
    System.arraycopy(properties, 0, ps, pds.length, 5);
    return ps;
  }
}
