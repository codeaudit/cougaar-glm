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
package org.cougaar.glm.ldm.oplan;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.cougaar.core.util.OwnedUniqueObject;
import org.cougaar.glm.ldm.plan.GeolocLocation;
import org.cougaar.planning.ldm.plan.Transferable;

public class DFSP
  extends OwnedUniqueObject
  implements Transferable, Cloneable
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
