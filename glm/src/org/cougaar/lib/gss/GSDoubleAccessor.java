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

package org.cougaar.lib.gss;

import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.asset.Asset;

/**
 * Base class for those classes that need to have resource and task
 * accessors as children
 *
 */

class GSDoubleAccessor implements GSParent {

  /** 
   * should be called first with resource accessor and then with
   * task accessor
   */
  public void addChild (Object obj) {
      //      System.out.println ("Double add child");
    if (obj instanceof GSConstantAccessor) {
	//      System.out.println ("Got a constant accessor");
      if (resourceAccessor == null)
        resourceAccessor = (GSAssetAccessor) obj;
      else if (taskAccessor == null)
        taskAccessor = (GSTaskAccessor) obj;
      else
        System.out.println ("Too many accessors in capability match");
    }

    else if (obj instanceof GSAssetAccessor) {
	//      System.out.println ("Got an asset accessor");
      if (resourceAccessor == null)
        resourceAccessor = (GSAssetAccessor) obj;
      else
        System.out.println ("Too many accessors in capability match");
    }

    else if (obj instanceof GSTaskAccessor) {
	//      System.out.println ("Got an task accessor");
      if (taskAccessor == null)
        taskAccessor = (GSTaskAccessor) obj;
      else
        System.out.println ("Too many accessors in capability match");
      //      System.out.println ("Leaving Got an task accessor");
    }

    else
      System.out.println ("Unknown type of child in accessor :" + 
			  obj.getClass());
  }

  public GSAssetAccessor getAssetAccessor () {
    return (GSAssetAccessor) resourceAccessor; 
  }
  public GSTaskAccessor getTaskAccessor () {
    return (GSTaskAccessor) taskAccessor; 
  }

  protected GSAssetAccessor resourceAccessor = null;
  protected GSTaskAccessor taskAccessor = null;

}

