/*--------------------------------------------------------------------------
 * <copyright>
 *  Copyright 1999-2003 BBNT Solutions, LLC
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
 * --------------------------------------------------------------------------*/
package org.cougaar.glm.plugins;

import org.cougaar.glm.ldm.asset.Organization;

/** Associates proper BasicProcessor with a given Plugin. */
public abstract class PluginDecorator {
    DecorationPlugin plugin_;
    
    // constructor
    public PluginDecorator(DecorationPlugin plugin) {
	plugin_ = plugin;
    }

    /** 
     *  Customizes the given plugin with one or more BasicProcessor 
     *  depending on information from the organizational asset 
     *  this cluster represents.
     *  @param plugin to be configured
     */
    public abstract void decoratePlugin(Organization cluster);
    
    protected void addTaskProcessor(BasicProcessor processor) {
	plugin_.addTaskProcessor(processor);
    }

    protected Boolean need (String processor) {
	return (Boolean)plugin_.getParam(processor);
    }

}
