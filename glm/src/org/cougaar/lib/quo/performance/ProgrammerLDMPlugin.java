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
 *  THIS SOFTWARE IS MODIFIED FOR TESTING QUO_ULTRALLOG INTEGRATION
 */
//package org.cougaar.lib.quo.performance;
package org.cougaar.lib.quo.performance;

import org.cougaar.lib.quo.performance.assets.LanguagePG;
import org.cougaar.lib.quo.performance.assets.NewLanguagePG;
import org.cougaar.lib.quo.performance.assets.NewSkillsPG;
import org.cougaar.lib.quo.performance.assets.ProgrammerAsset;
import org.cougaar.lib.quo.performance.assets.SkillsPG;
import org.cougaar.planning.ldm.asset.ItemIdentificationPG;
import org.cougaar.planning.ldm.asset.NewItemIdentificationPG;
import org.cougaar.planning.plugin.legacy.SimplePlugin;

/**
 * This COUGAAR Plugin creates and publishes ProgrammerAsset objects.
 *
 */
public class ProgrammerLDMPlugin extends SimplePlugin {
  /**
   * Used for initialization to populate the PLAN with ProgrammerAsset objects
   */
protected void setupSubscriptions() {

    // Register our new PropertyFactory so we can refer to properties by name
    theLDMF.addPropertyGroupFactory(new org.cougaar.lib.quo.performance.assets.PropertyGroupFactory());

    // Create the prototypes that will be used to create the programmer assets
    ProgrammerAsset new_prototype = (ProgrammerAsset)theLDMF.createPrototype
      (org.cougaar.lib.quo.performance.assets.ProgrammerAsset.class, "programmer");
    new_prototype.setLanguagePG(getLanguagePG(true, false) );  // knows Java but not JavaScript
    // Cache the prototype in the LDM : note this is not treated
    // As an asset that is available in subscriptions, but can
    // be used to build 'real' assets when asked for by prototype name
    theLDM.cachePrototype("programmer", new_prototype);

    // Create an asset based on an existing prototype
    ProgrammerAsset asset = (ProgrammerAsset) theLDMF.createInstance("programmer");
    asset.setLanguagePG(getLanguagePG(true, true) );  // knows Java and JavaScript
    asset.setSkillsPG(getSkillsPG(10, 100));    // 10 years experience, 100 SLOC/day
    asset.setItemIdentificationPG(getItemIdentificationPG("Bill Gates"));
    publishAdd(asset);

   //   // Create an asset based on an existing prototype
      ProgrammerAsset another_asset = (ProgrammerAsset) theLDMF.createInstance(asset);
     another_asset.setSkillsPG(getSkillsPG(15, 150));   // 15 years experience, 150 SLOC/day
     another_asset.setItemIdentificationPG(getItemIdentificationPG("Linus Torvalds"));
     publishAdd(another_asset);

}

/**
 * Create and populate a Language property group
 */
private LanguagePG getLanguagePG(boolean knowsJava, boolean knowsJavaScript) {
  NewLanguagePG new_language_pg = (NewLanguagePG)theLDMF.createPropertyGroup("LanguagePG");
  new_language_pg.setKnowsJava(knowsJava);
  new_language_pg.setKnowsJavaScript(knowsJavaScript);
  return new_language_pg;
}

/**
 * Create and populate a Skills property group
 */
private SkillsPG getSkillsPG(int yearsExperience, int productivity) {
  NewSkillsPG new_skills_pg = (NewSkillsPG)theLDMF.createPropertyGroup("SkillsPG");
  new_skills_pg.setYearsExperience(yearsExperience);
  new_skills_pg.setSLOCPerDay(productivity);
  return new_skills_pg;
}

/**
 * Create and populate an ItemIdentification property group
 */
private ItemIdentificationPG getItemIdentificationPG(String name) {
  NewItemIdentificationPG new_item_id_pg = (NewItemIdentificationPG)theLDMF.createPropertyGroup("ItemIdentificationPG");
  new_item_id_pg.setItemIdentification(name);
  return new_item_id_pg;
}

/**
 * No subscriptions, so this method does nothing
 */
protected void execute () {}

}

