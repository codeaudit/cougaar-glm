/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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

package org.cougaar.mlm.plugin.ldm;


import org.cougaar.core.blackboard.Subscriber;
import org.cougaar.planning.ldm.PropertyProvider;
import org.cougaar.planning.ldm.PrototypeProvider;
import org.cougaar.planning.plugin.legacy.SimplePlugin;

/**
 * The purpose of this class is to provide a base class for the creation of LDMPlugins.
 * This implementation leaves the actual methods of Asset creation as an exercise
 * for the reader, i.e. use of JDBC to access data.
 * @see LDMSQLPlugin
 */

public abstract class LDMEssentialPlugin
  extends SimplePlugin
  implements PropertyProvider, PrototypeProvider
{
  // For maintaining container of Assets added by this LDMPlugin
  Subscriber subscriber; 

  public LDMEssentialPlugin() {}
	
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
