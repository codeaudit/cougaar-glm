/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/tpfdd/gui/component/Attic/VirtualXAdjustmentListener.java,v 1.3 2001-10-17 19:06:59 gvidaver Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Harry Tsai
*/

package org.cougaar.domain.mlm.ui.tpfdd.gui.component;

public interface VirtualXAdjustmentListener
{	
	public final int LOCATION_CHANGED = 1<<0;
	public final int SIZE_CHANGED = 1<<1;

	public abstract void VirtualXChanged( int changeType, VirtualX vx );
}
