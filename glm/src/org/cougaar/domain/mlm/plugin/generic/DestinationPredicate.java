/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.plugin.generic;

import java.util.Collection;

import org.cougaar.core.cluster.ClusterIdentifier;

import org.cougaar.domain.planning.ldm.asset.Asset;

import org.cougaar.domain.planning.ldm.plan.RelationshipSchedule;
import org.cougaar.domain.planning.ldm.plan.Role;

import org.cougaar.util.TimeSpan;
import org.cougaar.util.UnaryPredicate;

import org.cougaar.domain.glm.ldm.Constants;
import org.cougaar.domain.glm.ldm.asset.Organization;

/**
 * DestinationPredicate - Unary Predicate describing the 
 * destination cluster of an Oplan or Policy that is being forwarded
 *
 */

public class DestinationPredicate implements UnaryPredicate {
  protected String roles[];

  protected ClusterIdentifier clusterId;


  DestinationPredicate(String [] roles, ClusterIdentifier clusterId) {
    this.roles = roles;
    this.clusterId = clusterId;

    if (roles == null)
      roles =  new String[0];
  }

  public String[] getRoles() {
    return roles;
  }

  public int hashCode() {
    String hashstring = "";
    for (int i=0; i<roles.length; i++)
      hashstring +=roles[i];

    return hashstring.hashCode();
  }

  public boolean equals(Object obj) {
    if (obj instanceof DestinationPredicate){
      DestinationPredicate dp = (DestinationPredicate)obj;
      String [] otherRoles = dp.getRoles();
      if (otherRoles.length != roles.length)
        return false;
      
      int matchcount = 0;
      for (int i=0; i<roles.length; i++)
        for (int j=0; j<otherRoles.length; j++)
          if (roles[i].equals(otherRoles[j]))
            matchcount++;
      if (matchcount == roles.length)
        return true;
    }
    return false;
  }
	  
  public boolean execute(Object o) {
    // Screen out self org
    if (o instanceof  Organization) {
      if (((Organization) o).isSelf()) {
        return false;
      }
      
      RelationshipSchedule schedule = 
        ((Organization)o).getRelationshipSchedule();

      // Does this org have all the roles we need?
      // BOGUS - time phased aspect ignored
      for (int i=0; i<roles.length; i++) {
        // Need to drop self org
        Collection matchRoles = 
          schedule.getMatchingRelationships(Role.getRole(roles[i]).getConverse(),
                                            TimeSpan.MIN_VALUE,
                                            TimeSpan.MAX_VALUE);
        
        if (matchRoles.size() == 0) {
          return false;
        }
      }
      return true;
    }
    return false;
  }
};



