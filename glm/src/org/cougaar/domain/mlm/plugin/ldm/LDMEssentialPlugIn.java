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

package org.cougaar.domain.mlm.plugin.ldm;


import org.cougaar.core.plugin.SimplePlugIn;
//import org.cougaar.core.plugin.ComponentPlugin;
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
