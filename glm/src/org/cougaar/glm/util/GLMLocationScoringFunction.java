/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/glm/util/GLMLocationScoringFunction.java,v 1.4 2002-11-19 17:21:46 twright Exp $ */
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

package org.cougaar.glm.util;

import org.cougaar.planning.ldm.measure.Latitude;
import org.cougaar.planning.ldm.measure.Longitude;
import org.cougaar.planning.ldm.plan.AspectLocation;
import org.cougaar.planning.ldm.plan.AspectScorePoint;
import org.cougaar.planning.ldm.plan.AspectScoreRange;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.AspectValue;
import org.cougaar.planning.ldm.plan.ScoringFunction;
import org.cougaar.glm.ldm.plan.GeolocLocation;
import org.cougaar.glm.ldm.plan.NewGeolocLocation;

import org.cougaar.planning.ldm.PlanningFactory;

import org.cougaar.util.Empty;
import org.cougaar.util.log.Logger;

import java.util.Enumeration;

/** 
 * Represents a location scoring function - score gets worse (higher or lower?)
 * as straight-line distance from Lat, Lon increases.
 * ?We'd like this to be part of the COUGAAR baseline...?
 */

public class GLMLocationScoringFunction extends ScoringFunction.PreferredValueScoringFunction {
  public static double EPSILON = 1.0d;

  /**
   * Makes an AspectLocation, which I assume to have type AspectType.POD.
   * (It's not clear anywhere that it should be anything else...)
   *
   * @see org.cougaar.planning.ldm.plan.AspectLocation
   * @see org.cougaar.planning.ldm.plan.AspectType
   */
  public GLMLocationScoringFunction(GeolocLocation loc, Logger logger) {
    super (AspectValue.newAspectValue(AspectType.POD, loc), 0);
    //    my_loc = loc;
    measureHelper = new GLMMeasure (logger);
  }

  /** 
   * This doesn't make any sense --- there is no way to represent this.
   * Geolocs are two-tuples, and so don't map onto AspectValues, which
   * are doubles.
   *
   * @param lowerbound (ignored)
   * @param upperbound (ignored)
   * @return Enumeration
   */
  public Enumeration getValidRanges(AspectValue lowerbound, AspectValue upperbound) {
    return Empty.enumeration;
  }

  /** 
   * This doesn't make any sense --- there is no way to represent this.
   * Geolocs are two-tuples, and so don't map onto AspectValues, which
   * are doubles.
   *
   * @param lowerbound (ignored)
   * @param upperbound (ignored)
   * @return null
   */
  public AspectScorePoint getMinInRange(AspectValue lowerbound, AspectValue upperbound){
    AspectScorePoint asp = null;
    return asp;
  }

  /** 
   * This doesn't make any sense --- there is no way to represent this.
   * Geolocs are two-tuples, and so don't map onto AspectValues, which
   * are doubles.
   *
   * @param lowerbound (ignored)
   * @param upperbound (ignored)
   * @return null
   */
  public AspectScorePoint getMaxInRange(AspectValue lowerbound, AspectValue upperbound){
    AspectScorePoint asp = null;
    return asp;
  }

  /**
    * The score is based on the distance between the two points in furlongs.
    * There are many better measures, but this works for now.
    * 
   * @return WORST if bad data only, though we should probably throw an exception
   */
  public double getScore(AspectValue av){
    if (!(av instanceof AspectLocation))
      return ScoringFunction.WORST;
    
    AspectLocation av_loc = (AspectLocation) av;
    GeolocLocation loc = (GeolocLocation) av_loc.getLocationValue();
    GeolocLocation my_loc = getLocation ();

    if (loc != null) {
      if (loc.getLongitude() != null &&
	  loc.getLatitude() != null &&
	  my_loc.getLongitude() != null &&
	  my_loc.getLatitude() != null) {

	double score_as_distance = measureHelper.distanceBetween(loc, my_loc).getFurlongs();
	return score_as_distance;
      }
    }

    // drop through - WORST after last point.
    return ScoringFunction.WORST;
  }

  // Do we want to return a copy?  i.e. a new instance?
  //  The dates used to be new instances of dates...
  public GeolocLocation getLocation () {
    AspectLocation al = (AspectLocation) getBest().getAspectValue();
    return (GeolocLocation) al.getLocationValue ();
  }
  
  public static void main (String [] args) {
//    PlanningFactory ldmf = getLDM().getFactory();

//    NewGeolocLocation new_gl = ldmf.newGeolocLocation();
//    new_gl.setName("Ad Dammam, SA");
//    new_gl.setGeolocCode("ABFL");
//    new_gl.setLatitude(Latitude.newDegrees("26.43"));
//    new_gl.setLongitude(Longitude.newDegrees("50.1"));
    
//    ScoringFunction sf = new GLMLocationScoringFunction (new_gl);
//    setDebug (true);

//    AspectLocation a_loc = new AspectLocation (AspectType.POD, 
//					    (double) beforeearly.getTime ());
//    logger.isDebugEnabled() ("Score for before early " + sf.getScore (a_loc));

//     a_loc = new AspectLocation (AspectType.POD, (double) early.getTime ());
//     logger.isDebugEnabled() ("Score for early " + sf.getScore (a_loc));

//USE THIS ONE FIRST    a_loc = new AspectLocation (AspectType.POD, new_gl);
//    logger.isDebugEnabled() ("Score for best " + sf.getScore (a_loc));

//     a_loc = new AspectLocation (AspectType.POD, (double) late.getTime ());
//     logger.isDebugEnabled() ("Score for late " + sf.getScore (a_loc));

//     a_loc = new AspectLocation (AspectType.POD, (double) afterlate.getTime ());
//     logger.isDebugEnabled() ("Score for after late " + sf.getScore (a_loc));
  }

  protected GLMMeasure measureHelper;
}

