/*--------------------------------------------------------------------------
 *                         RESTRICTED RIGHTS LEGEND
 *
 *   Use, duplication, or disclosure by the Government is subject to
 *   restrictions as set forth in the Rights in Technical Data and Computer
 *   Software Clause at DFARS 52.227-7013.
 *
 *                             BBNT Solutions LLC,
 *                             10 Moulton Street
 *                            Cambridge, MA 02138
 *                              (617) 873-3000
 *
 *   Copyright 2001 by
 *             BBNT Solutions LLC,
 *             all rights reserved.
 *
 * --------------------------------------------------------------------------*/
package org.cougaar.domain.mlm.construction;
import org.cougaar.domain.planning.ldm.asset.*;
import org.cougaar.domain.planning.ldm.measure.Count;
import org.cougaar.domain.glm.plugins.TimeUtils;
import org.cougaar.domain.glm.ldm.asset.*;
import org.cougaar.domain.glm.ldm.asset.*;
import org.cougaar.domain.glm.debug.GLMDebug;
import org.cougaar.domain.glm.ldm.plan.NewQuantityScheduleElement;

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
