/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.openmap;

/** 
 * This is a handy interface for links, nodes, and routes to
 * implement.  
 */

interface TransportNetworkElement {
    
    /** Get a string that will be printed below the map */
    public String getInfoLine();

};
