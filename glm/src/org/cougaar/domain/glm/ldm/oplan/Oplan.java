/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
/** The Oplan is a LDM Object that articulates operation plan
 * information to clusters.  The Oplan object includes items such as
 * time phasing, priority, and cday information along with an oplan ID
 * and references to other oplan objects.  To initially create the
 * Oplan object, the OplanPlugIn loads the specified oplan.xml file,
 * parses the oplan file for operational information and publishes the
 * Oplan object to the Log Plan. The Oplan is initially created in the
 * J3 (Joint Operational Officer) cluster and then is transferred to
 * other clusters by the Propagation Plugin. Subordinate clusters can
 * subscribe to changes in the Oplan in order to retrieve oplan
 * information and to react to changes accordingly.  Subordinate
 * clusters should not modify (set) ForcePacakge information.
 *
 *
 **/
	
package org.cougaar.domain.glm.ldm.oplan;

import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.Vector;

import org.cougaar.core.cluster.Subscriber;
import org.cougaar.domain.planning.ldm.plan.Transferable;

import org.cougaar.core.society.UID;
import org.cougaar.core.society.UniqueObject;
import org.cougaar.core.society.OwnedUniqueObject;
import org.cougaar.core.util.XMLizable;
import org.cougaar.core.util.XMLize;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeEvent;
import java.io.Serializable;

/**
 * Oplan
 **/
public class Oplan extends OwnedUniqueObject
  implements Transferable, XMLizable, Serializable, Cloneable, UniqueObject
{
    
  private String oplanID;
  private String opName;
  private String priority;
  private Date cDay;
  private Date endDay;          // End day of oplan
  private String xmlfilename_ ;
  private double version = 1.0;
    
  private String theaterID;
  private String terrainType;
  private String season;
  private String enemyForceType;
  private boolean hnsPOL;
  private String hnsPOLCap;
  private String hnsWaterCap;
  private boolean hnsWater;
  private Vector pods = new Vector();
  private Vector dfspVector = new Vector();
	
  // Priority values
  public static final String HIGH = "High";
  public static final String MEDIUM = "Medium";
  public static final String LOW = "Low";

  // Default oplan duration
  public static final long DEFAULT_OPLAN_DURATION = 180 * 86400000L;// 180 days
  public static final long INFER_END_DAY_FUDGE    =  30 * 86400000L;// 30 days

  // Theater values
  public static final String SOUTH_WEST_ASIA = "SWA";
		
  // Terrain values
  public static final String DESERT = "Desert";
		
  // Enemy values
  public static final String REGULAR = "Regular Forces";	
	
  // Season values
  public static final String SPRING = "Spring";
  public static final String SUMMER = "Summer";
  public static final String AUTUMN = "Autumn";
  public static final String FALL = "Autumn";
  public static final String WINTER = "Winter";

  public static final String DEFAULT_FILE_NAME = "oplan.xml";
	
  /**     
   * Constructor for the Oplan object.
   * 
   */    	
  public Oplan() 
  {
    // empty oplan
  }// Oplan
	
  /**     
   * Constructor for the Oplan object.  This should only be used by
   * the OPlanPlugIn.
   * @param xmlfilename Name of the oplan xml file to be parsed.
   * 
   */    	
  public Oplan( String xmlfilename )
  {
    setXMLFileName( xmlfilename );
    /*
    System.out.println("<<<OPlanPlugIn>>> The XML file name is: " 
                       + getXMLFileName() );
    */
  } //Oplan
	
  /**     
   * Constructor for the Oplan object.  This should only be used by
   * the OPlanPlugIn.
   * 
   */  	
  public Oplan(UID uid, 
	       String oplanID,
               String opName, 
               String priority, 
               Date cDay)
  {
    this(uid, oplanID, opName, priority, cDay, null);
  }

  public Oplan(UID uid, 
	       String oplanID,
               String opName, 
               String priority, 
               Date cDay,
               Date endDay)
  {
    setUID(uid);
    this.oplanID = unique(oplanID);
    this.opName = unique(opName);
    this.priority = unique(priority);
    this.cDay = cDay;
    if (endDay == null) {
      this.endDay = getDefaultEndDay();
    } else {
      this.endDay = endDay;
    }
  }//Oplan

  /**     
   * Constructor for the Oplan object.  This should only be used by
   * the OPlanPlugIn.
   * @param xmlfilename Name of the oplan xml file to be parsed.
   * 
   */  
  public Oplan(String xmlfilename,
               UID uid, 
	       String oplanID,
               String opName, 
               String priority, 
               Date cDay)
  {
    this(xmlfilename, uid, oplanID, opName, priority, cDay, null);
  }
                                                                     
  public Oplan(String xmlfilename,
               UID uid, 
	       String oplanID,
               String opName, 
               String priority, 
               Date cDay,
               Date endDay)
  {
    this (uid, oplanID, opName, priority, cDay, endDay);
    setXMLFileName(xmlfilename);
  }//Oplan
  
  // pods
  public void addPOD(POD pod) 
  {
    pods.addElement(pod); 
  }//setAPOE
	
  Vector getPODsV() { return pods; }
  public Enumeration getPODs() 
  {
    return pods.elements();
  }// getPOD

  public POD[] getPODArray() 
  {
    POD[] tmp = new POD[pods.size()];
    return (POD[])pods.toArray(tmp);
  }//getDFSP

  /**     
   * Sets the theaterID
   * @param theaterID See constants above for valid values.
   */	
  public void setTheaterID(String theaterID) 
  {
    this.theaterID = unique(theaterID);
  }
	
  /**     
   * Sets the terraintype
   * @param terrainType See constants above for valid values.
   */	
  public void setTerrainType(String terrainType) 
  {
    this.terrainType = unique(terrainType);
  }
	
  /**     
   * Sets the season
   * @param season See constants above for valid values.
   */		
  public void setSeason(String season) 
  {
    this.season = unique(season);	    
  }
	
  /**     
   * Sets the enemyForceType
   * @param forceType See constants above for valid values.
   */		
  public void setEnemyForceType(String enemyForceType) 
  {
    this.enemyForceType = unique(enemyForceType); 
}
	
  public void setHNSPOL(boolean hnsPOL) 
  {
    this.hnsPOL = hnsPOL;    
  }
	

  public void setHNSPOLCapacity(String hnsPOLCap) 
  {
    this.hnsPOLCap = unique(hnsPOLCap);	    
  }
	

  public void setHNSForWater(boolean hnsWater) 
  {
    this.hnsWater = hnsWater;    
  }
	

  public void setHNSWaterCapability(String hnsWaterCap) 
  {
    this.hnsWaterCap = unique(hnsWaterCap);    
  }
	
  public void addDFSP(DFSP dfsp)
  {
    dfspVector.addElement(dfsp);
  }//addDFSP
	
  Vector getDFSPsV() { return dfspVector; }

  public Enumeration getDFSPs()
  {
    return dfspVector.elements();
  }//getDFSP

  public DFSP[] getDFSPArray() 
  {
    DFSP[] tmp = new DFSP[dfspVector.size()];
    return (DFSP[])dfspVector.toArray(tmp);
  }//getDFSP
	

  public String getTheaterID() 
  {
    return (theaterID);
  }
	
	
  public String getTerrainType() 
  {
    return (terrainType);
  }
	
	
  public String getSeason() 
  {
    return (season);
  }
	
	
  public String getEnemyForceType() 
  {
    return (enemyForceType);
  }
	
	
  public boolean getHNSPOL() 
  {
    return (hnsPOL);
  }		
	
  public String getHNSPOLCapacity() 
  {
    return (hnsPOLCap);
  }
	
	
  public boolean getHNSForWater() 
  {
    return (hnsWater);
  }
	
	
  public String getHNSWaterCapability() 

  {
    return (hnsWaterCap);
  }

  /**     
   * Sets the oplan xml filename to be parsed.
   * @param name Name of the oplan xml file to be parsed.
   * 
   */   
  public void setXMLFileName( String name )
  {
    if (name == null) 
      this.xmlfilename_ = DEFAULT_FILE_NAME;
    else 
      this.xmlfilename_ = unique(name);
  }
  /**     
   * Gets the oplan xml filename to be parsed.
   * @return The name of the most recently parsed oplan xml file.
   * 
   */ 		
  public String getXMLFileName()
  {
    return this.xmlfilename_;
  }
  /**     
   * Sets the oplan ID.  This should not be called by any
   * subordinate clusters.
   * @param oplanID The id of the Oplan.
   */ 	       	
  public void setOplanId(String oplanID) 
  {
    this.oplanID = unique(oplanID);
  }//setOplanId

  /**     
   * Sets the oplan UID.  This should not be called by any
   * subordinate clusters.
   * @param UID The unique id of the Oplan.
   * 
   */ 	       	
    public void setOplanUID(UID uid) 
    {
      setUID(uid);
    }//setOplanUID
    
  /**     
   * Sets the operation name of the oplan.  This should 
   * not be called by any subordinate clusters.
   * @param opName The operation name.
   * 
   */ 		
  public void setOperationName(String opName) 
  {
    this.opName = unique(opName);
  }// setOperationName
	
  /**     
   * Sets the priority of the oplan.  This should 
   * not be called by any subordinate clusters. Static
   * final values will be created for the priority in
   * the near future.
   * @param priority The priority.
   * 
   */ 				
  public void setPriority(String priority) 
  {
    this.priority = unique(priority);	
  }//setPriority
  /**     
   * Sets the CDay of the oplan.  This should 
   * not be called by any subordinate clusters. 
   * @param cDay the CDay.  Should be in the format mm/dd/yyyy
   */
  public void setCday(Date cDay) 
  {
    this.cDay = cDay;	
  }// setCday
      
  /**     
   * Sets the end day of the oplan.  This should 
   * not be called by any subordinate clusters. 
   * @param endDay the endDay.  Should be in the format mm/dd/yyyy
   */
  public void setEndDay(Date endDay) 
  {
    this.endDay = endDay;	
  }// setCday

  /**
   * Infer the end day from all the contributing elements
   **/
  public void inferEndDay(Collection oplanContributors) {
    long maxET = Long.MIN_VALUE;
    for (Iterator iterator = oplanContributors.iterator(); 
         iterator.hasNext(); ) {
      Object e = iterator.next();
      if (e instanceof OplanContributor) {
        OplanContributor contrib = (OplanContributor) e;
        TimeSpan span = contrib.getTimeSpan();
        if (span != null) {
          long et = span.getEndTime();
          if (et != span.MAX_VALUE && et > maxET) maxET = et;
        }
      }
    }
    
    if (maxET != Long.MIN_VALUE) {
      setEndDay(new Date(maxET + INFER_END_DAY_FUDGE));
    } else {
      setEndDay(getDefaultEndDay());
    }
  }

  public void incrementVersion()
  {
    version = version + 0.00001;
  }// incrementVersion
   
  public double getVersion()
  {
    return (version);
  }//getVersion;   

  /**     
   * Gets the oplanID for the given OPlan. 
   * @return oplanID The unique ID for the Oplan.
   */
  public String getOplanId() 
  {
    return oplanID;
  }//getOplanId

  /**     
   * Gets the oplanID for the given OPlan. 
   * @return oplanID The unique ID for the Oplan.
   */
  public UID getUID() 
  {
    return super.getUID();
  }//getUID

  /**     
   * Gets the operation name for the Oplan.
   * @return oplanName The operation name for the Oplan.
   */		
  public String getOperationName() 
  {
    return (opName);
  }// getOperationName

  /**     
   * Gets the priority for the OPlan.
   * @return priority The priority for the Oplan.  The types
   * returned are declared as 
   */		
  public String getPriority() 
  {
    return (priority);
  }// getPriority

  /**     
   * Gets the current Cday being used by the Oplan
   * @return Date The current cDay
   */			
  public Date getCday() 
  {
    return (cDay);
  }// getCday
   		
  /**     
   * Gets the current end day being used by the Oplan
   * @return Date The current endDay
   */			
  public Date getEndDay() 
  {
    return (endDay);
  }// getEndDay

  private Date getDefaultEndDay() {
    return new Date(cDay.getTime() + DEFAULT_OPLAN_DURATION);
  }

  /**     
   * Returns a copy of the Oplan.
   * @return Object A copy of the Oplan.  
   */    		
  public Object clone() {
    Oplan newOplan = new Oplan(getUID(), oplanID, opName, priority, cDay, endDay);
    newOplan.setOwner(getOwner());
    newOplan.setTheaterID(theaterID);
    newOplan.setTerrainType(terrainType);
    newOplan.setSeason(season);
    newOplan.setEnemyForceType(enemyForceType);
    newOplan.setHNSPOL(hnsPOL);
    newOplan.setHNSPOLCapacity(hnsPOLCap);
    newOplan.setHNSWaterCapability(hnsWaterCap);
    newOplan.setHNSForWater(hnsWater);
    copyVectorInto(getPODsV(), newOplan.getPODsV());
    copyVectorInto(getDFSPsV(), newOplan.getDFSPsV());
    newOplan.setXMLFileName(xmlfilename_);
    return newOplan;
  }//clone

  // also clones the next level down or we end up sharing structure
  // after all that!
  private void copyVectorInto(Vector v, Vector nv) {
    nv.clear();
    nv.ensureCapacity(v.size());
    for (Enumeration e = v.elements(); e.hasMoreElements(); ) {
      nv.addElement(((Transferable)e.nextElement()).clone());
    }
  }

  /**     
   * Determines if the given oplan is the same as
   * the current oplan.
   * @param other Oplan to compare it to.
   * @return boolean true - same
   *				   false - not same
   */    
  public boolean same(Transferable other) {
    if (!(other instanceof Oplan)) return false;
    Oplan otherOplan = (Oplan)other;
    return getUID().equals(otherOplan.getUID());
  }//same


  /**     
   * Determines if the given oplan is the same as
   * the current oplan.
   * @param other Oplan to compare it to.
   * @return boolean true - same
   *				   false - not same
   */    
  public boolean equals(Object o) {
    if (o instanceof Oplan) {
      Oplan oplan = (Oplan) o;

      return
        matches(getOplanId(), oplan.getOplanId()) &&
        matches(getOperationName(), oplan.getOperationName()) &&
        matches(getPriority(), oplan.getPriority()) &&
        matches(getCday(), oplan.getCday()) &&
        matches(getEndDay(), oplan.getEndDay()) &&
        matches(getTheaterID(), oplan.getTheaterID()) &&
        matches(getTerrainType(), oplan.getTerrainType()) &&
        matches(getSeason(), oplan.getSeason()) &&
        matches(getEnemyForceType(), oplan.getEnemyForceType()) &&
        (getHNSPOL() == oplan.getHNSPOL()) &&
        matches(getHNSPOLCapacity(), oplan.getHNSPOLCapacity()) &&
        (getHNSForWater() == oplan.getHNSForWater()) &&
        matches(getHNSWaterCapability(), oplan.getHNSWaterCapability()) &&
        matches(getPODsV(), oplan.getPODsV()) &&
        matches(getDFSPsV(), oplan.getDFSPsV());
    } else
      return false;
  }
  
  /**     
   * This methods sets the Oplan contents to all
   * of the values specified in the given object.
   * @param other oplan object to set contents to.
   */    
  public void setAll(Transferable other) {
    if (!(other instanceof Oplan))
      throw new IllegalArgumentException("Parameter not Oplan");

    // assume oplanID and opName don't change
    Oplan otherOplan = (Oplan)other;
    setUID(otherOplan.getUID());
    setOwner(otherOplan.getOwner());
    theaterID = otherOplan.getTheaterID();
    terrainType = otherOplan.getTerrainType();
    season = otherOplan.getSeason();
    enemyForceType = otherOplan.getEnemyForceType();
    hnsPOL = otherOplan.getHNSPOL();
    hnsPOLCap = otherOplan.getHNSPOLCapacity();
    hnsWaterCap = otherOplan.getHNSWaterCapability();
    hnsWater = otherOplan.getHNSForWater();

    pods = new Vector();
    dfspVector = new Vector();

    Enumeration en = otherOplan.getDFSPs();
    while ( en.hasMoreElements() )
      dfspVector.addElement( en.nextElement() );

    en = otherOplan.getPODs();
    while ( en.hasMoreElements() )
      pods.addElement( en.nextElement() );

    priority = otherOplan.getPriority();
    cDay = otherOplan.getCday();
    endDay = otherOplan.getEndDay();
    xmlfilename_ = otherOplan.getXMLFileName();
  }// setAll

  // 
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

  /** @return A string Containing the UID and operation name of the Oplan */
  public String toString() {
    return getUID().toString() + " " + opName;
  }
  public static final String unique(String s) {
    return (s==null)?null:(s.intern());
  }

  private static boolean matches(Object a, Object b) {
    if (!(a instanceof Enumeration) ||
        !(b instanceof Enumeration)) {
      return (a==null)?(b==null):(a.equals(b));
    } else {
      Enumeration aEnum = (Enumeration) a;
      Enumeration bEnum = (Enumeration) b;

      while (aEnum.hasMoreElements() && bEnum.hasMoreElements()) {
        if (!matches(aEnum.nextElement(), bEnum.nextElement())) {
          return false;
        }
      }

      return  (!aEnum.hasMoreElements()) && (!bEnum.hasMoreElements());
    }
  }
}// OPlan











