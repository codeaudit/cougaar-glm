/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
package org.cougaar.domain.mlm.plugin.organization;

import java.util.*;

import org.cougaar.domain.planning.ldm.plan.Role;
import org.cougaar.domain.planning.ldm.plan.Verb;
import org.cougaar.domain.planning.plugin.AssetDataPlugIn;

import org.cougaar.domain.glm.ldm.Constants;

public class OrgDataPlugIn extends AssetDataPlugIn  {

  static {
    packages.add("org.cougaar.domain.glm.ldm.asset");
    packages.add("org.cougaar.domain.glm.ldm.plan");
    packages.add("org.cougaar.domain.glm.ldm.oplan");
    packages.add("org.cougaar.domain.glm.ldm.policy");
  }

}
