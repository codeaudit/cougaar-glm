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

package org.cougaar.domain.mlm.plugin.ldm;
import org.cougaar.util.StateModelException;
import org.cougaar.core.cluster.SubscriberException;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.asset.PropertyGroup;
import org.cougaar.domain.planning.ldm.asset.AssetFactory;
import org.cougaar.domain.planning.ldm.asset.NewPropertyGroup;

import org.cougaar.domain.planning.ldm.plan.Schedule;

import org.cougaar.domain.planning.ldm.RootFactory;
import org.cougaar.domain.planning.ldm.FactoryException;

import org.cougaar.domain.planning.ldm.DomainService;
import org.cougaar.domain.planning.ldm.PrototypeRegistryService;
import org.cougaar.core.plugin.LDMService;
import org.cougaar.core.component.ServiceRevokedListener;
import org.cougaar.core.component.ServiceRevokedEvent;


// This is only for interim use!
import org.cougaar.domain.planning.ldm.plan.NewRoleSchedule;

import org.cougaar.core.plugin.ComponentPlugin;
 
import java.lang.reflect.Method;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Hashtable;
import java.util.Date;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;

import java.text.DateFormat;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An instance of an LDMPlugIn that reads a Cluster's startup data
 * from an XML file (of the form *.ldm.xml).
 *
 * This PlugIn is invoked with one parameter, the name of the
 * .ldm.xml file to be parsed.  This file is currently looked for 
 * in the local directory.  Additional file search capabilities will
 * be added.  Example from a sample cluster.ini file:
 * <PRE>
 * plugin=org.cougaar.domain.mlm.plugin.sql.LDMXMLPlugIn( foo.ldm.xml )
 * </PRE>
 *
 * Modified to request specific services from ComponentPlugin instead of SimplePlugIn
 * and PlugInAdapter.
 */
public class LDMXMLPlugIn extends LDMEssentialPlugIn
{
  
  private Properties globalParameters = new Properties();
  private String xmlfilename;
  private File XMLFile;
  private Enumeration assets;
  private Document doc;
  private RootFactory theFactory = null;
  DomainService domainService = null;
  PrototypeRegistryService protregService = null;    
  
  public LDMXMLPlugIn() {}
  
  protected void setupSubscriptions() {
    
    // get the domain service  	
    if (theFactory == null) {
      domainService = (DomainService) getBindingSite().getServiceBroker().getService(
										     this, DomainService.class,
										     new ServiceRevokedListener() {
											 public void serviceRevoked(ServiceRevokedEvent re) {
											   theFactory = null;
											 }
										       });
      
      // get the registry service  	
      protregService = (PrototypeRegistryService) getBindingSite().getServiceBroker().getService(                                      this, PrototypeRegistryService.class,
																       new ServiceRevokedListener() {
																	   public void serviceRevoked(ServiceRevokedEvent re) {
																	     theFactory = null;
																	   }
																	 });
    }
    
    
    if (getBlackboardService().didRehydrate()) return; // Assets already added after rehydration
    
    theFactory = domainService.getFactory();
    try {
      getParams();
      parseXMLFile();
    } catch ( SubscriberException se ) {
      se.printStackTrace();
    }
  }

  /** 
   * Do nothing
   */
  public void execute() {}
  
  
  /**
   * Parse parameters passed to PlugIn
   */
  private void getParams() {
    //Collection pv = getParameters();			
    Vector pv = getParameters();
    if ( pv == null ) {
      throw new RuntimeException( "LDMXMLPlugIn requires a parameter" );
    } else {
      try {
	Enumeration ps = pv.elements();
	String p = (String) ps.nextElement();
	globalParameters.put( "XMLFile", p );
	xmlfilename = p;
      } catch( Exception e ) {
	e.printStackTrace();
      }
    }
  }
  
  private void parseXMLFile() {
    try {
      doc = getConfigFinder().parseXMLConfigFile(xmlfilename);
      assets = getAssets( doc );
      while(assets.hasMoreElements()){
	Asset asset = (Asset)assets.nextElement();
	getBlackboardService().publishAdd(asset);
      }
    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }
  
  Enumeration getAssets( Document doc ){
    Properties pt = null;
    Node root = doc.getDocumentElement();
    Enumeration a = null;
    if( root.getNodeName().equals( "clusterassets" )){
      //NodeList nlist = root.getChildNodes();
      //int nlength = nlist.getLength();
      
      // The following call registers all protoypes
      // found in our document, and returns to us
      // all the instances found in the document.
      //XMLAssetCreator xac = new XMLAssetCreator( this, getCluster() );
      assets = getLDMAssets( doc );
    }
    else{
      throw new RuntimeException("unrecognized field: "+
				 root.getNodeName());
    }
    return assets;
  }
  
  public Asset getPrototype( String atypename , Class hint) {
    return null;
  }
  
  public Asset getPrototype( String atypename ) {
    return null;
  }
  
  public Asset getAssetPrototype( String typeid ) {
    Asset proto = protregService.getPrototype(typeid); // not through ldm anymore 
    if (proto == null) {
      NodeList nlist = doc.getDocumentElement().getElementsByTagName( "prototype" );
      Element node = findPrototype( typeid, nlist );
      String assetClass = getAssetClass( node );
      
      proto = theFactory.createPrototype(assetClass, typeid);
      protregService.fillProperties(proto);
      
      protregService.cachePrototype(typeid, proto);
    }
    return proto;
  }
  
  private Element findPrototype( String typeid, NodeList nlist ) {
    int size = nlist.getLength();
    for ( int i = 0; i < size; i++ ) {
      Element child = (Element)nlist.item( i );
      if ( child.getAttribute( "name" ).equals( typeid ))
	return child;
    }
    return null;
  }
  
  
  private String getAssetClass( Element node ) {
    NodeList nl = node.getElementsByTagName( "object" );
    int size = nl.getLength();
    for ( int i = 0; i < size; i++ ) {
      Element child = (Element)nl.item( i );
      return child.getAttribute( "class" );
    }
    return null;
  }
  
  private void getOtherProperties( Asset asset, Element node ) {
    NodeList nl = node.getElementsByTagName( "property" );
    int size = nl.getLength();
    for ( int i = 0; i < size; i++ ) {
      NewPropertyGroup prop = createPG(((Element)nl.item(i)).getAttribute( "class" ));
      populateProperty( prop, (Element)nl.item( i ) );
      asset.setPropertyGroup( prop );
    }
  }
  
  
  private void populateProperty( PropertyGroup prop, Element node ) {
    try {
      NodeList props = node.getElementsByTagName( "field" );
      int props_size = props.getLength();
      for ( int i = 0; i < props_size; i++ ) {
	Element child = (Element)props.item( i );
	String methodName = new String( "set" + child.getAttribute( "name" ) ); 
	Class[] method_args = new Class[1];
	String type = child.getAttribute( "type" );
	method_args[0] = resolveClass(type);
	Object[] setter_args = new Object[1];
	setter_args[0] = resolveArgument(method_args[0], child.getAttribute( "value" ));
	Class propclass = prop.getClass();
	Method propmethod = propclass.getMethod( methodName, method_args );
	propmethod.invoke( prop, setter_args );
      }
    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }
  
  /**
   * Convert a string value to an object of the type specified.
   */
  private Object resolveArgument(Class type, String value) throws ClassNotFoundException {
    if (type == java.lang.Boolean.TYPE)
      return new java.lang.Boolean(value);
    else if (type == java.lang.Character.TYPE)
      return new java.lang.Character(value.charAt(0));
    else if (type == java.lang.Byte.TYPE)
      return new java.lang.Byte(value);
    else if (type == java.lang.Short.TYPE)
      return new java.lang.Short(value);
    else if (type == java.lang.Integer.TYPE)
      return new java.lang.Integer(value);
    else if (type == java.lang.Long.TYPE)
      return new java.lang.Long(value);
    else if (type == java.lang.Float.TYPE)
      return new java.lang.Float(value);
    else if (type == java.lang.Double.TYPE)
      return new java.lang.Double(value);
    else if (type == java.lang.Void.TYPE)
      return null;
    else return value;
  }

  /**
   * Return a class object for the string classname provided.
   * Will resolve primitives to their "Class" types.
   */
  private Class resolveClass(String type) throws ClassNotFoundException {
    if (type.equals("boolean"))
      return java.lang.Boolean.TYPE;
    else if (type.equals("char"))
      return java.lang.Character.TYPE;
    else if (type.equals("byte"))
      return java.lang.Byte.TYPE;
    else if (type.equals("short"))
      return java.lang.Short.TYPE;
    else if (type.equals("int"))
      return java.lang.Integer.TYPE;
    else if (type.equals("long"))
      return java.lang.Long.TYPE;
    else if (type.equals("float"))
      return java.lang.Float.TYPE;
    else if (type.equals("double"))
      return java.lang.Double.TYPE;
    else if (type.equals("void"))
      return java.lang.Void.TYPE;
    else return Class.forName(type);
  }
  
  public void fillProperties( Asset asset ) {
  }
  
  
  private Enumeration getLDMAssets( Document doc ) {
    RootFactory theFactory = domainService.getFactory();
    Vector assets = new Vector();
    
    NodeList prototypes = doc.getElementsByTagName( "prototype" );
    int proto_size = prototypes.getLength();
    for ( int i = 0; i < proto_size; i++ ) {
      Node child = prototypes.item( i );
      Asset prototype = getAssetPrototype( child.getAttributes().getNamedItem( "name" ).getNodeValue());
    }
    
    NodeList instances = doc.getElementsByTagName( "instance" );
    int inst_size = instances.getLength();
    for ( int j = 0; j < inst_size; j++ ) {
      Node child = instances.item( j );
      NodeList schedules = ((Element)child).getElementsByTagName( "schedule" );
      // Really only expecting one Schedule here, but...
      int sched_size = schedules.getLength();
      Schedule sched = null;
      for ( int k = 0; k < sched_size; k++ ) {
	try {
	  Node scheduleNode = schedules.item( k );
	  DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
	  Date start = df.parse( scheduleNode.getAttributes().getNamedItem( "start" ).getNodeValue() );
	  Date end = df.parse( scheduleNode.getAttributes().getNamedItem( "end" ).getNodeValue() );
	  sched = theFactory.newSimpleSchedule(start, end);
	} catch ( Exception e ) {
	  e.printStackTrace();
	}
      }
      String nomen = child.getAttributes().getNamedItem( "id" ).getNodeValue();
      String protoname = child.getAttributes().getNamedItem( "prototype" ).getNodeValue();
      
      Asset myAsset = theFactory.createInstance( protoname, nomen );
      ((NewRoleSchedule)myAsset.getRoleSchedule()).setAvailableSchedule( sched );
      assets.addElement( myAsset );
    } 
    return assets.elements();
  }
  
  private NewPropertyGroup createPG(String propname) {
    NewPropertyGroup tmp_prop = null;
    try {
      tmp_prop = (NewPropertyGroup)theFactory.createPropertyGroup(propname);
    } catch ( FactoryException fe ) {
      try {
	tmp_prop = (NewPropertyGroup)theFactory.createPropertyGroup(Class.forName(propname));
      } catch ( ClassNotFoundException cnf ) {
	cnf.printStackTrace();
            }
    } 
    return tmp_prop;
  }
  
}
