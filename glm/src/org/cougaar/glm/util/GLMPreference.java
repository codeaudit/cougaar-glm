/*  */
/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.glm.util;

import java.util.Date;

import org.cougaar.glm.ldm.plan.GeolocLocation;
import org.cougaar.lib.util.UTILPluginException;
import org.cougaar.lib.util.UTILPreference;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.AspectLocation;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.AspectValue;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Preference;
import org.cougaar.planning.ldm.plan.ScoringFunction;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.util.log.Logger;

/** 
 * This class contains preference-related methods.
 * Can ask basic questions like ready at, early, best, and latest date for
 * tasks.
 *
 * Can also create an various kinds of preferences.
 */

public class GLMPreference extends UTILPreference {
  private static String myName = "GLMPreference";
  private static double ONE_DAY = 1000*60*60*24; // millis
  private static double ONE_OVER_ONE_DAY = 1.0d/ONE_DAY; // millis
  private static double fiftyYears = ONE_DAY*365*50; // millis
  private static double boundaryDefaultScore = 0.75;

  public GLMPreference (Logger l) { super (l); }

  /**
   * Make a POD preference.
   *
   * Score increases as distance from best location increases...
   * 
   * This needs work...
   *
   * What should we do with the weight of the preference?
   * Should this be set by a policy object?
   */
  public Preference makePODPreference(PlanningFactory ldmf,
				      GeolocLocation loc) {
    GLMLocationScoringFunction podSF = new GLMLocationScoringFunction(loc, logger);
    Preference podPref = ldmf.newPreference(AspectType.POD, podSF, 1.0);
    return podPref;
  }

  /**
   * What should we do with the weight of the preference?
   * Should this be set by a policy object?
   *
   * Note that it uses one day as the slope -- i.e.
   * a day after the POD date, the pref is exceeded.
   */
  public Preference makePODDatePreference(PlanningFactory ldmf,
						 Date bestDate) {
    if (bestDate == null || bestDate.before(new Date(1000))) {
      System.err.println("GLMPreference creating bad POD_Date preference: the date is " + bestDate);
    }
    AspectValue podAV = AspectValue.newAspectValue(AspectType.POD_DATE, bestDate.getTime());
    ScoringFunction podSF = ScoringFunction.createPreferredAtValue(podAV, 
								   ONE_OVER_ONE_DAY);
    Preference podPref = ldmf.newPreference(AspectType.POD_DATE, podSF, 1.0);
    return podPref;
  }


  /**
   * Returns the POD Date from task object, null if POD date not a pref on this task
   *
   * @param  t - the Task with the pref
   * @return Date point of departure date for task, null if no POD date pref
   */

  public Date getPODDate(Task t) {
    Preference pod_pref = getPrefWithAspectType(t, AspectType.POD_DATE);
    if (pod_pref == null)
      return null;

    Date pod_date = 
      new Date((long)(pod_pref.getScoringFunction().getBest().getValue()));

    return pod_date;
  }
 
  /**
   * Get reported POD date from plan element
   */
  public Date getReportedPODDate (PlanElement pe) {
    return getReportedPODDate (pe.getReportedResult());
  }

  /**
   * Get reported POD date from allocation result
   */
  public Date getReportedPODDate (AllocationResult result) {
    double value = result.getValue (AspectType.POD_DATE);
    if (value != NO_ASPECT_VALUE)
      return new Date ((long) value);
    return null;
  }

  /**
   * Returns the POD location from task object, null if POD not a pref on this task
   *
   * @param  t - the Task with the pref
   * @return Date point of departure for task, null if no POD pref
   */

  public GeolocLocation getPODLocation (Task t) {
    Preference pod_pref = getPrefWithAspectType(t, AspectType.POD);
    if (pod_pref == null)
      return null;

    GeolocLocation geoloc = 
      ((GLMLocationScoringFunction) pod_pref.getScoringFunction()).getLocation();

    return geoloc;
  }
 
  /**
   * Get reported POD location from plan element
   */
  public GeolocLocation getReportedPODLocation (PlanElement pe) {
    return getReportedPODLocation (pe.getReportedResult ());
  }

  /**
   * Get reported POD location from allocation result
   */
  public GeolocLocation getReportedPODLocation (AllocationResult result) {
    if (result == null)
      return null;

    Object aspectValue = null;
    AspectValue [] results = result.getAspectValueResults ();

    try {
      for (int i = 0; i < results.length; i++) {
	if (results[i].getAspectType () == AspectType.POD) {
	  aspectValue = results[i];
	  AspectLocation loc = (AspectLocation) aspectValue;
	  return (GeolocLocation) loc.getLocationValue();
	}
      }
    } catch (ClassCastException cce) {
      throw new UTILPluginException(classname + 
				    ".getReportedPODLocation - expecting an AspectLocation as POD aspect value. " + 
				    "\nInstead got " + aspectValue.getClass ());
    }
				      

    return null;
  }

  private static final String classname = GLMPreference.class.getName ();
}
