/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.plugin.sample;

import org.cougaar.core.plugin.PlugInDelegate;

import org.cougaar.domain.planning.ldm.plan.AllocationResult;
import org.cougaar.domain.planning.ldm.plan.AspectType;
import org.cougaar.domain.planning.ldm.plan.Predictor;
import org.cougaar.domain.planning.ldm.plan.Preference;
import org.cougaar.domain.planning.ldm.plan.Task;

import java.util.Date;
import java.util.Enumeration;

public class MCCPredictor implements Predictor
{
    long SHIP_DAYS = 86400000L;
        
    public AllocationResult Predict(Task for_task, PlugInDelegate plugin) {
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
