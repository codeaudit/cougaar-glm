// $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/glm/util/Attic/CargoCategoryDecoder.java,v 1.2 2001-04-05 19:27:44 mthome Exp $
/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.util;

/**
 * Static class to extract some information out
 * of the cargo category codes.
 * Cargo category codes are 3 letter codes that define
 * how a piece of cargo can be transported from point A 
 * to point B.
 * @author Jose L. Herrero
 */
public final class CargoCategoryDecoder {
    
  /**
   * See if the cargo is air transportable.
   * @param code - 3 letter cargo category code
   * @return boolean
   */
  public static boolean AirTransportable(String code){
    code = code.toUpperCase();
    char c = code.charAt(1);
    return ((c != '0')&&(c != '4')&&(c != 'A')); 
  }
  
  /** 
   * See if cargo is outsized.
   * @param code - 3 letter cargo category code
   * @return boolean
   */

  public static boolean isOutsized (String code){
    code = code.toUpperCase();
    char c = code.charAt(1);
    return (( c == '1')||(c == '5')||(c == 'B'));
  }
  
  /** 
   * See if cargo is oversized.
   * @param code - 3 letter cargo category code
   * @return boolean
   */

  public static boolean isOversized (String code){
    code = code.toUpperCase();
    char c = code.charAt(1);
    return (( c == '2')||(c == '6')||(c == 'C'));
  }

  /**
     * See if the cargo fits on a C5 or a C17 Plane.
     * There is a limit to outsize a C17 can carry.  This info
     * can not be deduced from the cargo category code and must
     * be found using the length and width info. in ContainCapability.java.
     * @param code - 3 letter cargo category code
     * @return boolean
     */

    public static boolean FitsOnC5orC17(String code){
	code = code.toUpperCase();
	char c = code.charAt(1);
	// only bulk organic and oversized 
	// and outsized equipment
	return (FitsOnAnyPlane(code) ||
		FitsOnC17orC141(code) ||
		c == '1' || c == '5' || c == 'B');
    }

    /**
     * See if the cargo fits on a C17 or C141 plane
     * @param code - 3 letter cargo category code
     * @return boolean
     */
    public static boolean FitsOnC17orC141(String code){
	code = code.toUpperCase();
	char c = code.charAt(1);
	// only bulk organic and oversized equipment
	return (FitsOnAnyPlane(code) || 
		c == '2' || c == '6' || c == 'C');
    }

    /**
     * See if the cargo fits on any kind of US Military 
     * cargo plane.
     * @param code - 3 letter cargo category code
     * @return boolean
     */
    public static boolean FitsOnAnyPlane(String code){
	code = code.toUpperCase();
	char c = code.charAt(1);
	// bulk or organic cargo
	return (c == '3' || c == '7' || c == 'D' || c == '8' || c == '9'); 
    }

    /**
     * See if the cargo can go by ship.  For
     * the 1998 demo we will assume that anything
     * can go by ship.
     * @param code - 3 letter cargo category code
     * @return boolean
     */
    public static boolean FitsOnShip(String code){
	return true;
    }

    /**
     * Can be transported down american roads, if not then 
     * it must go by train.
     * @param code - 3 letter cargo category code
     * @return boolean
     */
    public static boolean IsRoadable(String code){
	code = code.toUpperCase();
	char c = code.charAt(0);
	return (c != 'A');
    }
    
    /**
     * Roadable vehicles, may be hazardouz, may require security
     * @param code - 3 letter cargo category code
     * @return boolean
     */
    public static boolean IsSelfTransportable(String code){
	code = code.toUpperCase();
	char c = code.charAt(0);
	return (c == 'K' || c == 'L' || c == 'R');
    }

    /**
     * Cargo can be put in a 20 ft container
     * @param code - 3 letter cargo category code
     * @return boolean
     */
    public static boolean Is20FtContainarizable(String code){
	code = code.toUpperCase();
	char c = code.charAt(2);
	return (c == 'B');
    }

    /**
     * Cargo can be put in a 20 ft container
     * @param code - 3 letter cargo category code
     * @return boolean
     */
    public static boolean Is40FtContainarizable(String code){
	code = code.toUpperCase();
	char c = code.charAt(2);
	return (c == 'C');
    }

    /**
     * See if some cargo can be transported by train
     * For the 1998 demo we assume that all vehicles are
     * trainable.
     * @param code - 3 letter cargo category code
     * @return boolean
     */
    public static boolean IsTrainable(String code){
	return true;
    }

    /**
     * See if we are a Roll on Roll off vehicle
     * @param code - 3 letter cargo category code
     * @return boolean
     **/
  public static boolean isRORO(String code){
    code = code.toUpperCase();
    char c = code.charAt(0);
    return c == 'A' || c == 'K' || c== 'L' || c == 'R';
  }

    /**
     * See if we are Ammo
     * @param code - 3 letter cargo category code
     * @return boolean
     **/
  public static boolean isAmmo(String code){
    code = code.toUpperCase();
    char c = code.charAt(0);
    return c == 'M' || c == 'N' || c== 'P';
  } 
}
