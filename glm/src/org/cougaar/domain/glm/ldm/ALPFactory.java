/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.ldm;

import java.util.*;

import org.cougaar.domain.planning.ldm.Factory;
import org.cougaar.domain.planning.ldm.RootFactory;
import org.cougaar.domain.planning.ldm.LDMServesPlugIn;
import org.cougaar.core.cluster.LogPlan;
import org.cougaar.core.cluster.ClusterServesLogicProvider;
import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.core.society.UniqueObject;
import org.cougaar.core.society.UID;
import org.cougaar.util.UnaryPredicate;

import org.cougaar.domain.planning.ldm.plan.*;
import org.cougaar.domain.planning.ldm.measure.*;
import org.cougaar.domain.planning.ldm.asset.*;
import org.cougaar.domain.glm.ldm.plan.*;
import org.cougaar.domain.glm.ldm.oplan.*;
import org.cougaar.domain.glm.ldm.policy.*;
import org.cougaar.domain.glm.ldm.asset.*;

/**
 * FGI Domain package definition.
 **/

public class ALPFactory implements org.cougaar.domain.planning.ldm.Factory {
  public ALPFactory(LDMServesPlugIn ldm) {
    RootFactory rf = ldm.getFactory();
    rf.addAssetFactory(new org.cougaar.domain.glm.ldm.asset.AssetFactory());
    rf.addPropertyGroupFactory(new org.cougaar.domain.glm.ldm.asset.PropertyGroupFactory());
  }

  //
  // factory methods for  ALP LDM objects
  // 

  /**@param c - Pass a valid string representation of the Capability
   * @return Capability
   * @see org.cougaar.domain.glm.ldm.plan.Capability for a list of valid values
   **/
  public static Capability getCapability(String c) {
    return new CapabilityImpl(c);
  }

  public static NewCapacityScheduleElement newCapacityScheduleElement() {
    return new CapacityScheduleElementImpl();
  }

  /** Create a capacity schedule. This schedule has a container of
   * CapacityScheduleElements.
   * @param Enumeration{CapacityScheduleElement}
   * @see org.cougaar.domain.glm.ldm.plan.CapacityScheduleElement
   **/
  public static NewSchedule newCapacitySchedule(Enumeration capacityElements) {
    Vector rse = new Vector();
    while (capacityElements.hasMoreElements()) {
      ScheduleElement se = (ScheduleElement) capacityElements.nextElement();
      if (se instanceof CapacityScheduleElement) {
        rse.addElement(se);
      } else {
        throw new IllegalArgumentException("Error in creating newCapacitySchedule - an element was passed in the Enumeration that was not a CapacityScheduleElement");
      }
    }
    ScheduleImpl s = new ScheduleImpl();
    s.setScheduleElementType(PlanScheduleElementType.CAPACITY);
    s.setScheduleElements(rse.elements());
    return s;
  }  

  /** Create a labor schedule.  LaborSchedules contain a schedule that 
   *  has QuantityScheduleElements and a schedule that has RateScheduleElements.  
   *  @param qtySchedule  The schedule with scheduleelementtype QUANTITY created by
   *  factory.newQuantitySchedule
   *  @param rateSchedule  The schedule with scheduleelementtype RATE created by
   *  factory.newRateSchedule
   *  @return LaborSchedule
   *  @see org.cougaar.domain.glm.ldm.plan.LaborSchedule
   *  @see org.cougaar.domain.glm.ldm.plan.QuantityScheduleElements
   *  @see org.cougaar.domain.glm.ldm.plan.RateScheduleElements
   **/

  public static LaborSchedule newLaborSchedule(Schedule qtySchedule, Schedule rateSchedule) {
    if ( qtySchedule != null &&
         rateSchedule !=null &&
         qtySchedule.getScheduleElementType().equals(PlanScheduleElementType.QUANTITY)  &&
         rateSchedule.getScheduleElementType().equals(PlanScheduleElementType.RATE) ) {
       LaborSchedule ls = new LaborScheduleImpl(qtySchedule, rateSchedule);
       return ls;
    } else {
      throw new IllegalArgumentException("Error in creating newLaborSchedule");
    }
  }
  public static NewCasRep newCasRep() {
     return new CasRepImpl();
  }

  public static NewCasRep newCasRepChangeInd() {
     return new CasRepChangeIndicatorImpl();
  }
  public static NewGeolocLocation newGeolocLocation() {
    return new GeolocLocationImpl();
  }
  public static NewIcaoLocation newIcaoLocation() {
    return new IcaoLocationImpl();
  }
  public static NewNamedPosition newNamedPosition() {
    return new NamedPositionImpl();
  }


  /** Build a new empty Position object with a Latitude and Longitude **/
  public static NewPosition newPosition() {
    return new PositionImpl();
  }

  /** Build a new Position object with a Latitude and Longitude
   * @param alatitude
   * @param alongitude
   * @return Location
   **/
  public static Location createPosition(Latitude alatitude, Longitude alongitude) {
    return new PositionImpl(alatitude, alongitude);
  }

  public static NewQuantityRangeScheduleElement newQuantityRangeScheduleElement() {
    return new QuantityRangeScheduleElementImpl();
  }
  public static NewQuantityScheduleElement newQuantityScheduleElement() {
    return new QuantityScheduleElementImpl();
  }
  /** Create a quantity schedule.  This schedule has a container of
   * QuantityScheduleElements.
   * @param qtyElements
   * @param scheduleType  Pass in they type of Schedule
   * @see org.cougaar.domain.planning.ldm.plan.ScheduleType
   * @see org.cougaar.domain.glm.ldm.plan.QuantityScheduleElement
   **/
  public static NewSchedule newQuantitySchedule(Enumeration qtyElements, String scheduleType) {
    Vector qtyse = new Vector();
    while (qtyElements.hasMoreElements()) {
      ScheduleElement se = (ScheduleElement) qtyElements.nextElement();
      if (se instanceof QuantityScheduleElement) {
        qtyse.addElement(se);
      } else {
        throw new IllegalArgumentException("Error in creating newQuantitySchedule - an element was passed in the Enumeration that was not a QuantityScheduleElement");
      }
    }
    ScheduleImpl s = new ScheduleImpl();
    s.setScheduleType(scheduleType);
    s.setScheduleElementType(PlanScheduleElementType.QUANTITY);
    s.setScheduleElements(qtyse.elements());

    return s;
  }

  /** Create a quantity range schedule.  This schedule has a container 
   * of QuantityRangeScheduleElements.
   * @param Enumeration{QuantityRangeScheduleElement}
   * @param scheduleType  Pass in the type of Schedule
   * @see org.cougaar.domain.planning.ldm.plan.ScheduleType
   * @see org.cougaar.domain.glm.ldm.plan.QuantityRangeScheduleElement
   **/
  public static NewSchedule newQuantityRangeSchedule(Enumeration qtyRangeElements, String scheduleType) {
    Vector qtyrse = new Vector();
    while (qtyRangeElements.hasMoreElements()) {
      ScheduleElement se = (ScheduleElement) qtyRangeElements.nextElement();
      if (se instanceof QuantityRangeScheduleElement) {
        qtyrse.addElement(se);
      } else {
        throw new IllegalArgumentException("Error in creating newQuantityRangeSchedule - an element was passed in the Enumeration that was not a QuantityRangeScheduleElement");
      }
    }
    ScheduleImpl s = new ScheduleImpl();
    s.setScheduleType(scheduleType);
    s.setScheduleElementType(PlanScheduleElementType.QUANTITYRANGE);
    s.setScheduleElements(qtyrse.elements());
    return s;
  }

  /** Create a rate schedule. This schedule has a container of
   * RateScheduleElements.
   * @param Enumeration{RateScheduleElement}
   * @see org.cougaar.domain.glm.ldm.plan.RateScheduleElement
   **/
  public static NewSchedule newRateSchedule(Enumeration rateElements) {
    Vector rse = new Vector();
    while (rateElements.hasMoreElements()) {
      ScheduleElement se = (ScheduleElement) rateElements.nextElement();
      if (se instanceof RateScheduleElement) {
        rse.addElement(se);
      } else {
        throw new IllegalArgumentException("Error in creating newRateSchedule - an element was passed in the Enumeration that was not a RateScheduleElement");
      }
    }
    ScheduleImpl s = new ScheduleImpl();
    s.setScheduleElementType(PlanScheduleElementType.RATE);
    s.setScheduleElements(rse.elements());
    return s;
  }

  public static NewRateScheduleElement newRateScheduleElement() {
    return new RateScheduleElementImpl();
  }

  public static DetailRequestAssignment newDetailRequestAssignment(DetailRequest request) {
    return new DetailRequestAssignment(request);
  }

  public static DetailReplyAssignment newDetailReplyAssignment(UniqueObject replyObj,
							       UID requestedUID,
							       ClusterIdentifier replyFrom,
							       ClusterIdentifier replyTo) {
    return new DetailReplyAssignment(replyObj, requestedUID, replyFrom, replyTo);
  }

  public static DetailRequest newDetailRequest(UID desiredObject,
					       ClusterIdentifier sourceCluster,
					       ClusterIdentifier requestingCluster) {
    return new DetailRequestImpl(desiredObject,
				 sourceCluster,
				 requestingCluster);
  }


  public static QueryRequestAssignment newQueryRequestAssignment(QueryRequest request) {
    return new QueryRequestAssignment(request);
  }

  public static QueryReplyAssignment newQueryReplyAssignment(Collection reply,
							       UnaryPredicate requestQuery,
							       ClusterIdentifier replyFrom,
							       ClusterIdentifier replyTo) {
    return new QueryReplyAssignment(reply, requestQuery, replyFrom, replyTo);
  }

  public static QueryRequest newQueryRequest(UnaryPredicate queryPredicate,
					       ClusterIdentifier sourceCluster,
					       ClusterIdentifier requestingCluster) {
    return new QueryRequestImpl(queryPredicate,
				 sourceCluster,
				 requestingCluster);
  }


}
  
