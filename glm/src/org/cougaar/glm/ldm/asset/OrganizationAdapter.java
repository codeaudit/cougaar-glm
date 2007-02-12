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

/* hand generated! */

package org.cougaar.glm.ldm.asset;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.planning.ldm.asset.ClusterPG;
import org.cougaar.planning.ldm.asset.NewRelationshipPG;
import org.cougaar.planning.ldm.asset.RelationshipBG;
import org.cougaar.planning.ldm.plan.HasRelationships;
import org.cougaar.util.TimeSpan;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.Collection;


public abstract class OrganizationAdapter extends GLMAsset {


  public OrganizationAdapter() { 
    super();
  }

  protected OrganizationAdapter(OrganizationAdapter prototype) {
    super(prototype);
  }

  private transient MessageAddress cid = null;
  private transient boolean cidComputed = false;

  public MessageAddress getMessageAddress()  {
    synchronized (this) {
      if (cidComputed) return cid;

      ClusterPG cpg = getClusterPG();
      if (cpg != null)
        cid = cpg.getMessageAddress();
      cidComputed = true;
      return cid;
    }
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
      properties[0] = new PropertyDescriptor("MessageAddress", OrganizationAdapter.class, "getMessageAddress", null);
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
    NewRelationshipPG relationshipPG = (NewRelationshipPG) PropertyGroupFactory.newRelationshipPG();
    RelationshipBG bg = new RelationshipBG();
    bg.init(relationshipPG, (HasRelationships) this);
    setRelationshipPG(relationshipPG);
  }
}



















