/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/tpfdd/util/Attic/Consumer.java,v 1.1 2000-12-15 20:17:47 mthome Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/

/**
 * Basic model for any entity that wants to consume items.  Current
 * examples include standard error, LogPlan and GanttChart views.
 * Generic interface defined so code in GenericProducer can be written
 * just once.  Specialized models like ItemPoolModelConsumer might look
 * identical for now, but might eventually support extended methods as
 * well as using this common base.
 */

package org.cougaar.domain.mlm.ui.tpfdd.util;


public interface Consumer
{
    void fireAddition(Object item);

    void fireDeletion(Object item);
    
    void fireChange(Object item);

    void firingComplete();
}
