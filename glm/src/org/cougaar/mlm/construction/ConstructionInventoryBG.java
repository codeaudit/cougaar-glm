/*--------------------------------------------------------------------------
 * <copyright>
 *  Copyright 2001-2003 BBNT Solutions, LLC
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
package org.cougaar.mlm.construction;
import org.cougaar.glm.debug.GLMDebug;
import org.cougaar.glm.ldm.asset.InventoryBG;
import org.cougaar.glm.ldm.asset.InventoryPG;
import org.cougaar.glm.ldm.asset.NewInventoryPG;
import org.cougaar.planning.ldm.asset.PGDelegate;
import org.cougaar.planning.ldm.asset.PropertyGroup;
import org.cougaar.planning.ldm.measure.Count;

public  class ConstructionInventoryBG extends InventoryBG {

    public ConstructionInventoryBG(InventoryPG pg) {
	    super(pg);
    }
    
    public boolean initialize(double[] levels) {

	  try {
	    double capacity = levels[0];
	    double erq = levels[2];
	    ((NewInventoryPG)myPG_).setCapacity(new Count(levels[0], Count.EACHES));
	    ((NewInventoryPG)myPG_).setInitialLevel(new Count(levels[1], Count.EACHES));
	    ((NewInventoryPG)myPG_).setReorderLevel(new Count(levels[2], Count.EACHES));
	    ((NewInventoryPG)myPG_).setMinReorder(new Count(levels[3], Count.EACHES));
	    ((NewInventoryPG)myPG_).setUnobtainable(new Count(0.0, Count.EACHES));
	    ((NewInventoryPG)myPG_).setFillToCapacity(false);

	    ((NewInventoryPG)myPG_).setMaintainAtCapacity(false);

	    if ((capacity < erq) || (erq < 0)) {
		    GLMDebug.ERROR("ConstructionInventoryBG","Cannot create InventoryBG, capacity "+
				 capacity+" < erq "+erq);
		    initialized_ = false;
	    } else {
		    initialized_ = true;
		    GLMDebug.DEBUG("ConstructionInventoryBG","Initialized InventoryPG, capacity "+
				capacity+", erq "+erq+", initiali level: "+levels[1]+", min reorder: "+levels[3]);
	    }
	  } catch (NullPointerException npe) {
	    initialized_ = false;
	  }
	  return initialized_;
	
  }

  public PGDelegate copy(PropertyGroup pg) {
	  ConstructionInventoryBG bg = new ConstructionInventoryBG((InventoryPG)pg);
	  return bg;
  }
}
