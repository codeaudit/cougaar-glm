/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

/* hand generated! */

package org.cougaar.domain.glm.ldm.asset;

import java.beans.PropertyDescriptor;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

import java.util.Collection;

import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.domain.planning.ldm.asset.ClusterPG;
import org.cougaar.domain.planning.ldm.asset.RelationshipBG;
import org.cougaar.domain.planning.ldm.asset.NewRelationshipPG;
import org.cougaar.domain.planning.ldm.asset.RelationshipPGImpl;

import org.cougaar.domain.planning.ldm.plan.HasRelationships;
import org.cougaar.domain.planning.ldm.plan.RelationshipImpl;
import org.cougaar.domain.planning.ldm.plan.RelationshipSchedule;
import org.cougaar.domain.planning.ldm.plan.RelationshipScheduleImpl;
import org.cougaar.domain.planning.ldm.plan.Role;
import org.cougaar.util.TimeSpan;

import org.cougaar.domain.glm.ldm.Constants;


public abstract class OrganizationAdapter extends GLMAsset {


  public OrganizationAdapter() { 
    super();
  }

  protected OrganizationAdapter(OrganizationAdapter prototype) {
    super(prototype);
  }

  private transient ClusterIdentifier cid = null;
  private transient boolean cidComputed = false;

  public ClusterIdentifier getClusterIdentifier()  {
    synchronized (this) {
      if (cidComputed) return cid;

      ClusterPG cpg = getClusterPG();
      if (cpg != null)
        cid = cpg.getClusterIdentifier();
      cidComputed = true;
      return cid;
    }
  }
  
  public boolean isSelf() {
    return getRelationshipPG().getRelationshipSchedule().getMatchingRelationships(Constants.Role.SELF).size() > 0;
  }

  /**
   * getSuperiors - returns superior relationships.
   * Performs a 2 stage search, returns all SUPERIOR relationships if they 
   * exist, if none exist returns all ADMINISTRATIVESUPERIOR relationships.
   */
  public Collection getSuperiors(long startTime, long endTime) {
    Collection superiors = 
      getRelationshipPG().getRelationshipSchedule().getMatchingRelationships(Constants.Role.SUPERIOR,
                                                    startTime,
                                                    endTime);

    if (superiors.isEmpty()) {
      superiors = 
        getRelationshipPG().getRelationshipSchedule().getMatchingRelationships(Constants.Role.ADMINISTRATIVESUPERIOR,
                                                      startTime,
                                                      endTime);
    }

    return superiors;
  }

  public Collection getSuperiors(TimeSpan timeSpan) {
    return getSuperiors(timeSpan.getStartTime(), timeSpan.getEndTime());
  }

  /**
   * getSubordinates - returns subordinate relationships.
   * Performs a 2 stage search, returns all SUBORDINATE relationships if they 
   * exist, if none exist returns all ADMINISTRATIVESUBORDINATE relationships.
   */
  public Collection getSubordinates(long startTime, long endTime) {
    Collection subordinates = 
      getRelationshipPG().getRelationshipSchedule().getMatchingRelationships(Constants.Role.SUBORDINATE,
                                                    startTime, endTime);

    if (subordinates.isEmpty()) {
      subordinates = 
        getRelationshipPG().getRelationshipSchedule().getMatchingRelationships(Constants.Role.ADMINISTRATIVESUBORDINATE,
                                                      startTime,
                                                      endTime);
    }
    return subordinates;
  }

  public Collection getSubordinates(TimeSpan timeSpan) {
    return getSubordinates(timeSpan.getStartTime(), timeSpan.getEndTime());
  }

  private static PropertyDescriptor properties[];
  static {
    try {
      properties = new PropertyDescriptor[1];
      properties[0] = new PropertyDescriptor("ClusterIdentifier", OrganizationAdapter.class, "getClusterIdentifier", null);
    } catch (IntrospectionException ie) {}
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    PropertyDescriptor[] pds = super.getPropertyDescriptors();
    PropertyDescriptor[] ps = 
      new PropertyDescriptor[pds.length + properties.length];
    System.arraycopy(pds, 0, ps, 0, pds.length);
    System.arraycopy(properties, 0, ps, pds.length, properties.length);
    return ps;
  }

  public Object clone() throws CloneNotSupportedException {
    OrganizationAdapter clone = (OrganizationAdapter) super.clone();
    clone.initRelationshipSchedule();
    return clone;
  }

  public void initRelationshipSchedule() {
    NewRelationshipPG relationshipPG = 
      (NewRelationshipPG) PropertyGroupFactory.newRelationshipPG();
    relationshipPG.setRelationshipBG(new RelationshipBG(relationshipPG, (HasRelationships) this));
    setRelationshipPG(relationshipPG);
  }
}



















