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


import org.cougaar.core.cluster.ClusterServesPlugIn;

import org.cougaar.domain.planning.ldm.asset.Asset;

import org.cougaar.domain.planning.ldm.RootFactory;
import org.cougaar.domain.planning.ldm.plan.Schedule;

import org.cougaar.domain.planning.ldm.LDMServesPlugIn;

import org.cougaar.core.plugin.PlugInAdapter;

import org.cougaar.domain.planning.ldm.plan.NewRoleSchedule;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Hashtable;
import java.util.Date;

import java.text.DateFormat;

//import com.sun.xml.parser.Resolver;
//import com.sun.xml.tree.XmlDocument;

import org.xml.sax.InputSource;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class XMLAssetCreator //extends QueryHandler
{


  // MUST CHANGE THIS!!!
  //public String getQuery() { return null; }
  //public void processRow( Object[] rowdata ) {}
	
  private LDMXMLPlugIn myLDMPlugIn;
  private ClusterServesPlugIn myCluster;

  public XMLAssetCreator( LDMXMLPlugIn plugin, ClusterServesPlugIn cluster ) {
    this.myLDMPlugIn = plugin;
    this.myCluster = cluster;
  }


  public Enumeration getLDMAssets( Node node, RootFactory ldmf ) {
    RootFactory ldmfactory = myCluster.getLDM().getFactory();
    Vector assets = new Vector();
    NodeList nlist = node.getChildNodes();
    int nlength = nlist.getLength();
    Asset pr = null;
    Asset myasset = null;
    String protoname = null;
    for ( int i = 0; i < nlength; i++ ) {
      Node child = nlist.item( i );
      if ( child.getNodeType() == Node.ELEMENT_NODE ) {
        if ( child.getNodeName().equals( "prototype" )) {
          protoname = child.getAttributes().getNamedItem( "name" ).getNodeValue();
          //System.err.println( "\nprotoname = " + protoname );
          pr = myLDMPlugIn.getPrototype( protoname );
          //System.err.println( "\npr = " + pr );
          //String bumper = "foo"; //remember to change this!
          //System.err.println( "\nCreating Asset " + pr );
          //pr = (Asset)ldmfactory.create( protoname, bumper );
          //assets.addElement( pr );
        } else if ( child.getNodeName().equals( "instance" )) {
          //System.err.println( "\nCreating Asset " + pr );
          NodeList sched_list = child.getChildNodes();
          int slen = sched_list.getLength();
          // Really only expecting one Schedule here, but...
          Schedule sched = null;
          for ( int j = 0; j < slen; j++ ) {
            try {
              Node iChild = sched_list.item( j );
              if ( iChild.getNodeType() == Node.ELEMENT_NODE ) {
                DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
                Date start = df.parse( iChild.getAttributes().getNamedItem( "start" ).getNodeValue() );
                Date end = df.parse( iChild.getAttributes().getNamedItem( "end" ).getNodeValue() );
                sched = ldmfactory.newSimpleSchedule( start, end );
              }
            } catch ( Exception e ) {
              e.printStackTrace();
            }
          }
          String bumper = child.getAttributes().getNamedItem( "id" ).getNodeValue();
          //pr = (Asset)ldmfactory.create( protoname, bumper );
          //if ( pr != null ) {
          myasset = ldmfactory.createInstance( protoname, bumper );
          //} else
          //	continue;
          ((NewRoleSchedule)pr.getRoleSchedule()).setAvailableSchedule( sched );
						  
          assets.addElement( myasset );
        }
      }
    }
    return assets.elements();
  }

}