/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/mlm/ui/tpfdd/gui/component/Attic/VirtualXAdjustmentListener.java,v 1.2 2002-01-30 21:58:59 ahelsing Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Harry Tsai
*/

package org.cougaar.mlm.ui.tpfdd.gui.component;

public interface VirtualXAdjustmentListener
{	
  int LOCATION_CHANGED = 1<<0;
  int SIZE_CHANGED = 1<<1;
  
  void VirtualXChanged( int changeType, VirtualX vx );
}
