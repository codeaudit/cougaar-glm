/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/mlm/ui/tpfdd/xml/Attic/UUID.java,v 1.1 2001-12-27 22:44:39 bdepass Exp $ */

/*
 * Copyright 1999 BBN Systems and Technologies, A Division of BBN Corporation
 * 10 Moulton Street, Cambridge, MA 02138 (617) 873-3000
 */


package org.cougaar.mlm.ui.tpfdd.xml;


import org.cougaar.mlm.ui.tpfdd.util.MismatchException;


public interface UUID
{
    /**
       Returns a string of the form clusterName/number
       @return String - the object ID
    */
    String getUUID();
}
