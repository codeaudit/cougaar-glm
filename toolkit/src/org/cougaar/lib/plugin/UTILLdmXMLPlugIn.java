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

package org.cougaar.lib.plugin;

import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.AggregateAsset;

import org.cougaar.core.plugin.LDMPlugInServesLDM;
import org.cougaar.core.plugin.SimplePlugIn;

import org.cougaar.util.ConfigFinder;

import org.cougaar.lib.plugin.UTILEntityResolver;
import org.cougaar.lib.param.Param;
import org.cougaar.lib.xml.parser.ParamParser;
import org.cougaar.lib.param.ParamTable;
import org.cougaar.lib.util.UTILPlugInException;

import org.cougaar.lib.xml.parser.PrototypeParser;
import org.cougaar.lib.xml.parser.InstanceParser;
import org.cougaar.lib.xml.parser.AggregateAssetParser;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;

import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import org.apache.xerces.parsers.DOMParser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXNotRecognizedException;
import org.cougaar.core.component.StateObject;

/**
 * <pre>
 * Reads an ldm xml file and populates the Cluster with the assets 
 * found there.  
 * 
 * By default, looks for files of the form <ClusterName>.ldm.xml
 *
 * The xml files can be anywhere the ConfigFinder looks.
 *
 * For example, for TOPS, the files are in the data directory, e.g.
 *
 * /opt/alp/plugins/TOPS/data/TRANSCOM.ldm.xml
 *
 * This is not on the default ConfigFinder path, so you have to add a 
 * system parameter like this to setarguments.csh:
 *
 * -Dorg.cougaar.config.path=$COUGAAR_INSTALL_PATH/plugins/TOPS/data;
 * 
 * NOTE : the trailing semi-colon is ABSOLUTELY necessary.
 *
 * Parameters :
 *   debug   - when true, tells of each items published to log plan
 *   ldmFile - lets you specify the ldm xml file to be anything you want
 *
 * </pre>
 */

public class UTILLdmXMLPlugIn extends SimplePlugIn implements LDMPlugInServesLDM, StateObject {
  private boolean debug = "true".equals(System.getProperty("UTILLdmXMLPlugIn.debug", "false"));
  private boolean showParserFeatures = "true".equals(System.getProperty("UTILLdmXMLPlugIn.showParserFeatures", "false"));

  private String originalAgentID = null;
  
  /**
   * Implemented for StateObject
   * <p>
   * Get the current state of the Component that is sufficient to
   * reload the Component from a ComponentDescription.
   *
   * @return null if this Component currently has no state
   */
  public Object getState() {
	if (debug)
	  System.out.println ("UTILLdmXMLPlugIn.getState called ");
	
    if (originalAgentID == null)
	return getClusterIdentifier().getAddress();
    else 
	return originalAgentID;
  }

  /**
   * Implemented for StateObject
   * <p>
   * Set-state is called by the parent Container if the state
   * is non-null.
   * <p>
   * The state Object is whatever this StateComponent provided
   * in it's <tt>getState()</tt> implementation.
   * @param o the state saved before
   */
  public void setState(Object o) {
	if (debug)
	  System.out.println ("UTILLdmXMLPlugIn.setState called ");

      originalAgentID = (String) o;
  }

  /** true iff originalAgentID is not null -- i.e. setState got called */
  protected boolean didSpawn () {
	boolean val = (originalAgentID != null);
	if (debug)
	  System.out.println ("UTILLdmXMLPlugIn.didSpawn returns - " + val);
	return val;
  }

  /**
   * Load the data from the file synchronously.
   * We'll not actually do anything later on.
   *
   * Does not create assets if being rehydrated.
   */
  protected void setupSubscriptions() {
	// if we just rehydrated, all the assets created before persisting should already be
	// in the logplan
	if (!didRehydrate () && !didSpawn())
	  createAssets();
  }

  /** 
   * Do nothing function
   */
  protected void execute() {}

  /**
   * <pre>
   * Parses the ldm.xml file and creates the objects
   * that are represented in that file.  The objects are then 
   * published to the cluster.
   *
   * If the debug parameter exists and is set to true, will tell of
   * each object published to log plan.
   * </pre>
   */
  private void createAssets(){
      Document doc = getParsedDocument();

      Enumeration assets = getAssets(doc);
      while(assets.hasMoreElements()){
      	Asset asset = (Asset)assets.nextElement();
      	boolean result = publishAdd(asset);
	if (debug)
	  System.out.println ("UTILLdmXMLPlugIn.createAssets - Publishing " + asset + " to the log plan");

	if (!result)
	  System.err.println("Warning: GLMLdmXMLPlugIn not successful publishing asset " + asset);
      }
  }

  /**
   * <pre>
   * Returns a document that represents the parsed xml file returned by getFileName.
   * Uses ConfigFileFinder to find the .ldm.xml file and any xml entity references within 
   * the file.
   *
   * Need to provide an entity resolver to the parser.
   * The entity resolver is just a wrapper of the ConfigFinder.
   *
   * </pre>
   * @see #getFileName
   * @see org.cougaar.lib.plugin.UTILEntityResolver#resolveEntity
   * @see org.cougaar.util.ConfigFinder#open
   * @return document that we can query for assets
   */
  protected Document getParsedDocument () {
    //	  String debugParam = getStringParam ("debug");
    //	  debug = (debugParam != null && "true".equals (debugParam));

      String dfile = getFileName ();
      DOMParser parser = new DOMParser();
      InputStream inputStream = null;
      try {
	  inputStream = ConfigFinder.getInstance().open(dfile);
	  if (debug)
	    System.out.println ("UTILLdmXMLPlugIn.getParsedDocument - Opened "+
				dfile + 
				" input stream " + inputStream);

	  if (showParserFeatures) {
	    System.out.println ("UTILLdmXMLPlugIn.getParsedDocument - parser features:");
	    String []features = parser.getFeaturesRecognized();
	    for (int i = 0; i < features.length; i++) {
	      try {
		System.out.println ("Feature " + i + " " + features[i] + " = " + parser.getFeature(features[i]));
	      } catch (SAXNotRecognizedException saxnre) {
		System.out.println ("Feature " + i + " " + features[i] + " = not recognized excep?");
	      }
	    }
	  }

	  parser.setFeature("http://apache.org/xml/features/allow-java-encodings", true);

	  // by default set to true, adds another node to tree that confuses getAssets below,
	  // since it's not expecting a entity ref node
	  parser.setFeature("http://apache.org/xml/features/dom/create-entity-ref-nodes", false);
	  if (debug)
	    System.out.println ("UTILLdmXMLPlugIn.getParsedDocument - setting parser create entity node to "+
				parser.getCreateEntityReferenceNodes());
	  parser.setEntityResolver (new UTILEntityResolver ());
	  InputSource is = new InputSource (inputStream);
	  is.setSystemId (dfile);
	  parser.parse(is);
      }
      catch(SAXParseException saxe){
	System.out.println ("UTILLdmXMLPlugIn.getParsedDocument - got SAX Parse exception in file " + 
			    saxe.getSystemId () + " line " + saxe.getLineNumber () + " col " + saxe.getColumnNumber ()+"\n"+
			    "Exception was :");
	System.err.println(saxe.getMessage());
      }
      catch(Exception e){
	if (inputStream == null) {
	  System.out.println ("UTILLdmXMLPlugIn.getParsedDocument - could not find " + 
			      dfile + ".  Please check config path.");
	}
	System.err.println(e.getMessage());
	e.printStackTrace();
      }

      return parser.getDocument();
  }

  /**
   * <pre>
   * Looks for a file called <ClusterName>.ldm.xml OR optionally a 
   * a file with the same name as the ldmFile variable.
   *
   * Optionally prepends the "envDir" directory to the path, which
   * can be used for finer control over finding of files (usually not needed).
   *
   * Subclass to look for a file with a different extension
   * This file will probably be in the "data" directory.
   * </pre>
   */
  protected String getFileName () {
    String dfile = getStringParam ("envDir");
    if (dfile == null)
        dfile = "";

	String ldmFile = getStringParam ("ldmFile");
	if (ldmFile == null)
	  ldmFile = getClusterIdentifier().getAddress() + ".ldm.xml";
	
    dfile = dfile + ldmFile;
    return dfile;
  }
  
  /** 
   * <pre>
   * Get the value of the parameter from those that are defined in the
   * <Cluster>.ini after each plugin.
   *
   * </pre>
   * @param the name of the parameter
   * @return the parameter named <code>name</code>, if there is one defined 
   */
  String getStringParam (String name) {
    Vector envParams = getParameters();
    String paramValue = null;
    Enumeration ps = envParams.elements(); 
    while(ps.hasMoreElements() && (paramValue == null)){
      String envS = (String) ps.nextElement();
      Param p = ParamParser.getParam(envS);
      if (p != null) {
        String n = p.getName();
        try {
          if (n.equals(name)){
            paramValue = p.getStringValue();
		  }
        } 
		catch (Exception e) { 
		  System.err.println(e.getMessage());
		  e.printStackTrace();
		}
      }
    }
    return paramValue;
  }

  /**
   * Uses the PrototypeParser, InstanceParser, and AggregateAsset parser to
   * create assets as specified in the <ClusterName>.ldm.xml file.
   *
   * @see org.cougaar.lib.xml.parser.PrototypeParser#cachePrototype
   * @see org.cougaar.lib.xml.parser.InstanceParser#getInstance
   * @see org.cougaar.lib.xml.parser.AggregateAssetParser#getAggregate
   * @param doc - the input xml document (from an .ldm.xml file)
   * @return an enumeration of the created assets.
   */
  private Enumeration getAssets(Document doc) {

    Node   root    = doc.getDocumentElement();
    Vector assets  = new Vector();
    
    if(root.getNodeName().equals("AssetList")){
      NodeList  nlist    = root.getChildNodes();
      int       nlength  = nlist.getLength();
      for(int i = 0; i < nlength; i++){
	Node child = nlist.item(i);
	if(child.getNodeType() == Node.ELEMENT_NODE){
	  String childname = child.getNodeName();
	  if (debug)
	    System.out.println ("UTILLdmXMLPlugIn.getAssets - childname " + childname);

	  if(childname.equals("prototype")) {
            //System.out.println("Parsing Prototype");
	    PrototypeParser.setDebug (debug);
	    PrototypeParser.cachePrototype(getLDM(), child);
	  }
	  else if(childname.equals("instance")) {
            //System.out.println("Parsing Instance");
	    List newassets = InstanceParser.getInstance(getLDM(), child);
	    assets.addAll(newassets);
	  }
	  else if(childname.equals("AggregateAsset")) {
	    // System.out.println("Parsing AggregateAsset.");
	    AggregateAsset newasset = AggregateAssetParser.getAggregate(getLDM(), child);
	    assets.addElement(newasset);
	  }
	  /*
	  else if(childname.equals("network")){
 	    Vector net_assets = NetworkParser.getTransportationNetwork(getLDM(), 
 								       getClusterObjectFactory(), 
 								       child);
 	    for(int j = 0; j < net_assets.size(); j++){
 	      assets.addElement(net_assets.elementAt(j));
 	    }
 	  }
	  */
	}
	else {
	  if (debug)
	    System.out.println ("UTILLdmXMLPlugIn.getAssets - got non-element node " + child);
	}
      }
    }
    return assets.elements();
  }
}

