// $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/glm/gss/Attic/GLMTaskAccessorImpl.java,v 1.2 2000-12-20 18:18:09 mthome Exp $
/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBNT Solutions (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.gss;

import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.domain.glm.ldm.plan.GeolocLocation;
import org.cougaar.domain.planning.ldm.plan.Task;

import org.cougaar.lib.gss.*;

/**
 * Accesses the specified property in the specified asset for a given task
 *
 * Copyright (C) 1999 BBN Technologies
 */

public class GLMTaskAccessorImpl implements GSTaskAccessor, GSParent {

  private final static int NONE = -1;
  private final static int ASSET = 0;
  private final static int LOCATION = 1;
  private final static int VALUE = 2;

  /**
   * Constructor specifying which asset (using preposition) and
   * property value for that asset
   */
  GLMTaskAccessorImpl (String preposition) {
    this.preposition = preposition;
    if (preposition == null)
      System.out.println ("No preposition for task accessor");
  }

  public void addChild (Object obj) {
    if (obj instanceof GSAssetAccessor) {
      assetAccessor = (GSAssetAccessor) obj;
      type = ASSET;
    }
    else if (obj instanceof GLMLocationAccessor) {
      locationAccessor = (GLMLocationAccessor) obj;
      type = LOCATION;
    }
    else if (obj instanceof GSValueAccessor) {
      valueAccessor = (GSValueAccessor) obj;
      type = VALUE;
    }
  }


  static Object getTaskObject (Task task, String preposition) {
    Object obj = null;
    if (preposition.equals ("direct")) {
      obj = task.getDirectObject();
      if (obj == null) {
        System.out.println ("No direct object for task " + task);
        return null;
      }
    } else {
      PrepositionalPhrase phrase = task.getPrepositionalPhrase (preposition);
      if (phrase == null) {
        System.out.println ("No phrase with preposition " + preposition +
                            " for task " + task);
        return null;
      }
      obj = phrase.getIndirectObject();
      if (obj == null) {
        System.out.println ("No indirect object for preposition " +
                            preposition + " for task " + task);
        return null;
      }
    }
    return obj;
  }


  /** Gets the value from the specified field; returns null if anything
      does not exist */
  public Object value (Task task) {
    Object obj = getTaskObject (task, preposition);
    Object retval = null;
    switch (type) {
    case ASSET:
      retval = assetAccessor.value ((Asset) obj);
      if (retval == null)
	assetAccessor.reportError ((Asset) obj);
      return retval;
    case LOCATION:
      return locationAccessor.value ((GeolocLocation) obj);
    case VALUE:
      return valueAccessor.value(obj);
    case NONE:
      return obj;
    }
    return null;
  }

  public GSAssetAccessor getAssetAccessor () { 
    return assetAccessor;
  }

  public GSValueAccessor getValueAccessor () { 
    return valueAccessor;
  }

  private GSAssetAccessor assetAccessor = null;
  private GLMLocationAccessor locationAccessor = null;
  private GSValueAccessor valueAccessor = null;
  private String preposition;
  private int type = NONE;

}
