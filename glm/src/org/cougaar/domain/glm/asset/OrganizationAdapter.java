/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

/* hand generated! */

package org.cougaar.domain.glm.asset;

import java.beans.PropertyDescriptor;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

import java.util.Collection;

import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.domain.planning.ldm.asset.ClusterPG;
import org.cougaar.domain.planning.ldm.plan.HasRelationships;
import org.cougaar.domain.planning.ldm.plan.RelationshipImpl;
import org.cougaar.domain.planning.ldm.plan.RelationshipSchedule;
import org.cougaar.domain.planning.ldm.plan.RelationshipScheduleImpl;
import org.cougaar.domain.planning.ldm.plan.Role;
import org.cougaar.util.TimeSpan;

import org.cougaar.domain.glm.Constants;

public abstract class OrganizationAdapter extends ALPAsset
  implements HasRelationships {

  public OrganizationAdapter() { 
    super();
    initRelationshipSchedule();
  }

  protected OrganizationAdapter(OrganizationAdapter prototype) {
    super(prototype);
    initRelationshipSchedule();
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
  
  private transient RelationshipSchedule relationshipSchedule;

  public boolean hasRelationshipSchedule() {
    return true;
  }

  public RelationshipSchedule getRelationshipSchedule()  {
    return relationshipSchedule;
  }

  public void setRelationshipSchedule(RelationshipSchedule schedule)  {
    if (!schedule.getHasRelationships().equals(this)) {
      throw new IllegalArgumentException("Attempt to use RelationshipSchedule for a different asset.");
    }
    relationshipSchedule = schedule;
  }

  public boolean isSelf() {
    return relationshipSchedule.getMatchingRelationships(Constants.Role.SELF).size() > 0;
  }

  public Collection getSuperiors(long startTime, long endTime) {
    Collection superiors = 
      relationshipSchedule.getMatchingRelationships(Constants.Role.SUPERIOR,
                                                    startTime,
                                                    endTime);

    if (superiors.isEmpty()) {
      superiors = 
        relationshipSchedule.getMatchingRelationships(Constants.Role.ADMINISTRATIVESUPERIOR,
                                                      startTime,
                                                      endTime);
    }

    return superiors;
  }

  public Collection getSuperiors(TimeSpan timeSpan) {
    return getSuperiors(timeSpan.getStartTime(), timeSpan.getEndTime());
  }

  public Collection getSubordinates(long startTime, long endTime) {
    Collection subordinates = 
      relationshipSchedule.getMatchingRelationships(Constants.Role.SUBORDINATE,
                                                    startTime, endTime);

    if (subordinates.isEmpty()) {
      subordinates = 
        relationshipSchedule.getMatchingRelationships(Constants.Role.ADMINISTRATIVESUBORDINATE,
                                                      startTime,
                                                      endTime);
    }
    return subordinates;
  }

  public Collection getSubordinates(TimeSpan timeSpan) {
    return getSubordinates(timeSpan.getStartTime(), timeSpan.getEndTime());
  }

  private void initRelationshipSchedule() {
    relationshipSchedule = new RelationshipScheduleImpl(this);
  }

  private static PropertyDescriptor properties[];
  static {
    try {
      properties = new PropertyDescriptor[2];
      properties[0] = new PropertyDescriptor("relationshipSchedule", OrganizationAdapter.class, "getRelationshipSchedule", null);
      properties[1] = new PropertyDescriptor("ClusterIdentifier", OrganizationAdapter.class, "getClusterIdentifier", null);
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


  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    if (out instanceof org.cougaar.core.cluster.persist.PersistenceOutputStream) {
      out.writeObject(relationshipSchedule);
    }
  }

  
  private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
    in.defaultReadObject();
    if (in instanceof org.cougaar.core.cluster.persist.PersistenceInputStream) {
      relationshipSchedule = (RelationshipSchedule)in.readObject();
    } else {      
      initRelationshipSchedule();
    }
  }       
}



