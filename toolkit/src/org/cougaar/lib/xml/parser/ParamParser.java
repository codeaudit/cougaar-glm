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

package  org.cougaar.lib.xml.parser;

import org.cougaar.lib.param.BooleanParam;
import org.cougaar.lib.param.DoubleParam;
import org.cougaar.lib.param.FloatParam;
import org.cougaar.lib.param.IntParam;
import org.cougaar.lib.param.LongParam;
import org.cougaar.lib.param.Param;
import org.cougaar.lib.param.ShortParam;
import org.cougaar.lib.param.StringParam;

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
