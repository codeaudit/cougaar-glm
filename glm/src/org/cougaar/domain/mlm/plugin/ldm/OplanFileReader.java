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


import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;

import org.cougaar.core.cluster.UIDServer;
import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.core.cluster.ClusterServesPlugIn;
import org.cougaar.core.society.UID;

import org.cougaar.domain.planning.ldm.RootFactory;
import org.cougaar.domain.planning.ldm.measure.Longitude;
import org.cougaar.domain.planning.ldm.measure.Latitude;
import org.cougaar.domain.planning.ldm.policy.Policy;

import org.cougaar.domain.glm.ldm.plan.GeolocLocationImpl;
import org.cougaar.domain.mlm.plugin.ldm.XMLPolicyCreator;

//import org.cougaar.domain.glm.*;
//import org.cougaar.domain.glm.ldm.*;
//import org.cougaar.domain.glm.ldm.plan.*;
//import org.cougaar.domain.glm.ldm.asset.*;
import org.cougaar.domain.glm.ldm.oplan.*;


/**
  This class is used to parse the given oplan xml file and instantiates oplan objects
  accordingly.  The oplan xml file is retrieved from the Oplan object.  Once retrieved
  The OplanFileReader parses the Nodes and attributes of the xml file.
  
*/

public class OplanFileReader {

  private String xmlfilename;
  private File XMLFile;
  private Document doc;
    
  // Reference back to the Oplan object
  private Oplan thePlan_;
  private ArrayList theOrgActivities_ = new ArrayList();
  private ArrayList theOrgRelations_ = new ArrayList();
  private ArrayList theForcePackages_ = new ArrayList();
  private ArrayList thePolicies_ = new ArrayList();
  
  private static final String OPLANID = "oplanID";
  private static final String OPERATIONNAME = "operationName";
  private static final String PRIORITY = "priority";
  private static final String CDAY = "cDay";
  private static final String ENDDAY = "endDay";
		
  // TimeSpan attributesth
  private static final String TIMESPAN = "timespan";
  private static final String TS_STARTTIME = "startTime";
  private static final String TS_THRUTIME = "thruTime";
	
  // Policy attributes
  private static final String OPLANPOLICY = "Policies";
  private static final String POLICY="Policy";
  private static final String KEYVALUE = "keyvalue";
  private static final String POLICYNAME = "policyname";

  // TheaterInfo
  //private static final String THEATERINFO = "theaterinfo";
  private static final String THEATERID = "theaterId";
  private static final String TERRAINTYPE = "terrainType";
  private static final String SEASON = "season";
  private static final String ENEMYFORCETYPE = "enemyForceType";
  private static final String HOSTNATIONPOL = "hostNationPOLSupport";
  private static final String HOSTNATIONPOLCAPABILITY = "hostNationPOLCapability";
  private static final String HOSTNATIONWATERSUPPORT = "hostNationWaterSupport";
  private static final String HOSTNATIONWATERCAPABILITY = "hostNationWaterCapability";
	
  // DFSP
  private static final String DFSP = "dfsp";
  private static final String DFSPNAME = "name";
	
  private static final String GEOLOC = "geoloc";
  private static final String GEOLOCNAME = "name";
  private static final String GEOLOCCODE = "code";
  private static final String GEOLOCLAT = "lat";
  private static final String GEOLOCLONG = "long";
								
	
  // TransportInfo
  //private static final String TRANSPORTINFO = "transportinfo";
	
  // POD
  private static final String POD = "pod";
  private static final String PODTYPE = "type";
  private static final String PODVALUE = "value";
	
  // Organization
  private static final String ORGANIZATION = "organization";	
  private static final String ORGID = "OrgId";
  private static final String ORGDEPLOYREQ = "orgdeploymentrequirements";
  private static final String ORGACTIVITY = "orgactivity";
  private static final String ORGRELATION = "orgrelation";
	
  // OrgActivity
  private static final String ACTIVITYTYPE = "ActivityType";
  private static final String ACTIVITYNAME = "ActivityName";
  private static final String OPTEMPO = "OpTempo";
  private static final String KEY = "key";
  private static final String VALUE = "value";
  //	private static final String TAALOC = "TAALoc";
  //	private static final String TAAEAD = "TAAEAD";
  //	private static final String TAALAD = "TAALAD";
	
		   
  // ForcePackage	   
  private static final String FORCEPACKAGE = "forcepackage";
  private static final String  FORCEPACKAGEID= "ForcePackageId";
  private static final String ASSIGNEDROLE = "AssignedRole";
    
  // OrgRelation
  private static final String RELATIONTYPE = "RelationType";
  private static final String OTHERORGID = "OtherOrgId";
    
  private String oplanID;
  private UID oplanUID;
  private String orgID;
  private RootFactory ldmF;
  private UIDServer uids;
  private ClusterIdentifier cid;
  private ClusterServesPlugIn theCluster;
				
  private OplanFileReader() {} //OplanFileReader

  public OplanFileReader(String xmlfilename, RootFactory ldmF,
                         ClusterServesPlugIn theCluster)
  {
    this.xmlfilename = xmlfilename;
    this.ldmF = ldmF;
    this.theCluster = theCluster;
    this.uids = theCluster.getUIDServer();
    this.cid = theCluster.getClusterIdentifier();
    //getParams();
       
  }//OplanFileReader
	
  public Oplan readOplan() {
    theForcePackages_.clear();
    theOrgActivities_.clear();
    theOrgRelations_.clear();
    thePolicies_.clear();

    thePlan_= new Oplan(xmlfilename);
    uids.registerUniqueObject(thePlan_);
    thePlan_.setOwner(cid);
    oplanID = thePlan_.getOplanId();
    oplanUID = thePlan_.getUID();
    readOplanData();
    return thePlan_;
  }

  public ArrayList getForcePackages(Oplan oplan) {
    if ((thePlan_ != null) &&
        (oplan.getUID() == thePlan_.getUID())) {
      return theForcePackages_;
    } else {
      throw new IllegalArgumentException("Oplan - " + oplan + 
                                         " does not match the current oplan - " +
                                         thePlan_);
    }
  }

  public ArrayList getOrgActivities(Oplan oplan) {
    if ((thePlan_ != null) &&
        (oplan.getUID() == thePlan_.getUID())) {
      return theOrgActivities_;
    } else {
      throw new IllegalArgumentException("Oplan - " + oplan + 
                                         " does not match the current oplan - " +
                                         thePlan_);
    }
  }

  public ArrayList getOrgRelations(Oplan oplan) {
    if ((thePlan_ != null) &&
        (oplan.getUID() == thePlan_.getUID())) {
      return theOrgRelations_;
    } else {
      throw new IllegalArgumentException("Oplan - " + oplan + 
                                         " does not match the current oplan - " +
                                         thePlan_);
    }
  }

  public ArrayList getPolicies(Oplan oplan) {
    if ((thePlan_ != null) &&
        (oplan.getUID() == thePlan_.getUID())) {
      return thePolicies_;
    } else {
      throw new IllegalArgumentException("Oplan - " + oplan + 
                                         " does not match the current oplan - " +
                                         thePlan_);
    }
  }
        

  /**
     get the XMLFileName
  */
  private void getParams() 
  {
    /* Need to add back! 
    Vector pv = getParameters();
    if ( pv == null ) {
      throw new RuntimeException( "Oplan requires a parameter" );
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
		  
    xmlfilename= "oplan.xml";
    */
    
    //xmlfilename = thePlan_.getXMLFileName();
  }
 
  public void readOplanData()
  {
    try {
      doc = theCluster.getConfigFinder().parseXMLConfigFile( xmlfilename );
      parseFileData(doc);
    } catch ( Exception e ) {
      e.printStackTrace();
    }// trycatch block
  }// readOplanData
   
  protected void parseFileData(Node node) 
  {
    if( node != null)
      {
        int type = node.getNodeType();

        if ( type == Node.DOCUMENT_NODE )
          node = ((Document)node).getDocumentElement();				 
        else
          System.err.println("\nErrror: not DOCUMENT NODE\n");

        setOplanAttrs( node );

        // Get the remaining child notes.
        NodeList theNodes = ((Element)node).getChildNodes();
        int orgNum = 0;
        int theaterNum = 0;
		  
        if (theNodes != null)
          {
            // Parse each of the subNodes.
            for ( int i = 0; i < theNodes.getLength(); i++ )
              {			    
                // From DTD
                // pod,dfsp,cinc+,timespan?,organization+,forcepackage*,OplanPolicy*
			    
                String nodeName = theNodes.item(i).getNodeName();				
			
                if (nodeName.equals(TIMESPAN)) 
                  setTimeSpan( thePlan_, theNodes.item(i) );	

                if( nodeName.equals(ORGANIZATION) )
                  {
                    addOrganization( theNodes.item(i) ); 			
                    orgNum++;
                  }
                if( nodeName.equals(FORCEPACKAGE) ) 
                  addForcePackage( theNodes.item(i) ); 
				   
                if (nodeName.equals(OPLANPOLICY) ) 
                  setPolicy( theNodes.item(i) );   

                if (nodeName.equals(POD) )
                  addPOD( theNodes.item(i) );

                if (nodeName.equals(DFSP) )
                  addDFSP( theNodes.item(i) );
								
              }// for loop
          }// if theNodes != null
		  
      } // node != null
    else
      System.err.println("OplanFileReader:parseFileData Unable to parse " + xmlfilename + " please check the file.");
  }// parseFileData

  private static SimpleDateFormat datefmt=new SimpleDateFormat("MM/dd/yyyy");
  private void setOplanAttrs(Node data)
  {
    NamedNodeMap attributes = data.getAttributes();
    Date cDaydate = new Date();
    Date endDaydate = null;
	   
    // Oplan Attributes	   
    /* cDay
       endDay
       priority
       operationName
       oplanID  */
	   
    if( attributes.getNamedItem(OPLANID) != null)
      {
        oplanID = attributes.getNamedItem(OPLANID).getNodeValue();
        thePlan_.setOplanId(oplanID);
      }  
    if( attributes.getNamedItem(OPERATIONNAME) != null)
      thePlan_.setOperationName(attributes.getNamedItem(OPERATIONNAME).getNodeValue());
    if( attributes.getNamedItem(PRIORITY) != null)
      thePlan_.setPriority(attributes.getNamedItem(PRIORITY).getNodeValue());
    if( attributes.getNamedItem(CDAY) != null) {
      try	{
        SimpleDateFormat formatter = datefmt;
        cDaydate =formatter.parse(attributes.getNamedItem(CDAY).getNodeValue());
      }
      catch (ParseException e) { 
        System.out.println(e.getMessage());
      }// try catch
      thePlan_.setCday(cDaydate);
    }// if CDAY
    if( attributes.getNamedItem(ENDDAY) != null) {
      try	{
        SimpleDateFormat formatter = datefmt;
        endDaydate =formatter.parse(attributes.getNamedItem(ENDDAY).getNodeValue());
      }
      catch (ParseException e) { 
        System.out.println(e.getMessage());
      }// try catch
      thePlan_.setEndDay(endDaydate);
    }// if ENDDAY
    if( attributes.getNamedItem(THEATERID) != null)
      thePlan_.setTheaterID(attributes.getNamedItem(THEATERID).getNodeValue());
    if( attributes.getNamedItem(TERRAINTYPE) != null)
      thePlan_.setTerrainType(attributes.getNamedItem(TERRAINTYPE).getNodeValue());
    if( attributes.getNamedItem(SEASON) != null)
      thePlan_.setSeason(attributes.getNamedItem(SEASON).getNodeValue());
    if( attributes.getNamedItem(ENEMYFORCETYPE) != null)
      thePlan_.setEnemyForceType(attributes.getNamedItem(ENEMYFORCETYPE).getNodeValue());
    if( attributes.getNamedItem(HOSTNATIONPOL) != null)
      thePlan_.setHNSPOL(new Boolean(attributes.getNamedItem(HOSTNATIONPOL).getNodeValue()).booleanValue() );
    if( attributes.getNamedItem(HOSTNATIONPOLCAPABILITY) != null)
      thePlan_.setHNSPOLCapacity(attributes.getNamedItem(HOSTNATIONPOLCAPABILITY).getNodeValue());
    if( attributes.getNamedItem(HOSTNATIONWATERSUPPORT ) != null)
      thePlan_.setHNSForWater(new Boolean(attributes.getNamedItem(HOSTNATIONWATERSUPPORT ).getNodeValue()).booleanValue() );
    if( attributes.getNamedItem(HOSTNATIONWATERCAPABILITY) != null)
      thePlan_.setHNSWaterCapability(attributes.getNamedItem(HOSTNATIONWATERCAPABILITY).getNodeValue());
  }// setOplanAttrs

	
  public void addPOD(Node data) 
  {  
    POD pod = new POD();
    NamedNodeMap attributes = data.getAttributes();
    if (attributes.getNamedItem(PODTYPE) != null)				
      pod.setType(attributes.getNamedItem(PODTYPE).getNodeValue());				
    if (attributes.getNamedItem(PODVALUE) != null)				
      pod.setValue(attributes.getNamedItem(PODVALUE).getNodeValue());	   
				   
    thePlan_.addPOD(pod);     	  	 
  }// addPOD

  public void addOrganization(Node data)
  {  
    //	  Organization org = new Organization(oplanID);
    int orgActivityNum = 0;
    int orgDeployNum = 0;	
    NamedNodeMap attributes = data.getAttributes();
	  
    // DTD requirements
    // OrgId
	  
    if( attributes.getNamedItem(ORGID) != null)
      {
        orgID = attributes.getNamedItem(ORGID).getNodeValue();
        //	     org.setOrgId(orgID);  
      }   
    // get the subNodes
    NodeList theNodes = ((Element)data).getChildNodes();
    if (theNodes != null)
      {
        int len = theNodes.getLength();
        for ( int i = 0; i < len; i++ )
          {
            // <!ELEMENT organization (orgactivity+,orgrelation*)>
            String nodeName = theNodes.item(i).getNodeName();

            if( nodeName.equals(ORGACTIVITY) ) 				  
              {
                setOrgActivity(theNodes.item(i), theNodes.item(i).getAttributes());
                orgActivityNum++;
              }// if orgactivity
            if( nodeName.equals(ORGRELATION))
              {
                addOrgRelation(null, theNodes.item(i)); // more than one
              }   
				     
				
          }// for loop
      }// if statement
	  
    if (orgDeployNum > 1)
      System.err.println("OplanFileReader:addOrganization Only one deployment requirement can be specified per organization.");
	     
    if (orgActivityNum < 1)
      System.out.println("OplanFileReader:addOrganization At least one org activity must be specified per organization.");

  }// addOrganization
	

  public void addForcePackage(Node data)
  {
    ForcePackage fp = new ForcePackage(oplanUID);
    uids.registerUniqueObject(fp);
    fp.setOwner(cid);
    NamedNodeMap attributes = data.getAttributes();
    // DTD Requirements
    // forcepackage.ForcePackageId
	   
    if( attributes.getNamedItem(FORCEPACKAGEID) != null)
      fp.setForcePackageId(attributes.getNamedItem(FORCEPACKAGEID).getNodeValue());
		 
    NodeList theNodes = ((Element)data).getChildNodes();
	   
    // More DTD Requirements
    /* <!ELEMENT forcepackage (timespan?,orgrelation+)> */
    if (theNodes != null)
      {
        int len = theNodes.getLength();
        for ( int i = 0; i < len; i++ )
          {			    
            String nodeName = theNodes.item(i).getNodeName();
            if( nodeName.equals(TIMESPAN) ) 
              {
	        //setTimeSpan(fp, theNodes.item(i).getAttributes());			
                setTimeSpan(fp, theNodes.item(i));
              }   
            if( nodeName.equals(ORGRELATION) ) 
              addOrgRelation(fp, theNodes.item(i)); // more than one
          }// for loop
      }// if statement

    
    theForcePackages_.add(fp);
	  	   
  }// setForcePackages	

  public void setOrgActivity(Node data, NamedNodeMap attributes) 
  {
    OrgActivity oa = new OrgActivity(orgID, oplanUID);
    uids.registerUniqueObject(oa);
    oa.setOwner(cid);
		     
    if( attributes.getNamedItem(ACTIVITYTYPE) != null)
      oa.setActivityType(attributes.getNamedItem(ACTIVITYTYPE).getNodeValue());
    if( attributes.getNamedItem(ACTIVITYNAME) != null)
      oa.setActivityName(attributes.getNamedItem(ACTIVITYNAME).getNodeValue());
    if( attributes.getNamedItem(OPTEMPO) != null)
      oa.setOpTempo(attributes.getNamedItem(OPTEMPO).getNodeValue());

    addGeoLocs(oa, data);

    // DTD Requirements (keyvalue*,timespan?)>	 
    NodeList subNodes = ((Element)data).getChildNodes();
    if (subNodes != null)
      {
        int len = subNodes.getLength();
        for ( int i = 0; i < len; i++ )
          {
            String nodeName = subNodes.item(i).getNodeName();
				// Create a has table for OrgActivity stuff
            if( nodeName.equals(KEYVALUE) ) 
              {
                NamedNodeMap subattributes = subNodes.item(i).getAttributes();
                if ( ( subattributes.getNamedItem(KEY) != null) &&
                     ( subattributes.getNamedItem(VALUE) != null))
                  oa.addActivityItem(subattributes.getNamedItem(KEY).getNodeValue(),
                                     subattributes.getNamedItem(VALUE).getNodeValue());  
              }// if statement
	    // if Timespan
            if (nodeName.equals(TIMESPAN))
              setTimeSpan(oa, subNodes.item(i));
          }// for loop			
      }// if statement		 		 
		 
    theOrgActivities_.add(oa);
  }//setOrgActivity
	
  public void addOrgRelation(Object obj, Node node)
  {
    OrgRelation or;
	  
    if (obj instanceof ForcePackage) {
      or = new OrgRelation("", oplanUID);
      or.setForcePackageId( ((ForcePackage)obj).getForcePackageId() );
    } else {
      or = new OrgRelation(orgID, oplanUID);
      or.setForcePackageId("");
    }
    uids.registerUniqueObject(or);
    or.setOwner(cid);

    NamedNodeMap attributes = node.getAttributes();
	  
    /* <!ELEMENT orgrelation (timespan?)>
       <!--
       orgrelation.OtherOrgId: E
       orgrelation.AssignedRole: N S 
       orgrelation.RelationType: E
       --> */

    if( attributes.getNamedItem(ASSIGNEDROLE) != null)
      or.setAssignedRole(attributes.getNamedItem(ASSIGNEDROLE).getNodeValue());
    if( attributes.getNamedItem(RELATIONTYPE) != null)
      or.setRelationType(attributes.getNamedItem(RELATIONTYPE).getNodeValue());
    if( attributes.getNamedItem(OTHERORGID) != null)
      or.setOtherOrgId(attributes.getNamedItem(OTHERORGID).getNodeValue());

    // Set a unique Identifier
    //UID uid = ldmF.getNextUID();
    //or.setOrgRelationId(uid);
    uids.registerUniqueObject(or);
    or.setOwner(cid);

    NodeList subNodes = ((Element)node).getChildNodes();
    if (subNodes != null)
      {		   
        int len = subNodes.getLength();
        for ( int i = 0; i < len; i++ )
          {
            String nodeName = subNodes.item(i).getNodeName();
            if( nodeName.equals(TIMESPAN) ) 
              {
                // setTimeSpan(oa, subNodes.item(i).getAttributes()); 
                setTimeSpan(or, subNodes.item(i));
              }// if statement
          } // for loop
      }// if there are subNodes

    // if (obj instanceof ForcePackage)
    //     ((ForcePackage)obj).addOrgRelation(or);
		                
    theOrgRelations_.add(or);
    
  }//addOrgRelation
	
  public void setTimeSpan(Object object, Node theNode) 
  {
	    
    NamedNodeMap attributes = theNode.getAttributes();

    int startDelta = 0;
    int thruDelta = 0;

    // Parse for the integer value and store it.	 
    if( attributes.getNamedItem(TS_STARTTIME) != null)
      {
	     
        String startTime = attributes.getNamedItem(TS_STARTTIME).getNodeValue();
        int index = startTime.indexOf('+');
        try
          {
            startDelta =  (new Integer(startTime.substring(index + 1).trim())).intValue();
          } catch (NumberFormatException e)
            {
              System.err.println("startTime is not in the correct Format");
            }// catch
      }// if startTime
    // Parse for the integer value and store it.
    if( attributes.getNamedItem(TS_THRUTIME) != null)
      {
        String thruTime = attributes.getNamedItem(TS_THRUTIME).getNodeValue();
        int index = thruTime.indexOf('+');
        try
          {
            thruDelta =  (new Integer(thruTime.substring(index + 1).trim())).intValue();
          } catch (NumberFormatException e)
            {
              System.err.println("thruTime is not in the correct Format");
            }// catch
      }// thruTime     
	  
    TimeSpan ts = new org.cougaar.domain.glm.ldm.oplan.TimeSpan(thePlan_.getCday(), 
                                                                startDelta, thruDelta);

    if (object instanceof OplanContributor)
      ((OplanContributor)object).setTimeSpan(ts);
	     
  }//setTimeSpan
		
  public void addDFSP(Node node)
  {
    DFSP dfsp = new DFSP();
    uids.registerUniqueObject(dfsp);
    dfsp.setOwner(cid);

    NamedNodeMap attributes = node.getAttributes();
    
    if( attributes.getNamedItem(DFSPNAME) != null)
      dfsp.setName(attributes.getNamedItem(DFSPNAME).getNodeValue());
			 
    addGeoLocs(dfsp, node);

    thePlan_.addDFSP(dfsp);
		  
  }//setDFSP
	

  public void addGeoLocs(Object obj, Node node)
  {
    NodeList subNodes = ((Element)node).getChildNodes();

    if (subNodes != null)
      {
        int len = subNodes.getLength();
        for (int i = 0; i < len; i++)
          {
            String  nodeName = subNodes.item(i).getNodeName();
            if (nodeName.equals(GEOLOC) )
              {
			    
                setGeoLoc(obj, subNodes.item(i));
              }//if statement
          }//for loop
      }//if
  }//addGeoLocs

  private HashMap geolocCache = new HashMap(89);

  public void setGeoLoc(Object obj, Node node)
  {
    Latitude lat = null;
    Longitude lon = null;
    String name = null;
    NamedNodeMap subattributes = node.getAttributes();
        
    try {

      // Create a newGeolocLocation
      GeolocLocationImpl geoLoc = null;

      // get the geoLoc Code from the XML
      Node gcn = subattributes.getNamedItem(GEOLOCCODE);
      String geoLocCode = null;
      if (gcn != null && (geoLocCode=gcn.getNodeValue()) != null) {
        geoLocCode = geoLocCode.intern();
        geoLoc = (GeolocLocationImpl) geolocCache.get(geoLocCode);
      }

      if (geoLoc == null) {
        if (geoLocCode == null) geoLocCode = new String("");

        geoLoc = new GeolocLocationImpl();
        // Set the value in NewGeoLocation accordingly.
        geoLoc.setGeolocCode(geoLocCode);

        // Get the GeoLoc Name from the XML
        String geoLocName;
        if (subattributes.getNamedItem(GEOLOCNAME) == null)
          geoLocName = "";  
        else
          geoLocName = subattributes.getNamedItem(GEOLOCNAME).getNodeValue();
	     
        // Set the value in NewGeoLocation accordingly.
        geoLoc.setName(geoLocName);

        // get the Latitude and Longitude values.
        if (( subattributes.getNamedItem(GEOLOCLAT)!= null) &&
            ( subattributes.getNamedItem(GEOLOCLONG) != null))	
          {
            lat = Latitude.newLatitude(subattributes.getNamedItem(GEOLOCLAT).getNodeValue());
            lon = Longitude.newLongitude(subattributes.getNamedItem(GEOLOCLONG).getNodeValue());
              
            geoLoc.setLatitude(lat);
            geoLoc.setLongitude(lon);
          }// if the latitude long
        // cache the instance
        geolocCache.put(geoLocCode, geoLoc);
      }

      if ( geoLoc != null) {   
        if (obj instanceof DFSP) ((DFSP)obj).setGeoLoc(geoLoc);	   	 
        if (obj instanceof OrgActivity) ((OrgActivity)obj).setGeoLoc(geoLoc);	    
      } else {
        System.err.println("OplanFileReader:setGeoLoc - geoloc not specifed correctly in xml file.");
      }
    } catch (Exception e) {
      System.err.println("OplanFileReader Got exception: " + e);
      e.printStackTrace();
      //System.exit(-1);
    }
  }
	
  public void setPolicy(Node data)
  {
    XMLPolicyCreator pc = new XMLPolicyCreator(xmlfilename, 
                                               theCluster.getConfigFinder(),
                                               ldmF);
    // New functionality to create an OplanPolicy Object
    // 9-13-99

    NodeList subNodes = ((Element)data).getChildNodes();

    if (subNodes != null) 
      {
        int len = subNodes.getLength();

        for ( int i = 0; i < len; i++ ) 
          {
            String nodeName = subNodes.item(i).getNodeName();
            if( nodeName.equals(POLICY) ) {
              Policy thePolicy = (Policy)pc.getPolicy(subNodes.item(i));
              if (thePolicy != null) 
                {
                  //String policyName = subNodes.item(i).getAttributes().getNamedItem("name").getNodeValue();
                  thePolicies_.add(thePolicy);
                }
			          					 
            }// if statement
          }// for loop
      }// if statement 

  }//setPolicy

}// OPlanFileReader
