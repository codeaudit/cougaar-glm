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

package org.cougaar.mlm.plugin.ldm;


import java.lang.reflect.Method;

import org.cougaar.planning.ldm.ClusterServesPlugin;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.AssetFactory;
import org.cougaar.planning.ldm.asset.PropertyGroup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;



public class XMLPrototypeProvider 
	 //extends org.cougaar.mlm.plugin.ldm.PrototypeProvider
{

    private AssetFactory theAssetFactory = null;
	 private PlanningFactory ldmfactory = null;
	 // CHANGE THESE!
	 //  use this can handle for now, since we will be using real bumper #s
	 //  for typeid's that aren't generically definable bd 1/19/00	
	  	public boolean canHandle( String typeid ) { return true; }
	 // 	public Asset getAssetPrototype( String typeid ) { return null; }
	 // 	public String getQuery() { return null; }
	 // 	public void processRow( Object[] rowdata ) {}
	 private LDMXMLPlugin myLDMPlugin;
	 private ClusterServesPlugin myCluster;

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

	 public XMLPrototypeProvider(  LDMXMLPlugin plugin, ClusterServesPlugin cluster ) {
		  theAssetFactory = new AssetFactory();
		  this.myLDMPlugin = plugin;
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
