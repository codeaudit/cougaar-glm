/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.examples;

import org.cougaar.core.plugin.PlugInAdapter;
import org.cougaar.core.plugin.PrototypeProvider;

import org.cougaar.domain.planning.ldm.LDMServesPlugIn;
import org.cougaar.domain.planning.ldm.RootFactory;
import org.cougaar.domain.planning.ldm.asset.*;


import org.cougaar.domain.glm.ldm.*;import org.cougaar.domain.glm.ldm.*;import org.cougaar.domain.glm.*;
import org.cougaar.domain.glm.ldm.plan.*;
import org.cougaar.domain.glm.ldm.asset.*;
import org.cougaar.domain.glm.ldm.oplan.*;

/** This plugin knows how to build protypes with typeids
 * beginning with "NSN/". Actually, it only understands
 * NSNs in the range of 100-199, which (for our purposes)
 * are all Widgets, which are of the class PhysicalAsset.
 **/

public class PrototypeProviderPlugInExample 
  // no need to inherit any fancy plugin functionality
  extends PlugInAdapter 
  // advertize as a prototypeProvider
  implements PrototypeProvider {

  /** This method is called by ldm.getPrototype() methods
   * after first checking the cache.  If the right thing
   * is not found in the cache, the ldm calls prototype providers
   * in order until one returns an asset.
   **/
  public Asset getPrototype(String aTypeName, Class hint) {
    // first, see if we can filter based on the hint
    if (hint != null &&
        // we only make PhysicalAssets.
        ! (hint.isAssignableFrom(PhysicalAsset.class))) 
      return null;

    // decide if this is the kind of prototype we can handle.
    if (aTypeName.startsWith("NSN/")) {
      Asset proto = makeNSNPrototype(aTypeName);
      // couldn't handle this typename after all
      if (proto == null) return null;
      
      // let propertyProviders fill in details
      theLDM.fillProperties(proto);
      // cache it so we don't get called again on this object
      theLDM.cachePrototype(aTypeName, proto);
      // return it
      return proto;
    } else {
      return null;
    }
  }

  private Asset makeNSNPrototype(String typeid) {
    // one would normally have a database lookup or somesuch.
    int nsn = Integer.parseInt(typeid.substring(4));

    Asset proto = null;
    // is this a Widget?
    if (nsn >= 100 && nsn <= 199) {
      proto = theLDMF.createPrototype(typeid, "PhysicalAsset");
      ((NewTypeIdentificationPG)proto.getTypeIdentificationPG()).setNomenclature("Widget Type "+(nsn-100));
    }
    return proto;
  }

}

