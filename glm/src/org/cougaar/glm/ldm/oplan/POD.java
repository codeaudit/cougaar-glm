/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
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
package org.cougaar.glm.ldm.oplan;

import org.cougaar.planning.ldm.plan.Transferable;
import org.cougaar.core.util.OwnedUniqueObject;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.cougaar.glm.ldm.plan.GeolocLocation;
import org.cougaar.glm.ldm.plan.NewGeolocLocation;

public class POD
  extends OwnedUniqueObject
  implements Transferable, Cloneable
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
