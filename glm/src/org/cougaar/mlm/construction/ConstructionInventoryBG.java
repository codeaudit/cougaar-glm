/*--------------------------------------------------------------------------
 * <copyright>
 *  
 *  Copyright 2001-2004 BBNT Solutions, LLC
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
 * --------------------------------------------------------------------------*/
package org.cougaar.mlm.construction;

import org.cougaar.glm.ldm.asset.InventoryBG;
import org.cougaar.glm.ldm.asset.InventoryPG;
import org.cougaar.glm.ldm.asset.NewInventoryPG;
import org.cougaar.planning.ldm.asset.PGDelegate;
import org.cougaar.planning.ldm.asset.PropertyGroup;
import org.cougaar.planning.ldm.measure.Count;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

public class ConstructionInventoryBG extends InventoryBG {
  protected Logger logger = Logging.getLogger(ConstructionInventoryBG.class);

  public ConstructionInventoryBG(InventoryPG pg) {
    super(pg);
  }

  public boolean initialize(double[] levels) {

    try {
      double capacity = levels[0];
      double erq = levels[2];
      ((NewInventoryPG) myPG_).setCapacity(new Count(levels[0], Count.EACHES));
      ((NewInventoryPG) myPG_).setInitialLevel(new Count(levels[1], Count.EACHES));
      ((NewInventoryPG) myPG_).setReorderLevel(new Count(levels[2], Count.EACHES));
      ((NewInventoryPG) myPG_).setMinReorder(new Count(levels[3], Count.EACHES));
      ((NewInventoryPG) myPG_).setUnobtainable(new Count(0.0, Count.EACHES));
      ((NewInventoryPG) myPG_).setFillToCapacity(false);

      ((NewInventoryPG) myPG_).setMaintainAtCapacity(false);

      if ((capacity < erq) || (erq < 0)) {
        if (logger.isErrorEnabled()) {
          logger.error("Cannot create InventoryBG, capacity " + capacity + " < erq " + erq);
        }
        initialized_ = false;
      } else {
        initialized_ = true;
        if (logger.isDebugEnabled()) {
          logger.debug("Initialized InventoryPG, capacity " + capacity + ", erq " + erq + ", initiali level: " + levels[1] + ", min reorder: " + levels[3]);
        }
      }
    } catch (NullPointerException npe) {
      initialized_ = false;
    }
    return initialized_;

  }

  public PGDelegate copy(PropertyGroup pg) {
    ConstructionInventoryBG bg = new ConstructionInventoryBG((InventoryPG) pg);
    return bg;
  }
}
