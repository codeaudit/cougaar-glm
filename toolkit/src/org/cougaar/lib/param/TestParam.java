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

package  org.cougaar.lib.param;

import java.util.Vector;

import org.cougaar.util.log.Logger;
import org.cougaar.util.log.LoggerFactory;

/**
 * Class that is used to test a ParamTable.  The table consists of
 * [name, com.bbn.tops.param.Parameter] pairs.
 *
 */
public class TestParam{
  // this is OK since it's just for testing
  private static Logger logger=LoggerFactory.getInstance().createLogger("TestParam");

  /**
   * Test main function
   */
  public void main(String[] argv){
    Vector v = new Vector();
    v.addElement("envDir={String}.");
    v.addElement("envFile={String}testparam.xml");
    ParamTable pt = new ParamTable(logger);
    pt.addIniParameters (v);
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









