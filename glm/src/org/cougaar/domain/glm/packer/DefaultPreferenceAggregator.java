// Copyright (11/99) Honeywell Inc.
// Unpublished - All rights reserved. This software was developed with funding 
// under U.S. government contract MDA972-97-C-0800

package org.cougaar.domain.glm.packer;

//utils
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;



import org.cougaar.domain.planning.ldm.RootFactory;

import org.cougaar.domain.planning.ldm.plan.AspectType;
import org.cougaar.domain.planning.ldm.plan.AspectValue;
import org.cougaar.domain.planning.ldm.plan.Preference;
import org.cougaar.domain.planning.ldm.plan.ScoringFunction;
import org.cougaar.domain.planning.ldm.plan.Task;



/**
  * This is the PreferenceAggregator used by the packer created by
  * HTC.  The set of preferences it creates is set up to meet the
  * needs of the TOPS MCCGlobalMode cluster that will be receiving
  * the tasks the packer creates.
  */
public class DefaultPreferenceAggregator implements PreferenceAggregator {

  private Calendar myCalendar = Calendar.getInstance();

  /**
    * Will create a preference as follows:
    * START_TIME should be at or greater than 0.0
    * END_TIME should be bracketed around the earliest END_TIME of
    * the input tasks and
    * QUANTITY should be set at the sum of the quantities of the
    * input tasks.
    */
  public ArrayList aggregatePreferences(Iterator tasks, RootFactory rootFactory) {

    ArrayList prefs = new ArrayList();
    double endTime = java.lang.Double.POSITIVE_INFINITY;
    double startTime = 0.0;
    double quantity = 0.0;

    // find values for endTime and quantity
    while (tasks.hasNext()) {
      Task t = (Task) tasks.next();

      // replaced min with this CWG
      if (t.getPreferredValue(AspectType.END_TIME) < endTime ){
	endTime = t.getPreferredValue(AspectType.END_TIME);
      }

      // replaced min with this CWG
      if (t.getPreferredValue(AspectType.START_TIME) > startTime ){
	startTime = t.getPreferredValue(AspectType.START_TIME);
      }
      quantity += t.getPreferredValue(AspectType.QUANTITY);
    }

    // make the START_TIME preference
    // this is a placeholder for more faithful logic later...
    // [1999/11/15:goldman]
    //
    // MSB 1-25-2000 : Make Start time 40 days before end_time
    //startTime = endTime - (40.0*86400000.0);
    myCalendar.setTime(new Date((long)endTime));
    myCalendar.add(Calendar.DATE, -40);
    startTime = myCalendar.getTime().getTime();

    prefs.add(makeStartPreference(startTime, rootFactory));

    // make the endTime preference...
    prefs.add(makeEndPreference(endTime, rootFactory));

    prefs.add(makeQuantityPreference(quantity, rootFactory));
    return prefs;
  }

  // Added the rootFactory argument.  Seemed to need it to make the pref. CGW
  private Preference makeQuantityPreference(double amount, RootFactory rootFactory) {
    AspectValue av = new AspectValue(AspectType.QUANTITY, amount );
    ScoringFunction sf = ScoringFunction.createNearOrBelow(av, 0.1);
    Preference pref = rootFactory.newPreference(AspectType.QUANTITY, sf );
    return pref;
  }

  private Preference makeStartPreference(double startDate, RootFactory rootFactory) {
    AspectValue startTime = new AspectValue(AspectType.START_TIME, startDate);
    ScoringFunction sf = ScoringFunction.createNearOrAbove(startTime, 0.0);
    Preference pref = rootFactory.newPreference(AspectType.START_TIME, sf);
    return pref;
  }

  /**
   * makeEndPreference -
   * separate earliest, best, and latest for TOPS. Picked 1 day out
   * of the blue (with help from Gordon
   */
  private Preference makeEndPreference(double endDate, RootFactory rootFactory) {

    if (GenericPlugin.DEBUG) {
      System.out.println("Aggregated end date - " + new Date((long)endDate));
    }

    AspectValue latest = new AspectValue(AspectType.END_TIME, endDate);

    // MSB 1-25-2000 : Set best to 7 days earlier than latest
    myCalendar.setTime(new Date((long)endDate));
    myCalendar.add(Calendar.DATE, -7);
    AspectValue best = 
      new AspectValue(AspectType.END_TIME, myCalendar.getTime().getTime());

    // MSB 1-25-2000 : Set earliest to 21 days earlier than best
    myCalendar.add(Calendar.DATE, -21);
    AspectValue earliest = 
      new AspectValue(AspectType.END_TIME, myCalendar.getTime().getTime());
    ScoringFunction sf = ScoringFunction.createVScoringFunction(earliest, best,
                                                                latest);
    Preference pref = rootFactory.newPreference(AspectType.END_TIME, sf);
    return pref;
  }
}







