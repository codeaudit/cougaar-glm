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

import java.util.Enumeration;

import org.cougaar.glm.ldm.plan.GeolocLocation;
import org.cougaar.planning.ldm.plan.AspectLocation;
import org.cougaar.planning.ldm.plan.AspectScorePoint;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.AspectValue;
import org.cougaar.planning.ldm.plan.ScoringFunction;
import org.cougaar.util.Empty;
import org.cougaar.util.log.Logger;

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

