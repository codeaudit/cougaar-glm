/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/mlm/ui/tpfdd/gui/model/Attic/ItemPoolModelListener.java,v 1.2 2002-01-30 21:58:59 ahelsing Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/

/**
  Simple pool model for things like LogPlans that take care of
  rendering their own view of data and need to know only what's in it.
  Implementations should do error-checking on "item" to catch dataflow
  bugs -- possible to be told to do something inconsistent with known
  model.  This is a bit redundant WRT AscentConsumer; there is no
  specialization like in RowModelListener.  Keeping it anyway for
  parallelness with RowModel; might add future flexibilty.
*/

package org.cougaar.mlm.ui.tpfdd.gui.model;


import org.cougaar.mlm.ui.tpfdd.util.Consumer;


public interface ItemPoolModelListener extends Consumer
{
    void fireItemAdded(Object item);

    void fireItemDeleted(Object item);

    void fireItemValueChanged(Object item);

    void fireItemWithIndexDeleted(Object item, int index);
}
