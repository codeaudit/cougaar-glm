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

package  org.cougaar.lib.xml.parser;

import org.cougaar.lib.param.Param;
import org.cougaar.lib.param.StringParam;
import org.cougaar.lib.param.BooleanParam;
import org.cougaar.lib.param.DoubleParam;
import org.cougaar.lib.param.FloatParam;
import org.cougaar.lib.param.IntParam;
import org.cougaar.lib.param.LongParam;
import org.cougaar.lib.param.ShortParam;

/**
 * Class that create parameters from strings.
 */
public class ParamParser {

  /**
   * no constructor
   */
  public ParamParser() {}

  /**
   * get parameter from parsed "name={type}value"
   * @param ntv name=[{type}]value
   * @return param
   */
  public Param getParam(String ntv){
    Param p = null;
    if (ntv != null) {
      int eq = ntv.indexOf("=");
      if ((eq > 0) && (eq < (ntv.length()-1))) {
	p = getParam(
	      ntv.substring(0, eq).trim(),
	      ntv.substring(eq+1).trim());
      }
    }
    return p;
  }

  /**
   * get parameter from parsed "{type}value"
   * @param n name of the parameter
   * @param tv [{type}]value of the parameter: parse "{type}value"
   * @return param
   */
  protected Param getParam(String n, String tv){
    Param p = null;
    if (tv != null) {
      int endcb = tv.indexOf('}');
      if (tv.charAt(0) == '{') {
	if ((endcb > 0) && (endcb < tv.length()-1))
	  p = getParam(n, 
	        tv.substring(1,endcb).trim(), 
		tv.substring(endcb+1).trim());
      } else {
        if (endcb < 0)
	  p = getParam(n, "String", tv);
      }
    }
    return p;
  }

  /**
   * get Parameter
   * @param n name of the parameter
   * @param t type of the parameter, null == "String"
   * @param v String value of the parameter
   * @return param
   */
  protected Param getParam(String n, String t, String v){
    Param p = null;
    try {
      if ((t == null) || (t.equals("String")))
        p = new StringParam(n, v);
      else if (t.equals("Boolean"))
        p = new BooleanParam(n, v.equalsIgnoreCase("true"));
      else if (t.equals("Double"))
        p = new DoubleParam(n, Double.parseDouble(v));
      else if (t.equals("Float"))
        p = new FloatParam(n, Float.parseFloat(v));
      else if (t.equals("Int"))
        p = new IntParam(n, Integer.parseInt(v));
      else if (t.equals("Long"))
        p = new LongParam(n, Long.parseLong(v));
      else if (t.equals("Short"))
        p = new ShortParam(n, Short.parseShort(v));
    } catch (Exception e) {
      p = null;
    }
    return p;
  }
}
