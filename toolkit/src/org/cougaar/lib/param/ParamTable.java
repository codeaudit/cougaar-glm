/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.param;

import org.cougaar.util.ConfigFinder;

import java.io.File;
import java.io.InputStream;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.xml.sax.DocumentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.HandlerBase;
import org.xml.sax.InputSource;
import org.xml.sax.Parser;
import org.xml.sax.helpers.ParserFactory;

import org.cougaar.lib.plugin.UTILEntityResolver;
import org.cougaar.lib.xml.parser.ParamParser;

/**
 * Class that encapsulates a parameter table.  The table consists of
 * [name, com.bbn.tops.param.Parameter] pairs.  If a parameter does not exist,
 * a ParamException will be thrown. this is not a RuntimeException,
 * so clients must use the try{}catch{} trick.  The catch block is a 
 * good place to put "default" values for the parameters.
 *
 */
public class ParamTable{

  /**
   * No default Constructor.
   */
  private ParamTable() {}

  /**
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
   * @param envParams vector of ParamParse-able strings
   */
  public ParamTable(Vector envParams){
    paramTable = new Hashtable();

    // environment parameters are "name={type}value" or "name=StringValue"
    // parse parameters.  Remember these special ones: envDir, envFile

    String envDir  = null;
    String envFile = null;
    Vector envPV   = new Vector();

    Enumeration ps = envParams.elements(); 
    while(ps.hasMoreElements()){
      String envS = (String) ps.nextElement();
      Param p = ParamParser.getParam(envS);
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
	  System.err.println(e.getMessage());
	  e.printStackTrace();
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
    try {
	InputStream inputStream = ConfigFinder.getInstance().open(pfile);

	// must specify which parser to use.
	Parser parser = ParserFactory.makeParser("com.ibm.xml.parsers.SAXParser");
	parser.setDocumentHandler(new ParamHandler(this));
	parser.setEntityResolver (new UTILEntityResolver ());
	parser.parse(new InputSource (inputStream));
    }
    catch(Exception e){
      System.err.println(e.getMessage());
      e.printStackTrace();
//        System.out.println ("Config path is " + ConfigFinder.configPath);
    }

    // add environment parameters, replacing any from file
    for (Enumeration pv = envPV.elements(); pv.hasMoreElements();) {
      Param p = (Param) pv.nextElement();
      addParam(p.getName(), p);
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

  public static String getInstallRelativeEnvDir (final String envDir) {
    File envDirectory = new File(envDir);
    String installRelativeDir = new String (envDir);
    if (!envDirectory.exists() && !envDirectory.isAbsolute()) {
      if (envDir.startsWith ("../")) {
	installRelativeDir = 
	  getWithSlashAppended (System.getProperties().getProperty("org.cougaar.install.path")) + 
	  envDir.substring (3, envDir.length ());
      } else {
	installRelativeDir = 
	  getWithSlashAppended (System.getProperties().getProperty("org.cougaar.install.path")) + 
	  envDir;
      }
    }
    return getWithSlashAppended (installRelativeDir);
  }

  /**
   * Add a parameter into the parameter table.
   * @param n name of the paramter
   * @param p the parameter to add
   */
  void addParam(String n, Param p){
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

  public String toString () {
    StringBuffer sb = new StringBuffer ();
    for (Iterator i = paramTable.keySet().iterator ();
         i.hasNext (); ) {
      Object key = i.next ();
      sb.append ("" + key + "->" + paramTable.get(key) + "\n");
    }
    return sb.toString ();
  }

  private Hashtable paramTable;
}

