/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.plan;

/**
 * Capability - the Capability an Asset has.
 *
 *  @author  ALPINE <alpine-software@bbn.com>
 *  @version $Id: Capability.java,v 1.1 2000-12-15 20:18:01 mthome Exp $
 */

public interface Capability { 
  // capability prototypes
  static final String SELF = "Self";
  static final String SPAREPARTSPROJECTOR = "SparePartsProjector";
  static final String SPAREPARTSPROVIDER = "SparePartsProvider";
  static final String SUPERIOR = "Superior";
  static final String SPAREPART = "SparePart";
  static final String MAJORENDITEM = "MajorEndItem";
  static final String SUBORDINATE = "Subordinate";
  static final String ORDER = "Order";
}
