/*  */
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

package org.cougaar.glm.callback;

import java.util.Enumeration;

import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.lib.callback.UTILFilterCallbackAdapter;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.log.Logger;

/**
 *  Filters for organizations, telling listeners of new and changed ones.
 */

public class GLMOrganizationCallback extends UTILFilterCallbackAdapter {
  public GLMOrganizationCallback (GLMOrganizationListener listener, Logger logger) {
    super (listener, logger);
  }

  /**
   * Filter for organizations
   */
  protected UnaryPredicate getPredicate () {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	return (o instanceof Organization);
      }
    };
  }

  /**
   * Tell listener when an organization changes, or is added 
   * to the set of organizations known to this cluster.
   */
  public void reactToChangedFilter () {
    Enumeration changedList = mySub.getChangedList();
    Enumeration addedList   = mySub.getAddedList();

    if (changedList.hasMoreElements ())
      ((GLMOrganizationListener) myListener).handleChangedOrganization (changedList);

    if (addedList.hasMoreElements ())
      ((GLMOrganizationListener) myListener).handleNewOrganization (addedList);
  }
}
        
        
                
                        
                
        
        
