/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/glm/Attic/GLMConst.java,v 1.2 2001-04-05 19:27:28 mthome Exp $ */
/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm;

import org.cougaar.domain.planning.ldm.plan.Role;

/**
 * Class to hold constants for the GLM tree.
 * Values should probably only be included here if they are public static final
 *
 */
public class GLMConst {
  public static final Role AMMUNITION_SEA_PORT = Role.getRole("AmmunitionSeaPort");
  public static final Role AMMO_SEA_PORT = Role.getRole("AmmoSeaPort");
  public static final Role GENERIC_SEA_PORT = Role.getRole("GenericSeaPort");
  public static final Role THEATER_SEA_PROVIDER = Role.getRole("TheaterSeaTransportationProvider");
}
