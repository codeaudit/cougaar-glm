/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package  org.cougaar.lib.param;

import java.util.Vector;

/**
 * Class that is used to test a ParamTable.  The table consists of
 * [name, com.bbn.tops.param.Parameter] pairs.
 *
 */
public class TestParam{

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
    catch(ParamException e){System.err.println(e.getMessage());}
    System.out.println("debug = " + b);

    try{b = pt.getBooleanParam("print");}
    catch(ParamException e){System.err.println(e.getMessage());}
    System.out.println("print = " + b);

    try{b = pt.getBooleanParam("run");}
    catch(ParamException e){System.err.println(e.getMessage());}
    System.out.println("run = " + b);

    try{b = pt.getBooleanParam("test");}
    catch(ParamException e){System.err.println(e.getMessage());}
    System.out.println("test = " + b);

    try{str = pt.getStringParam("planeType");}
    catch(ParamException e){System.err.println(e.getMessage());}
    System.out.println("planeType = " + str);

    try{s = pt.getShortParam("testShort");}
    catch(ParamException e){System.err.println(e.getMessage());}
    System.out.println("testShort = " + s);

    try{f = pt.getFloatParam("testFloat");}
    catch(ParamException e){System.err.println(e.getMessage());}
    System.out.println("testFloat = " + f);

    try{l = pt.getLongParam("testLong");}
    catch(ParamException e){System.err.println(e.getMessage());}
    System.out.println("testLong = " + l);

    try{d = pt.getDoubleParam("testDouble");}
    catch(ParamException e){System.err.println(e.getMessage());}
    System.out.println("testDouble = " + d);

    try{i = pt.getIntParam("numPlanes");}
    catch(ParamException e){System.err.println(e.getMessage());}
    System.out.println("numPlanes = " + i);
  }
}









