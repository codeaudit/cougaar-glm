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

package  org.cougaar.lib.param;

import java.util.Vector;
import org.cougaar.util.log.*;

/**
 * Class that is used to test a ParamTable.  The table consists of
 * [name, com.bbn.tops.param.Parameter] pairs.
 *
 */
public class TestParam{
  private static Logger logger=LoggerFactory.getInstance().createLogger("TestParam");

  /**
   * Test main function
   */
  public static void main(String[] argv){
    Vector v = new Vector();
    v.addElement("envDir={String}.");
    v.addElement("envFile={String}testparam.xml");
    ParamTable pt = new ParamTable(v);
    boolean b = false;
    short s = 20;
    int i = 50;
    float f = 1000;
    double d = 1000.0;
    String str = "empty";
    long l = 50000;

    try{b = pt.getBooleanParam("debug");}
    catch(ParamException e){logger.error(e.getMessage());}
    logger.debug("debug = " + b);

    try{b = pt.getBooleanParam("print");}
    catch(ParamException e){logger.error(e.getMessage());}
    logger.debug("print = " + b);

    try{b = pt.getBooleanParam("run");}
    catch(ParamException e){logger.error(e.getMessage());}
    logger.debug("run = " + b);

    try{b = pt.getBooleanParam("test");}
    catch(ParamException e){logger.error(e.getMessage());}
    logger.debug("test = " + b);

    try{str = pt.getStringParam("planeType");}
    catch(ParamException e){logger.error(e.getMessage());}
    logger.debug("planeType = " + str);

    try{s = pt.getShortParam("testShort");}
    catch(ParamException e){logger.error(e.getMessage());}
    logger.debug("testShort = " + s);

    try{f = pt.getFloatParam("testFloat");}
    catch(ParamException e){logger.error(e.getMessage());}
    logger.debug("testFloat = " + f);

    try{l = pt.getLongParam("testLong");}
    catch(ParamException e){logger.error(e.getMessage());}
    logger.debug("testLong = " + l);

    try{d = pt.getDoubleParam("testDouble");}
    catch(ParamException e){logger.error(e.getMessage());}
    logger.debug("testDouble = " + d);

    try{i = pt.getIntParam("numPlanes");}
    catch(ParamException e){logger.error(e.getMessage());}
    logger.debug("numPlanes = " + i);
  }
}









