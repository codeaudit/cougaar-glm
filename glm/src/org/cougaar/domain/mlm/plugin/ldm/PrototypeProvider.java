/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.plugin.ldm;

import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.domain.planning.ldm.plan.NewScheduleElement;
import org.cougaar.domain.planning.ldm.plan.Schedule;
import org.cougaar.domain.glm.ldm.plan.NewQuantityScheduleElement;
import org.cougaar.domain.planning.ldm.plan.ScheduleElement;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.asset.AggregateAsset;
import org.cougaar.domain.planning.ldm.asset.ItemIdentificationPGImpl;
import org.cougaar.domain.planning.ldm.asset.NewItemIdentificationPG;
import org.cougaar.domain.planning.ldm.asset.NewTypeIdentificationPG;
import org.cougaar.domain.planning.ldm.RootFactory;

import org.cougaar.util.UnaryPredicate;

import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

/** A QueryHandler which can convert a TypeIdentification code
 * into an Asset Prototype.
 **/

public abstract class PrototypeProvider extends QueryHandler {
  public PrototypeProvider() {}

  /** Called to decide if this QueryHandler can provide
   * type resolution for this type id code.
   **/
  public abstract boolean canHandle(String typeid);

  /** Turn a typeid into an Asset Prototype, presumably
   * by looking it up in a database.
   **/
  public abstract Asset getAssetPrototype(String typeid);
}
