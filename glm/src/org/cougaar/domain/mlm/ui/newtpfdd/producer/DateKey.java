/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/newtpfdd/producer/Attic/DateKey.java,v 1.1 2001-02-22 22:42:31 wseitz Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/


package org.cougaar.domain.mlm.ui.tpfdd.producer;


import java.lang.Comparable;

import org.cougaar.domain.mlm.ui.tpfdd.util.OutputHandler;


public class DateKey implements Comparable
{
    long dateSeconds;

    DateKey(long dateSeconds)
    {
	this.dateSeconds = dateSeconds / (60 * 10 * 1000); // consider 10 minutes apart to be equivalent
    }

    public int compareTo(Object object)
    {
	if ( !(object instanceof DateKey) )
	    OutputHandler.out("DK:cT Error: " + getClass() + " cannot compare to " + object.getClass());
	DateKey otherKey = (DateKey)object;
	if ( dateSeconds < otherKey.dateSeconds )
	    return -1;
	if ( dateSeconds == otherKey.dateSeconds )
	    return 0;
	return 1;
    }

    public String toString()
    {
	return dateSeconds + "";
    }
}
