/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/mlm/ui/tpfdd/xml/Attic/Parseable.java,v 1.1 2001-12-27 22:44:36 bdepass Exp $ */

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
    public String toXMLDocument();

    public String toXML();

    public String toURLQuery();
}