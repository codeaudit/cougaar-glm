/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
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

