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


import org.cougaar.core.plugin.SimplePlugIn;
import org.cougaar.core.plugin.PropertyProvider;
import org.cougaar.core.plugin.PrototypeProvider;
//import org.cougaar.core.plugin.LDMPlugInServesLDM;
import org.cougaar.util.StateModelException;
import org.cougaar.core.cluster.Subscriber;
import org.cougaar.core.cluster.SubscriberException;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.util.UnaryPredicate;

/**
 * The purpose of this class is to provide a base class for the creation of LDMPlugIns.
 * This implementation leaves the actual methods of Asset creation as an exercise
 * for the reader, i.e. use of JDBC to access data.
 * @see LDMSQLPlugIn
 */

public abstract class LDMEssentialPlugIn
  extends SimplePlugIn
  implements PropertyProvider, PrototypeProvider
{
  // For maintaining container of Assets added by this LDMPlugIn
  Subscriber subscriber;

  public LDMEssentialPlugIn() {}
	
  /**
   * Empty execute(); may (should?) be overridden by subclasses.
   */
  public void execute() {}
	
  //
  // LDMService
  //
  //public abstract Asset getPrototype(String typeid, Class hint);
	
  //public abstract void provideProperties(Asset asset);
	
}
