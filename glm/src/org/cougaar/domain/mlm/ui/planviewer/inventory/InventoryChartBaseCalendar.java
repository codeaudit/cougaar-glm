/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.planviewer.inventory;

import java.util.TimeZone;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.util.Locale;

public class InventoryChartBaseCalendar extends GregorianCalendar {

    final static private int baseYear=2000;
    final static private TimeZone baseTimeZone = TimeZone.getTimeZone("GMT");
    final static private InventoryChartBaseCalendar baseCal = new InventoryChartBaseCalendar();

    public InventoryChartBaseCalendar () {
	super(baseYear,0,0);
	setTimeZone(baseTimeZone);
	// Weird thing if you reverse the constructor and setter like below doing exact same thing supposedly
	// you get a different base time!!! and it effects the calendar.
	//super(TimeZone.getTimeZone("GMT"));
	//set(baseYear,0,0,0,0,0);
	//System.out.println("InventoryChartBaseCalendar::My date: " + this.getTime());
    }

    public static long getBaseTime(){ return baseCal.getTime().getTime(); }
    public static int  getBaseYear(){ return baseYear; }

}



