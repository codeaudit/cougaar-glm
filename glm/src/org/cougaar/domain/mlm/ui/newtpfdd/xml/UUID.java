/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/newtpfdd/xml/Attic/UUID.java,v 1.2 2001-02-23 01:02:24 wseitz Exp $ */

/*
 * Copyright 1999 BBN Systems and Technologies, A Division of BBN Corporation
 * 10 Moulton Street, Cambridge, MA 02138 (617) 873-3000
 */


package org.cougaar.domain.mlm.ui.newtpfdd.xml;


import org.cougaar.domain.mlm.ui.newtpfdd.util.MismatchException;


public interface UUID
{
    /**
       Returns a string of the form clusterName/number
       @return String - the object ID
    */
    String getUUID();
}
