/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
package org.cougaar.domain.glm.ldm.oplan;

import org.cougaar.domain.planning.ldm.plan.Transferable;
import org.cougaar.core.society.OwnedUniqueObject;
import org.cougaar.core.util.XMLizable;
import org.cougaar.core.util.XMLize;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.cougaar.domain.glm.ldm.plan.GeolocLocation;
import org.cougaar.domain.glm.ldm.plan.NewGeolocLocation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class POD
  extends OwnedUniqueObject
  implements XMLizable, Transferable, Cloneable
{
   private String type;
   private String value;

   public void setType(String type)
   {
      this.type = unique(type);
   }//setType

   public void setValue(String value)
   {
      this.value = unique(value);
   }//setValue

   public String getType()
   {
      return type;
   }//getType

   public String getValue()
   {
      return value;
   }//getValue

   public Object clone() 
   {
      POD pod = new POD();
      pod.setUID(getUID());
      pod.setOwner(getOwner());
      pod.setType(type);
      pod.setValue(value);

      return pod;
   }// clone

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
    if (t instanceof POD) {
      POD pod = (POD) t;
      return matches(type, pod.getType()) &&
        matches(value, pod.getValue());
    } else {
      return false;
    }
  }
  public void setAll(Transferable t) {
    if (t instanceof POD) {
      POD pod = (POD) t;
      setUID(pod.getUID());
      setOwner(pod.getOwner());
      setType(pod.getType());
      setValue(pod.getValue());
    }
  }
  public static final String unique(String s) {
    return (s==null)?null:(s.intern());
  }

}// POD
