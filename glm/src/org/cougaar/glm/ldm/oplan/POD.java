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
import org.cougaar.planning.ldm.plan.Transferable;

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
