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
package org.cougaar.domain.mlm.plugin.organization;

import java.util.Collection;
import org.cougaar.domain.glm.ldm.Constants;
import org.cougaar.domain.planning.ldm.plan.Verb;
import org.cougaar.domain.planning.plugin.AssetDataPlugInBase;

public class OrgDataPlugIn extends AssetDataPlugInBase  {

  static {
    packages.add("org.cougaar.domain.glm.ldm.asset");
    packages.add("org.cougaar.domain.glm.ldm.plan");
    packages.add("org.cougaar.domain.glm.ldm.oplan");
    packages.add("org.cougaar.domain.glm.ldm.policy");
  }

  protected Verb getReportVerb(Collection roles) {
    // kludge - assuming that collection of roles never mixes subordinate with
    // provider roles.
    if (roles.contains(Constants.Role.SUBORDINATE) ||
        roles.contains(Constants.Role.ADMINISTRATIVESUBORDINATE)) {
      //System.out.println("Report for duty");
      return Constants.Verb.ReportForDuty;
    } else {
      //System.out.println("Report for service");
      return Constants.Verb.ReportForService;
    }
  }
}
