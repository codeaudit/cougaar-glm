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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.cougaar.core.util.OwnedUniqueObject;
import org.cougaar.core.util.UID;
import org.cougaar.planning.ldm.plan.Transferable;

/**
 * ForcePackage
 **/
public class ForcePackage
  extends OwnedUniqueObject
  implements OplanContributor, Transferable, Cloneable
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
   * @param theTimeSpan The timeSpan for the ForcePackage.
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
