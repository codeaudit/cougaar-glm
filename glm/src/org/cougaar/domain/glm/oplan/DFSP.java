/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
package org.cougaar.domain.glm.oplan;

import org.cougaar.domain.planning.ldm.plan.Transferable;
import org.cougaar.core.society.OwnedUniqueObject;
import org.cougaar.util.XMLizable;
import org.cougaar.util.XMLize;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Enumeration;
import java.util.Vector;
import org.cougaar.domain.glm.plan.GeolocLocation;
import org.cougaar.domain.glm.plan.NewGeolocLocation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DFSP
  extends OwnedUniqueObject
  implements XMLizable, Transferable, Cloneable
{
  private String name;
  private GeolocLocation geoLoc;
	
  public DFSP() 
  {
  }
	
  public DFSP(String name)
  {
    this.name = unique(name);
  }
	
  public void setName(String name) 
  {
    this.name = unique(name);
	
  }//setname
	
  public void setGeoLoc(GeolocLocation geoLoc) 
  {
    this.geoLoc = geoLoc;
  }//addGeoLoc
	
  public GeolocLocation getGeoLoc()
  {
    return geoLoc;
  }

  public String getName() 
  {
    return name;
  }//getname
	
  public Object clone() {
    DFSP dfsp = new DFSP(name);
    dfsp.setUID(getUID());
    dfsp.setOwner(getOwner());
    dfsp.setGeoLoc((GeolocLocation)geoLoc.clone());	
    return dfsp;
  }

  public Element getXML(Document doc) {
    return XMLize.getPlanObjectXML(this,doc);
  }

  //dummy PropertyChangeSupport for the Jess Interpreter.
  protected transient PropertyChangeSupport pcs = new PropertyChangeSupport(this);

  public void addPropertyChangeListener(PropertyChangeListener pcl) {
    pcs.addPropertyChangeListener(pcl);
  }

  public void removePropertyChangeListener(PropertyChangeListener pcl)   {
    pcs.removePropertyChangeListener(pcl);
  }

  private boolean matches(Object a, Object b) {
    return (a==null)?(b==null):(a.equals(b));
  }

  public boolean same(Transferable t) {
    if (t instanceof DFSP) {
      DFSP pod = (DFSP) t;
      return matches(name, pod.getName()) &&
        matches(geoLoc, pod.getGeoLoc());
    } else {
      return false;
    }
  }
  public void setAll(Transferable t) {
    if (t instanceof DFSP) {
      DFSP pod = (DFSP) t;
      setUID(pod.getUID());
      setOwner(pod.getOwner());
      setName(pod.getName());
      setGeoLoc(pod.getGeoLoc());
    }
  }
  public static final String unique(String s) {
    return (s==null)?null:(s.intern());
  }

}
