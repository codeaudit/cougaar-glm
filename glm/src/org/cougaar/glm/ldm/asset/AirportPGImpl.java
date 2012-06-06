/*
 * <copyright>
 *  
 *  Copyright 1997-2012 Raytheon BBN Technologies
 *  under partial sponsorship of the Defense Advanced Research Projects
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

/* @generated Wed Jun 06 08:28:58 EDT 2012 from alpprops.def - DO NOT HAND EDIT */
/** Implementation of AirportPG.
 *  @see AirportPG
 *  @see NewAirportPG
 **/

package org.cougaar.glm.ldm.asset;

import org.cougaar.planning.ldm.measure.*;
import org.cougaar.planning.ldm.asset.*;
import org.cougaar.planning.ldm.plan.*;
import java.util.*;

import  org.cougaar.glm.ldm.plan.*;
import org.cougaar.glm.ldm.oplan.*;
import org.cougaar.glm.ldm.policy.*;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.glm.execution.common.InventoryReport;


import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.beans.PropertyDescriptor;
import java.beans.IndexedPropertyDescriptor;

public class AirportPGImpl extends java.beans.SimpleBeanInfo
  implements NewAirportPG, Cloneable
{
  public AirportPGImpl() {
  }

  // Slots

  private Distance theMaximumRunwayLength;
  public Distance getMaximumRunwayLength(){ return theMaximumRunwayLength; }
  public void setMaximumRunwayLength(Distance maximum_runway_length) {
    theMaximumRunwayLength=maximum_runway_length;
  }
  private String theRunwayType;
  public String getRunwayType(){ return theRunwayType; }
  public void setRunwayType(String runway_type) {
    theRunwayType=runway_type;
  }
  private long theMaximumOnGround;
  public long getMaximumOnGround(){ return theMaximumOnGround; }
  public void setMaximumOnGround(long maximum_on_ground) {
    theMaximumOnGround=maximum_on_ground;
  }
  private Area theRampSpace;
  public Area getRampSpace(){ return theRampSpace; }
  public void setRampSpace(Area ramp_space) {
    theRampSpace=ramp_space;
  }
  private double theMaximumFlightRate;
  public double getMaximumFlightRate(){ return theMaximumFlightRate; }
  public void setMaximumFlightRate(double maximum_flight_rate) {
    theMaximumFlightRate=maximum_flight_rate;
  }
  private MassTransferRate theThroughput;
  public MassTransferRate getThroughput(){ return theThroughput; }
  public void setThroughput(MassTransferRate throughput) {
    theThroughput=throughput;
  }


  public AirportPGImpl(AirportPG original) {
    theMaximumRunwayLength = original.getMaximumRunwayLength();
    theRunwayType = original.getRunwayType();
    theMaximumOnGround = original.getMaximumOnGround();
    theRampSpace = original.getRampSpace();
    theMaximumFlightRate = original.getMaximumFlightRate();
    theThroughput = original.getThroughput();
  }

  public boolean equals(Object other) {

    if (!(other instanceof AirportPG)) {
      return false;
    }

    AirportPG otherAirportPG = (AirportPG) other;

    if (getMaximumRunwayLength() == null) {
      if (otherAirportPG.getMaximumRunwayLength() != null) {
        return false;
      }
    } else if (!(getMaximumRunwayLength().equals(otherAirportPG.getMaximumRunwayLength()))) {
      return false;
    }

    if (getRunwayType() == null) {
      if (otherAirportPG.getRunwayType() != null) {
        return false;
      }
    } else if (!(getRunwayType().equals(otherAirportPG.getRunwayType()))) {
      return false;
    }

    if (!(getMaximumOnGround() == otherAirportPG.getMaximumOnGround())) {
      return false;
    }

    if (getRampSpace() == null) {
      if (otherAirportPG.getRampSpace() != null) {
        return false;
      }
    } else if (!(getRampSpace().equals(otherAirportPG.getRampSpace()))) {
      return false;
    }

    if (!(getMaximumFlightRate() == otherAirportPG.getMaximumFlightRate())) {
      return false;
    }

    if (getThroughput() == null) {
      if (otherAirportPG.getThroughput() != null) {
        return false;
      }
    } else if (!(getThroughput().equals(otherAirportPG.getThroughput()))) {
      return false;
    }

    return true;
  }

  public boolean hasDataQuality() { return false; }
  public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return null; }

  // static inner extension class for real DataQuality Support
  public final static class DQ extends AirportPGImpl implements org.cougaar.planning.ldm.dq.NewHasDataQuality {
   public DQ() {
    super();
   }
   public DQ(AirportPG original) {
    super(original);
   }
   public Object clone() { return new DQ(this); }
   private transient org.cougaar.planning.ldm.dq.DataQuality _dq = null;
   public boolean hasDataQuality() { return (_dq!=null); }
   public org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return _dq; }
   public void setDataQuality(org.cougaar.planning.ldm.dq.DataQuality dq) { _dq=dq; }
   private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    if (out instanceof org.cougaar.core.persist.PersistenceOutputStream) out.writeObject(_dq);
   }
   private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
    in.defaultReadObject();
    if (in instanceof org.cougaar.core.persist.PersistenceInputStream) _dq=(org.cougaar.planning.ldm.dq.DataQuality)in.readObject();
   }
    
    private final static PropertyDescriptor properties[]=new PropertyDescriptor[1];
    static {
      try {
        properties[0]= new PropertyDescriptor("dataQuality", DQ.class, "getDataQuality", null);
      } catch (Exception e) { e.printStackTrace(); }
    }
    public PropertyDescriptor[] getPropertyDescriptors() {
      PropertyDescriptor[] pds = super.properties;
      PropertyDescriptor[] ps = new PropertyDescriptor[pds.length+properties.length];
      System.arraycopy(pds, 0, ps, 0, pds.length);
      System.arraycopy(properties, 0, ps, pds.length, properties.length);
      return ps;
    }
  }


  private transient AirportPG _locked = null;
  public PropertyGroup lock(Object key) {
    if (_locked == null)_locked = new _Locked(key);
    return _locked; }
  public PropertyGroup lock() { return lock(null); }
  public NewPropertyGroup unlock(Object key) { return this; }

  public Object clone() throws CloneNotSupportedException {
    return new AirportPGImpl(AirportPGImpl.this);
  }

  public PropertyGroup copy() {
    try {
      return (PropertyGroup) clone();
    } catch (CloneNotSupportedException cnse) { return null;}
  }

  public Class getPrimaryClass() {
    return primaryClass;
  }
  public String getAssetGetMethod() {
    return assetGetter;
  }
  public String getAssetSetMethod() {
    return assetSetter;
  }

  private final static PropertyDescriptor properties[] = new PropertyDescriptor[6];
  static {
    try {
      properties[0]= new PropertyDescriptor("maximum_runway_length", AirportPG.class, "getMaximumRunwayLength", null);
      properties[1]= new PropertyDescriptor("runway_type", AirportPG.class, "getRunwayType", null);
      properties[2]= new PropertyDescriptor("maximum_on_ground", AirportPG.class, "getMaximumOnGround", null);
      properties[3]= new PropertyDescriptor("ramp_space", AirportPG.class, "getRampSpace", null);
      properties[4]= new PropertyDescriptor("maximum_flight_rate", AirportPG.class, "getMaximumFlightRate", null);
      properties[5]= new PropertyDescriptor("throughput", AirportPG.class, "getThroughput", null);
    } catch (Exception e) { 
      org.cougaar.util.log.Logging.getLogger(AirportPG.class).error("Caught exception",e);
    }
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    return properties;
  }
  private final class _Locked extends java.beans.SimpleBeanInfo
    implements AirportPG, Cloneable, LockedPG
  {
    private transient Object theKey = null;
    _Locked(Object key) { 
      if (this.theKey == null) this.theKey = key;
    }  

    public _Locked() {}

    public PropertyGroup lock() { return this; }
    public PropertyGroup lock(Object o) { return this; }

    public NewPropertyGroup unlock(Object key) throws IllegalAccessException {
       if( theKey.equals(key) ) {
         return AirportPGImpl.this;
       } else {
         throw new IllegalAccessException("unlock: mismatched internal and provided keys!");
       }
    }

    public PropertyGroup copy() {
      try {
        return (PropertyGroup) clone();
      } catch (CloneNotSupportedException cnse) { return null;}
    }


    public Object clone() throws CloneNotSupportedException {
      return new AirportPGImpl(AirportPGImpl.this);
    }

    public boolean equals(Object object) { return AirportPGImpl.this.equals(object); }
    public Distance getMaximumRunwayLength() { return AirportPGImpl.this.getMaximumRunwayLength(); }
    public String getRunwayType() { return AirportPGImpl.this.getRunwayType(); }
    public long getMaximumOnGround() { return AirportPGImpl.this.getMaximumOnGround(); }
    public Area getRampSpace() { return AirportPGImpl.this.getRampSpace(); }
    public double getMaximumFlightRate() { return AirportPGImpl.this.getMaximumFlightRate(); }
    public MassTransferRate getThroughput() { return AirportPGImpl.this.getThroughput(); }
  public final boolean hasDataQuality() { return AirportPGImpl.this.hasDataQuality(); }
  public final org.cougaar.planning.ldm.dq.DataQuality getDataQuality() { return AirportPGImpl.this.getDataQuality(); }
    public Class getPrimaryClass() {
      return primaryClass;
    }
    public String getAssetGetMethod() {
      return assetGetter;
    }
    public String getAssetSetMethod() {
      return assetSetter;
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
      return properties;
    }

    public Class getIntrospectionClass() {
      return AirportPGImpl.class;
    }

  }

}
