/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/tpfdd/gui/view/Attic/ScheduleElement.java,v 1.1 2000-12-15 20:17:48 mthome Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Sundar Narasimhan, Daniel Bromberg
*/

package org.cougaar.domain.mlm.ui.tpfdd.gui.view;


public interface ScheduleElement
{
    public String getUUID();

    public long getActualStart();

    public long getActualEnd();

    public long getEstimatedStart();

    public long getEstimatedEnd();

    public int getMode();

    public boolean isContiguous();
}
