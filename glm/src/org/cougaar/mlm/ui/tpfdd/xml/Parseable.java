/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/mlm/ui/tpfdd/xml/Attic/Parseable.java,v 1.2 2002-01-30 21:59:00 ahelsing Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/


package org.cougaar.mlm.ui.tpfdd.xml;


import org.cougaar.mlm.ui.tpfdd.util.Copiable;


public interface Parseable extends Copiable
{
    String toXMLDocument();

    String toXML();

    String toURLQuery();
}
