/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

package org.cougaar.mlm.plugin.sample;

import org.cougaar.planning.plugin.legacy.PluginDelegate;

import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.Predictor;
import org.cougaar.planning.ldm.plan.Preference;
import org.cougaar.planning.ldm.plan.Task;

import java.util.Date;
import java.util.Enumeration;

public class MCCPredictor implements Predictor
{
    long SHIP_DAYS = 86400000L;
        
    public AllocationResult Predict(Task for_task, PluginDelegate plugin) {
        // Get Task Preferences, paying attention only to START_TIME
        // and END_TIME for this simple example;
        Date start = null;
        Date end = null;
        boolean is_success = true;
        Enumeration preferences = for_task.getPreferences();
        while (preferences.hasMoreElements()) {
            Preference pref = (Preference)preferences.nextElement();
            int at = pref.getAspectType();
            if (at == AspectType.START_TIME)
                start = new Date(pref.getScoringFunction().getBest().getAspectValue().longValue());
            if (at == AspectType.END_TIME)
                end = new Date(pref.getScoringFunction().getBest().getAspectValue().longValue());
        }
        int[] aspect_array = null;
        double[] results_array = null;
        if ((end.getTime() - start.getTime()) < SHIP_DAYS) {
            is_success = false;
            aspect_array = new int[1];
            results_array = new double[1];
        } else {
            aspect_array = new int[2];
            results_array = new double[2];
            aspect_array[0] = AspectType.START_TIME;
            aspect_array[1] = AspectType.END_TIME;
            results_array[0] = (double)start.getTime();
            results_array[0] = (double)(start.getTime() + SHIP_DAYS);;
        }
        AllocationResult myestimate = plugin.getFactory().newAllocationResult(0.1, is_success, aspect_array, results_array);
        return myestimate;
    }
}
