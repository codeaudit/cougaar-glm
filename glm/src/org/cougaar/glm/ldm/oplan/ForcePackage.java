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
 
/** The ForcePackage is a LDM Object that contains temporary groupings of 
 * organizations.  The ForcePackage object includes items such 
 * as time phasing, organziational relationships, and a forcePackage ID.   
 * To initially create the ForcePackage objects, the OplanPlugin loads the 
 * specified oplan.xml file, parses the oplan file for operational information 
 * and publishes the ForcePackage objects to the Log Plan. 
 * The ForcePackages are initially created in the J3 cluster and then is 
 * transferred to other clusters by the Propagation Plugin. Subordinate 
 * clusters can subscribe 
 * to changes in the ForcePackage in order to react to changes accordingly.
 * Subordinate clusters should not modify ForcePacakge information.
 *
 *
 **/
 
package org.cougaar.glm.ldm.oplan;

import org.cougaar.planning.ldm.plan.Transferable;
import org.cougaar.core.util.OwnedUniqueObject;
import org.cougaar.core.util.UID;
import org.cougaar.core.util.XMLizable;
import org.cougaar.core.util.XMLize;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Enumeration;
import java.util.Vector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * ForcePackage
 **/
public class ForcePackage
  extends OwnedUniqueObject
  implements OplanContributor, Transferable, XMLizable, Cloneable
{
  private TimeSpan theTimeSpan;
  private String forcePackageID;
  private UID oplanUID;
 
  /**     

   * Constructor for the ForcePackage object. 
   */    	
  public ForcePackage(UID oplanUID)
  {
    this.oplanUID = oplanUID;
  }//ForcePackage

  /**     
   * Constructor for the Oplan object.
   * @param forcePkgID The force pacakge ID.
   */    	
  public ForcePackage(String forcePkgID, UID oplanUID) 
  {
    forcePackageID = unique(forcePkgID);
    this.oplanUID = oplanUID;	
  }//ForcePackage

  /**     
   * Sets the forcePacakge ID for the given ForcePacakge.
   * @param forcePackageID The ID for the ForcePackage.
   */    	
  public void setForcePackageId(String forcePackageID) 
  {
    this.forcePackageID = unique(forcePackageID);
  }// setForcePackageID

  /**     
   * Sets the time span for the given ForcePackage
   * @param timeSpan The timeSpan for the ForcePackage.
   */    
  public void setTimeSpan(TimeSpan theTimeSpan) 
  {
    this.theTimeSpan = theTimeSpan;
  }//setTimeSpan

  /**     
   * Gets the forcePacakge ID for the given ForcePacakge.
   * @return forcePackageID The ID for the ForcePackage.
   **/    	
  public String getForcePackageId() 
  {
    return forcePackageID;
  }//getForcePackageId

  /**     
   * Gets the time span for the given ForcePackage
   * @return timeSpan The timeSpan for the ForcePackage.
   */   	
  public TimeSpan getTimeSpan() 
  {
    return theTimeSpan;
  }//getTimeSpan
	
  /** @deprecated Use getOplanUID */
  public UID getOplanID()
  {	    
    return (oplanUID); 
  }

  public UID getOplanUID()
  {	    
    return (oplanUID); 
  }

  /** Clones the ForcePackage object
   * @return forcePackage A Copy of the forcePackage
   */
  public Object clone() {
    ForcePackage fp = new ForcePackage(oplanUID);
    fp.setUID(getUID());
    fp.setOwner(getOwner());
    if (theTimeSpan != null)
      fp.setTimeSpan((TimeSpan)theTimeSpan.clone());
    fp.setForcePackageId(forcePackageID);
    return fp;
  }//clone

  /** This method sets the contents of ForcePackage to the values
   * in the given ForcePackage object.
   * @param other ForcePackage object
   */	  
  public void setAll(Transferable other) 
  {
    if (!(other instanceof ForcePackage))
      throw new IllegalArgumentException("Parameter not ForcePackage");
    
    ForcePackage otherFP = (ForcePackage)other;
    setUID(otherFP.getUID());
    setOwner(otherFP.getOwner());
    theTimeSpan = otherFP.getTimeSpan();
    oplanUID = otherFP.getOplanUID();
    forcePackageID = otherFP.getForcePackageId();
  }// setAll

  /** This method determines whether forcepacakges contain the
   * same values.
   * @param other ForcePackage object
   * @return boolean true - same
   *			false - not the same.
   */	  
  public boolean same(Transferable other) 
  {
    if (!(other instanceof ForcePackage))
      return false;
    ForcePackage otherFP = (ForcePackage)other;
    if (forcePackageID.equals(otherFP.getForcePackageId()))
      return true;
    return false;
  }//same

  private boolean matches(Object a, Object b) {
    return (a==null)?(b==null):(a.equals(b));
  }

  // full equals implementation, though we'll probably only
  // use it for the timespan test
  public boolean equals(Object other) {
    if (other instanceof ForcePackage) {
      ForcePackage x = (ForcePackage) other;
      return 
        matches(getTimeSpan(),x.getTimeSpan()) &&
        matches(forcePackageID, x.forcePackageID) &&
        matches(getOplanUID(), x.getOplanUID());
    }
    return false;
  }

  // XMLizable method for UI, other clients
  //
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
  public static final String unique(String s) {
    return (s==null)?null:(s.intern());
  }

}// ForcePackage
