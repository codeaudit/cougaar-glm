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
package org.cougaar.glm.execution.eg;

public interface PlugIn {
  /**
   * A name to use in menus and such.
   **/
  String getPlugInName();

  /**
   * Get a description of this plugin.
   **/
  String getDescription();

  /**
   * Plugins can be parameterized with a string.
   **/
  void setParameter(String parameter);

  /**
   * Test if this plugin can be configured
   * @return true if it is possible to configure this plugin (has a GUI)
   **/
  boolean isConfigurable();


  void setEventGenerator(EventGenerator eg);


  void configure(java.awt.Component frame);

  /**
   * PlugIns should save and restore their state in a property file.
   * @param props the Properties map in which the values are saved.
   * @param prefix the prefix for every property name. The prefix
   * uniquely identifies the plugin.
   **/
  void save(java.util.Properties props, String prefix);
  void restore(java.util.Properties props, String prefix);
}
