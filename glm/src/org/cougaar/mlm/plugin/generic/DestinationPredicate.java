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

package org.cougaar.mlm.plugin.generic;

import java.util.Collection;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.planning.ldm.plan.RelationshipSchedule;
import org.cougaar.planning.ldm.plan.Role;
import org.cougaar.util.TimeSpan;
import org.cougaar.util.UnaryPredicate;

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



