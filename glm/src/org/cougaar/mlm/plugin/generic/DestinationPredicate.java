/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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

package org.cougaar.mlm.plugin.generic;

import java.util.Collection;

import org.cougaar.core.mts.MessageAddress;

import org.cougaar.planning.ldm.asset.Asset;

import org.cougaar.planning.ldm.plan.RelationshipSchedule;
import org.cougaar.planning.ldm.plan.Role;

import org.cougaar.util.TimeSpan;
import org.cougaar.util.UnaryPredicate;

import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.Organization;

/**
 * DestinationPredicate - Unary Predicate describing the 
 * destination cluster of an Oplan or Policy that is being forwarded
 *
 */

public class DestinationPredicate implements UnaryPredicate {
  protected String roles[];

  protected MessageAddress clusterId;


  DestinationPredicate(String [] roles, MessageAddress clusterId) {
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
}



