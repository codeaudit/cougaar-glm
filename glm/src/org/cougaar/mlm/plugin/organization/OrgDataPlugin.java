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
package org.cougaar.mlm.plugin.organization;

import java.util.Collection;
import java.util.Iterator;

import org.cougaar.glm.ldm.Constants;
import org.cougaar.planning.ldm.plan.Role;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.planning.plugin.asset.AssetDataPluginBase;

public class OrgDataPlugin extends AssetDataPluginBase  {

  static {
    packages.add("org.cougaar.glm.ldm.asset");
    packages.add("org.cougaar.glm.ldm.plan");
    packages.add("org.cougaar.glm.ldm.oplan");
    packages.add("org.cougaar.glm.ldm.policy");
  }

  protected Verb getReportVerb(Collection roles) {
    // Assuming that collection of roles never mixes subordinate with
    // provider roles.
    for (Iterator iterator = roles.iterator(); iterator.hasNext();) {
      Role role = (Role) iterator.next();

      // Does this Role match SUPERIOR/SUBORDINATE RelationshipType
      if ((role.getName().endsWith(Constants.RelationshipType.SUBORDINATE_SUFFIX)) &&
	  (role.getConverse().getName().endsWith(Constants.RelationshipType.SUPERIOR_SUFFIX))) {
	return Constants.Verb.ReportForDuty;
      }
    } 

    // Didn't get a superior/subordinate match
    return Constants.Verb.ReportForService;
  }
}
