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


package org.cougaar.mlm.debug.ui.draw;
 

/** Class MoreMath
 *
 * Provides functions that are not part of the standard Math class.
 *
 * Functions:
 *	asinh(float x) - hyperbolic arcsine
 *	sinh(float x) - hyperbolic sine
 *
 * Function                Definition                              
 * Hyperbolic sine         (e^x-e^-x)/2                            
 * Hyperbolic cosine       (e^x+e^-x)/2                            
 * Hyperbolic tangent      (e^x-e^-x)/(e^x+e^-x)                   
 * Hyperbolic arc sine     log  (x+sqrt(1+x^2))                    
 * Hyperbolic arc cosine   2 log  (sqrt((x+1)/2) + sqrt((x-1)/2))  
 * Hyperbolic arc tangent  (log  (1+x) - log (1-x))/2
 *
 * @author alpine-software@bbn.com
 *
 * @since jdk1.1
 * */

public class MoreMath {
    public static final float asinh (float x) {
	return (float)Math.log (x + Math.sqrt (x * x + 1));
    }

    public static final float sinh (float x) {
	return (float)(Math.pow(Math.E,x) - Math.pow(Math.E,-x)) / 2.0f;
    }

    /** roundAdjust() AKA qint() - return x s.t. it will be rounded
        away from zero when we cast to an integer or long integer.
        Please Note: This is FAST! */
    public static final double roundAdjust (double in) {
	return (((int) in) < 0) ? (in - 0.5) : (in + 0.5);
    }
    public static final double qint(double x) {
	return roundAdjust(x);
    }

    /** lonDistance(lon1, lon2) - shortest arc distance btwn two lons */
    public static final float lonDistance(float lon1, float lon2) {
	return Math.min(
	    Math.abs(lon1-lon2),
	    ((lon1 < 0) ? lon1+180 : 180-lon1) +
	    ((lon2 < 0) ? lon2+180 : 180-lon2)
	    );
    }

    /** DEG_TO_SC() and SC_TO_DEG() - convert between decimal degrees
        and scoords */
    public static final long DEG_TO_SC (double deg) {
	return (long) (deg * 3600000);
    }
    public static final double SC_TO_DEG (int sc) {
	return ((double)(sc) / (60.0 * 60.0 * 1000.0));
    }


    // HACK - are there functions that already exist?
    /** sign() - return sign of number */
    public static final short sign(short x) {
	return (x < 0) ? (short)-1 : (short)1;
    }
    public static final int sign(int x) {
	return (x < 0) ? -1 : 1;
    }
    public static final long sign(long x) {
	return (x < 0) ? -1 : 1;
    }
    public static final float sign(float x) {
	return (x < 0f) ? -1 : 1;
    }
    public static final double sign(double x) {
	return (x < 0d) ? -1 : 1;
    }
}
