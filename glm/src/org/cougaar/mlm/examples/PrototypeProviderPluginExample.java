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

package org.cougaar.mlm.examples;

import org.cougaar.glm.ldm.asset.PhysicalAsset;
import org.cougaar.planning.ldm.PrototypeProvider;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.NewTypeIdentificationPG;
import org.cougaar.planning.plugin.legacy.PluginAdapter;

/** This plugin knows how to build protypes with typeids
 * beginning with "NSN/". Actually, it only understands
 * NSNs in the range of 100-199, which (for our purposes)
 * are all Widgets, which are of the class PhysicalAsset.
 **/

public class PrototypeProviderPluginExample 
  // no need to inherit any fancy plugin functionality
  extends PluginAdapter 
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

