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
package org.cougaar.mlm.plugin.organization;

import java.util.Collection;
import java.util.Iterator;

import org.cougaar.glm.ldm.Constants;
import org.cougaar.planning.ldm.plan.Role;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.planning.plugin.asset.AssetDataPlugin;

public class OrgDataPlugin extends AssetDataPlugin  {

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
