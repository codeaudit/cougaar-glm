/*--------------------------------------------------------------------------
 *                         RESTRICTED RIGHTS LEGEND
 *
 *   Use, duplication, or disclosure by the Government is subject to
 *   restrictions as set forth in the Rights in Technical Data and Computer
 *   Software Clause at DFARS 52.227-7013.
 *
 *                             BBN Technologies,
 *                               A Division of
 *                              BBN Corporation
 *                             10 Moulton Street
 *                            Cambridge, MA 02138
 *                              (617) 873-3000
 *
 *   Copyright 1999 by
 *             BBN Technologies, A Division of
 *             BBN Corporation, all rights reserved.
 *
 * --------------------------------------------------------------------------*/
package org.cougaar.domain.glm.plugins;

import org.cougaar.domain.glm.asset.Organization;

/** Associates proper BasicProcessor with a given PlugIn. */
public abstract class PlugInDecorator {
    DecorationPlugIn plugin_;
    
    // constructor
    public PlugInDecorator(DecorationPlugIn plugin) {
	plugin_ = plugin;
    }

    /** 
     *  Customizes the given plugin with one or more BasicProcessor 
     *  depending on information from the organizational asset 
     *  this cluster represents.
     *  @param plugin to be configured
     */
    public abstract void decoratePlugIn(Organization cluster);
    
    protected void addTaskProcessor(BasicProcessor processor) {
	plugin_.addTaskProcessor(processor);
    }

    protected Boolean need (String processor) {
	return (Boolean)plugin_.getParam(processor);
    }

}
