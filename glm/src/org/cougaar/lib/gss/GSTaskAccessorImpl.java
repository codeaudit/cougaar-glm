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

package org.cougaar.lib.gss;

import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;

import org.cougaar.planning.ldm.plan.Task;

/**
 * Accesses the specified property in the specified asset for a given task
 *
 */

public class GSTaskAccessorImpl implements GSTaskAccessor, GSParent {

  private final static int NONE = -1;
  private final static int ASSET = 0;
  private final static int LOCATION = 1;
  private final static int VALUE = 2;

  /**
   * Constructor specifying which asset (using preposition) and
   * property value for that asset
   */
  GSTaskAccessorImpl (String preposition) {
    this.preposition = preposition;
    if (preposition == null)
      System.out.println ("No preposition for task accessor");
  }

  public void addChild (Object obj) {
    if (obj instanceof GSAssetAccessor) {
      assetAccessor = (GSAssetAccessor) obj;
      type = ASSET;
    }
//     else if (obj instanceof GSLocationAccessor) {
//       locationAccessor = (GSLocationAccessor) obj;
//       type = LOCATION;
//     }
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
//     case LOCATION:
//       return locationAccessor.value ((GeolocLocation) obj);
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
//   private GSLocationAccessor locationAccessor = null;
  private GSValueAccessor valueAccessor = null;
  private String preposition;
  private int type = NONE;

}
