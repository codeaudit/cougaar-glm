/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.ldm.asset;

import java.util.Vector;
import java.util.Enumeration;
import java.io.Serializable;
import org.cougaar.domain.planning.ldm.asset.*;
import org.cougaar.domain.planning.ldm.plan.*;
import java.util.*;
import java.beans.PropertyDescriptor;
import java.beans.IndexedPropertyDescriptor;

public abstract class GLMAsset
  extends org.cougaar.domain.glm.ldm.asset.AssetSkeleton 
{
  protected GLMAsset() {
    super();
  }
  protected GLMAsset(GLMAsset prototype) {
    super(prototype);
  }
}

