/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.plugin.ldm;


import org.cougaar.core.cluster.ClusterServesPlugIn;

import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.asset.AssetFactory;
import org.cougaar.domain.planning.ldm.asset.AggregateAsset;
import org.cougaar.domain.planning.ldm.asset.ItemIdentificationPGImpl;
import org.cougaar.domain.planning.ldm.asset.NewItemIdentificationPG;
import org.cougaar.domain.planning.ldm.asset.NewTypeIdentificationPG;
import org.cougaar.domain.planning.ldm.asset.PropertyGroup;

import org.cougaar.domain.planning.ldm.RootFactory;

import java.lang.reflect.Method;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Hashtable;

//import com.sun.xml.parser.Resolver;
//import com.sun.xml.tree.XmlDocument;

import org.xml.sax.InputSource;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;



public class XMLPrototypeProvider 
	 //extends org.cougaar.domain.mlm.plugin.ldm.PrototypeProvider
{

    private AssetFactory theAssetFactory = null;
	 private RootFactory ldmfactory = null;
	 // CHANGE THESE!
	 //  use this can handle for now, since we will be using real bumper #s
	 //  for typeid's that aren't generically definable bd 1/19/00	
	  	public boolean canHandle( String typeid ) { return true; }
	 // 	public Asset getAssetPrototype( String typeid ) { return null; }
	 // 	public String getQuery() { return null; }
	 // 	public void processRow( Object[] rowdata ) {}
	 private LDMXMLPlugIn myLDMPlugIn;
	 private ClusterServesPlugIn myCluster;

	// don't use this canHandle (this is the orig) see message above!
	 //public boolean canHandle( String typeid ) {
	//	  if ( (typeid.startsWith( "M35A2" )) ||
	//			 (typeid.startsWith( "MCC" )) ||
	//			 (typeid.startsWith( "ARBN"))) {
	//			//System.err.println( "\n" + typeid + ": RETURNING TRUE!" );
	//			return true;
	//	  }
	//	  //System.err.println( "\n" + typeid + ": RETURNING FALSE!" );
	//	  return false;
	 //}

	 public XMLPrototypeProvider(  LDMXMLPlugIn plugin, ClusterServesPlugIn cluster ) {
		  theAssetFactory = new AssetFactory();
		  this.myLDMPlugIn = plugin;
		  this.myCluster = cluster;
	 }


	 //public Asset getAssetPrototype( String typeid, XmlDocument doc ) {
	 public Asset getAssetPrototype( String typeid, Document doc ) {
		  ldmfactory = myCluster.getLDM().getFactory();
		  //Node node = findNode( typeid, doc );
		  NodeList nlist = doc.getDocumentElement().getElementsByTagName( "prototype" );
		  Element node = findPrototype( typeid, nlist );
		  try {
				if ( !myCluster.getLDM().isPrototypeCached( typeid )) {
					 String assetClass = getAssetClass( node );
					 Asset tmp_asset = (Asset)theAssetFactory.create( Class.forName( assetClass ), typeid );
					 //System.err.println( "\nTrying to register " + typeid + " of class " + assetClass );
					 //myCluster.getLDM().registerPrototype( typeid, assetClass );
					 //tmp_asset.setOtherProperties( getOtherProperties( node ));
					 getOtherProperties( tmp_asset, node );
					 myCluster.getLDM().cachePrototype( typeid, tmp_asset );
					 //System.err.println( "Registered prototype for " + assetClass );
				}
		  } catch ( Exception re ) {
				re.printStackTrace();
		  }
		  Asset pr = (Asset)myCluster.getLDM().getPrototype( typeid );
		  //NewTypeIdentificationPG p1 = (NewTypeIdentificationPG) pr.getTypeIdentificationPG();
		  //String tmptypeid = p1.getTypeIdentification();

		  return pr;
	
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
				if ( ((Element)nl.item(i)).getAttribute( "class" ).endsWith( "TypeIdentificationPG" ) )
					 continue;
				PropertyGroup prop = ldmfactory.createPropertyGroup( ((Element)nl.item(i)).getAttribute( "class" ) );
				populateProperty( prop, (Element)nl.item( i ) );
				asset.addOtherPropertyGroup( prop );
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
					 if (child.getAttribute("type").equals("int"))
					   method_args[0] = Integer.TYPE;
					 else
					   method_args[0] = Class.forName( child.getAttribute( "type" ));
					 Object[] setter_args = new Object[1];
					 setter_args[0] = child.getAttribute( "value" );
					 Class propclass = prop.getClass();
					 Method propmethod = propclass.getMethod( methodName, method_args );
					 propmethod.invoke( prop, setter_args );
				}
		  } catch ( Exception e ) {
				e.printStackTrace();
		  }
	 }

}
