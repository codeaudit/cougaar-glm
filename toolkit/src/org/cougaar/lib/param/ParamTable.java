/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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

package org.cougaar.lib.param;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.xerces.parsers.SAXParser;
import org.cougaar.lib.plugin.UTILEntityResolver;
import org.cougaar.lib.xml.parser.ParamParser;
import org.cougaar.util.ConfigFinder;
import org.cougaar.util.log.Logger;
import org.xml.sax.InputSource;

/**
 * <pre>
 * Class that encapsulates a parameter table.  The table consists of
 * [name, org.cougaar.lib.param.ParamTable] pairs.  If a parameter does not exist,
 * a ParamException will be thrown. this is not a RuntimeException,
 * so clients must use the try{}catch{} trick.  The catch block is a 
 * good place to put "default" values for the parameters.
 * </pre>
 */
public class ParamTable implements ParamMap {
  /**
   * No default Constructor.
   */
  private ParamTable() {}

  /**
   * <pre>
   * Constructor. In the act of building a the parameter handler
   * we pass that class a pointer to ourselves so that it can 
   * call our addParam() method.
   *
   * Need to provide an entity resolver to the parser.
   * The entity resolver is just a wrapper of the configFinder.
   * The resolver is expected to resolve a systemID, where:
   *
   * systemID is of the form : file:/ferris/bueller/day
   * user.dir is             :      /ferris/bueller
   * and we want just the last part (day)
   * NOTE: on NT -->
   * systemID is of the form : file:/C:/ferris/bueller/day
   * user.dir is             :       C:/ferris/bueller
   * No slash in front of the path on the user.dir.
   *
   * envParams MUST have a param "envFile".  It specifies the parse file!
   *
   * </pre>
   * @param envParams vector of ParamParse-able strings
   */
  public ParamTable(Logger logger){
    paramTable = new HashMap ();
    paramParser = new ParamParser ();
    this.logger = logger;
  }

  protected void addIniParameters (Vector envParams) {
    // environment parameters are "name={type}value" or "name=StringValue"
    // parse parameters.  Remember these special ones: envDir, envFile

    String envDir  = null;
    String envFile = null;
    Vector envPV   = new Vector();

    Enumeration ps = envParams.elements(); 
    while(ps.hasMoreElements()){
      String envS = (String) ps.nextElement();
      Param p = paramParser.getParam(envS);
      if (p != null) {
        envPV.add(p);
        String n = p.getName();
        try {
          if (n.equals("envDir")){
            envDir = p.getStringValue();
	  }
          else if (n.equals("envFile")){
            envFile = p.getStringValue();
	  }
        } 
	catch (Exception e) { 
	  logger.error(e.getMessage(), e);
	}
      }
    }

    // set env dir.
    if (envDir == null)
      envDir = "";
    else {
      // append slash if needed.
      envDir = getWithSlashAppended (envDir);
    }

    String pfile = envDir + envFile;

    populateParamMap (pfile);
	
    // add environment parameters, replacing any from file
    for (Enumeration pv = envPV.elements(); pv.hasMoreElements();) {
      Param p = (Param) pv.nextElement();
      addParam(p.getName(), p);
    }
  }

  /** 
   * Given the XML parameter file <code>pfile</code>, parse it and <p>
   * use the ParamHandler to populate the parameter map.
   *
   * Also uses UTILEntityResolver to find any entities that have   <p>
   * been included in the xml file.  The Entity Resolver uses the  <p>
   * ConfigFinder to find referenced files.
   *
   * @param pfile parameter file to read
   * @see org.cougaar.lib.param.ParamHandler#startElement
   * @see org.cougaar.lib.plugin.UTILEntityResolver#resolveEntity
   **/
  protected void populateParamMap (String pfile) {
    try {
      InputStream inputStream = ConfigFinder.getInstance().open(pfile);

      SAXParser parser = new SAXParser();
      parser.setContentHandler(new ParamHandler(this));
      parser.setEntityResolver (new UTILEntityResolver (logger));
      InputSource is = new InputSource (inputStream);
      is.setSystemId (pfile);
      parser.parse(is);
    }
    catch(Exception e){
      if (logger.isDebugEnabled ()) {
	logger.debug(e.getMessage(), e);
      }
      if (logger.isInfoEnabled() && (e instanceof FileNotFoundException)) {
	logger.info("ParamTable.populateParamMap - could not find env.xml file : "+
		    e.getMessage() + "\nTurn this message off by setting logger to normal setting.");
      }
    }
  }
  
  /** append slash if needed. */
  static String getWithSlashAppended (final String envDir) {
    String copy = new String(envDir);
    char lastDirChar = copy.charAt(copy.length()-1);
    if ((lastDirChar != '/') && (lastDirChar != '\\')) {
      int bwSlash = copy.indexOf('\\');
      if (bwSlash > 0){
	copy += "\\";
      }
      else{
	copy += "/";
      }
    }
    return copy;
  }

  /**
   * Add a parameter into the parameter table.
   * @param n name of the paramter
   * @param p the parameter to add
   */
  public void addParam(String n, Param p){
    paramTable.put(n, p);
  }
  
  /**
   * Get the value of a boolean parameter.
   * @param name of the parameter to get
   * @return the boolean value of the parameter
   */
  public boolean getBooleanParam(String name) throws ParamException{
    if(paramTable.containsKey(name)){
      Param p = (Param)paramTable.get(name);
      return p.getBooleanValue();
    }
    throw new ParamException("parameter " + name + " not found");
  }

  /**
   * Get the value of a double parameter.
   * @param name of the parameter to get
   * @return the double value of the parameter
   */
  public double getDoubleParam(String name) throws ParamException{
    if(paramTable.containsKey(name)){
      Param p = (Param)paramTable.get(name);
      return p.getDoubleValue();
    }
    throw new ParamException("parameter " + name + " not found");
  }
  
  /**
   * Get the value of a float parameter.
   * @param name of the parameter to get
   * @return the float value of the parameter
   */
  public float getFloatParam(String name) throws ParamException{
    if(paramTable.containsKey(name)){
      Param p = (Param)paramTable.get(name);
      return p.getFloatValue();
    }
    throw new ParamException("parameter " + name + " not found");
  }
  
  /**
   * Get the value of a int parameter.
   * @param name of the parameter to get
   * @return the int value of the parameter
   */
  public int getIntParam(String name) throws ParamException{
    if(paramTable.containsKey(name)){
      Param p = (Param)paramTable.get(name);
      return p.getIntValue();
    }
    throw new ParamException("parameter " + name + " not found");
  }
  
  /**
   * Get the value of a long parameter.
   * @param name of the parameter to get
   * @return the long value of the parameter
   */
  public long getLongParam(String name) throws ParamException{
    if(paramTable.containsKey(name)){
      Param p = (Param)paramTable.get(name);
      return p.getLongValue();
    }
    throw new ParamException("parameter " + name + " not found");
  }

  /**
   * Get the value of a short parameter.
   * @param name of the parameter to get
   * @return the short value of the parameter
   */
  public short getShortParam(String name) throws ParamException{
    if(paramTable.containsKey(name)){
      Param p = (Param)paramTable.get(name);
      return p.getShortValue();
    }
    throw new ParamException("parameter " + name + " not found");
  }

  /**
   * Get the value of a String parameter.
   * @param name of the parameter to get
   * @return the String value of the parameter
   */
  public String getStringParam(String name) throws ParamException{
    if(paramTable.containsKey(name)){
      Param p = (Param)paramTable.get(name);
      return p.getStringValue();
    }
    throw new ParamException("parameter " + name + " not found");
  }

  /** is the parameter in the table? */
  public boolean hasParam (String name) { return paramTable.containsKey(name); }

  /** show the parameters in the map **/
  public String toString () {
    StringBuffer sb = new StringBuffer ();
    List keys = new ArrayList (paramTable.keySet());
    Collections.sort (keys);
	
    for (Iterator i = keys.iterator (); i.hasNext (); ) {
      Object key = i.next ();
      sb.append ("" + key + "->" + paramTable.get(key) + "\n");
    }
    return sb.toString ();
  }

  private Map paramTable;
  protected Logger logger;
  protected ParamParser paramParser;
}

