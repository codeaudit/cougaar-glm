/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/glm/util/Attic/GLMLocationScoringFunction.java,v 1.5 2001-08-22 20:27:30 mthome Exp $ */
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

package org.cougaar.domain.glm.util;

import org.cougaar.domain.planning.ldm.measure.Latitude;
import org.cougaar.domain.planning.ldm.measure.Longitude;
import org.cougaar.domain.planning.ldm.plan.AspectLocation;
import org.cougaar.domain.planning.ldm.plan.AspectScorePoint;
import org.cougaar.domain.planning.ldm.plan.AspectScoreRange;
import org.cougaar.domain.planning.ldm.plan.AspectType;
import org.cougaar.domain.planning.ldm.plan.AspectValue;
import org.cougaar.domain.planning.ldm.plan.ScoringFunction;
import org.cougaar.domain.glm.ldm.plan.GeolocLocation;
import org.cougaar.domain.glm.ldm.plan.NewGeolocLocation;

import org.cougaar.domain.planning.ldm.RootFactory;

//import org.cougaar.util.SingleElementEnumeration;
import org.cougaar.util.Empty;

//import java.util.Calendar;
//import java.util.Date;
import java.util.Enumeration;
//import java.util.Vector;

/** 
 * Represents a location scoring function - score gets worse (higher or lower?)
 * as straight-line distance from Lat, Lon increases.
 * ?We'd like this to be part of the COUGAAR baseline...?
 */

public class GLMLocationScoringFunction extends ScoringFunction.PreferredValueScoringFunction {
  private static boolean debug = false;
  public static void setDebug (boolean dbg) { debug = dbg; }
  public static double EPSILON = 1.0d;

  //  protected GeolocLocation my_loc = null;
  
  /**
   * Makes an AspectLocation, which I assume to have type AspectType.POD.
   * (It's not clear anywhere that it should be anything else...)
   *
   * @see org.cougaar.domain.planning.ldm.plan.AspectLocation
   * @see org.cougaar.domain.planning.ldm.plan.AspectType
   */
  public GLMLocationScoringFunction(GeolocLocation loc) {
    super (new AspectLocation (AspectType.POD, loc), 0);
    //    my_loc = loc;
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

	double score_as_distance = GLMMeasure.distanceBetween(loc, my_loc).getFurlongs();
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
//    RootFactory ldmf = getLDM().getFactory();

//    NewGeolocLocation new_gl = ldmf.newGeolocLocation();
//    new_gl.setName("Ad Dammam, SA");
//    new_gl.setGeolocCode("ABFL");
//    new_gl.setLatitude(Latitude.newDegrees("26.43"));
//    new_gl.setLongitude(Longitude.newDegrees("50.1"));
    
//    ScoringFunction sf = new GLMLocationScoringFunction (new_gl);
//    setDebug (true);

//    AspectLocation a_loc = new AspectLocation (AspectType.POD, 
//					    (double) beforeearly.getTime ());
//    System.out.println ("Score for before early " + sf.getScore (a_loc));

//     a_loc = new AspectLocation (AspectType.POD, (double) early.getTime ());
//     System.out.println ("Score for early " + sf.getScore (a_loc));

//USE THIS ONE FIRST    a_loc = new AspectLocation (AspectType.POD, new_gl);
//    System.out.println ("Score for best " + sf.getScore (a_loc));

//     a_loc = new AspectLocation (AspectType.POD, (double) late.getTime ());
//     System.out.println ("Score for late " + sf.getScore (a_loc));

//     a_loc = new AspectLocation (AspectType.POD, (double) afterlate.getTime ());
//     System.out.println ("Score for after late " + sf.getScore (a_loc));
  }
}

