/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.psp.plan;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

import org.cougaar.core.cluster.CollectionSubscription;
import org.cougaar.core.cluster.Subscription;
import org.cougaar.domain.planning.ldm.measure.Latitude;
import org.cougaar.domain.planning.ldm.measure.Longitude;
import org.cougaar.domain.planning.ldm.plan.*;
import org.cougaar.lib.planserver.*;
import org.cougaar.core.util.AbstractPrinter;
import org.cougaar.util.EmptyEnumeration;
import org.cougaar.util.UnaryPredicate;

import org.cougaar.domain.glm.ldm.asset.Organization;

/**
  * "Grab-bag" PSP to answer various "asset related" questions
  *
  * ?ORG_ASSET_RELATIONS : returns ALL Org Asset relations across society.
  *                        Returns Vector of UIOrgRelationships
  *
  * ?ASSETS_AT_THIS_CLUSTER : returns Vector of UIDs
  *
  * This PSP doesn't work if implements UseDirectSocketOutputStream
  **/

public class PSP_UIAssets extends PSP_BaseAdapter
                          implements PlanServiceProvider, UISubscriber {
      
    /** A zero-argument constructor is required for dynamically loaded PSPs,
        required by Class.newInstance()
    **/
    public PSP_UIAssets() {
                super();
    }

    /*************************************************************************************
     * 
     **/
    public PSP_UIAssets( String pkg, String id ) throws RuntimePSPException {
                setResourceLocation(pkg, id);
    }

    /*************************************************************************************
     *
     **/
    public boolean test(HttpInput query_parameters, PlanServiceContext sc) {
        super.initializeTest(); // IF subclass off of PSP_BaseAdapter.java
        return false;  // This PSP is only accessed by direct reference.
    }

    /*************************************************************************************
     *
     **/
    private static UnaryPredicate getOrgPred(){
        return new UnaryPredicate() {
              public boolean execute(Object o) {
                 if (o instanceof Organization) {
              //System.out.println("PREDICATE CALLED true!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                          return true; }
                 return false;
          }
      };
    }

  protected AbstractPrinter getAbstractPrinter(
      PrintStream out,
      HttpInput query_parameters,
      String defaultFormat) throws Exception {
    String format = defaultFormat;
    Enumeration params = query_parameters.getURLParameters().elements();
    while (params.hasMoreElements()) {
      String p = ((String)params.nextElement()).toLowerCase();
      if (p.startsWith("format=")) {
        format = p.substring("format=".length()).trim();
        if ((format.length() <= 0) ||
            !AbstractPrinter.isValidFormat(format)) {
          throw new RuntimePSPException("Invalid format!: "+format);
        }
      }
    }
    return AbstractPrinter.createPrinter(format, out);
  }



    private final static int ORG_ASSET_RELATIONS = 1;
    private final static int ASSETS_AT_THIS_CLUSTER =2;

    private int mode; // ORG_ASSET_RELATIONS, ASSETS_AT_THIS_CLUSTER
    private boolean hostFlag = false;

    /*************************************************************************************
     *
     **/
    public void execute( PrintStream out, HttpInput query_parameters,
                         PlanServiceContext psc,
                                           PlanServiceUtilities psu ) throws Exception    {
       execute(getAbstractPrinter(out, query_parameters, "html"),
               query_parameters, psc, psu); 
    }



    private void execute( AbstractPrinter pr, HttpInput query_parameters,
                         PlanServiceContext psc,
                                           PlanServiceUtilities psu ) throws Exception    {

       System.out.println("[PSP_UIAssets.execute] Using printer: " + pr.getClass().getName() );

      // pr.beforeFirstPrint();

       if (  true == query_parameters.existsParameter("ORG_ASSET_RELATIONS") ) {
                mode = ORG_ASSET_RELATIONS;
       }
       hostFlag = query_parameters.existsParameter("IS_HOST");
       

       if( hostFlag && (mode == ORG_ASSET_RELATIONS) ) {
            getAllOrgAssetRelationships(pr, query_parameters, psc, psu );
       }
       else if( mode == ORG_ASSET_RELATIONS ) {
            getOrgAssetRelationships(pr, query_parameters, psc, psu );
       }

      // pr.afterLastPrint();
    }

    public void getAllOrgAssetRelationships( AbstractPrinter pr,
                                             HttpInput query_parameters,
                                             PlanServiceContext psc,
                                             PlanServiceUtilities psu ) throws Exception
    {
        Vector results = new Vector();
        Vector urls = new Vector();
        Vector names = new Vector();
        psc.getAllURLsAndNames(urls, names);
        int sz = urls.size();
        int i;
        for(i=0;i<sz;i++)
        {
            String u = (String)urls.elementAt(i);
            String n = (String)names.elementAt(i);

           System.out.println("[PSP_UIAssets.getAllOrgAssetRelationships] Attempting to OPEN PSP_ClusterRelationship Child URL("+ u +")");
            try{
                      URL myURL = new URL(u+this.getPath()+"?FORMAT=DATA?ORG_ASSET_RELATIONS");

                      System.out.println("[PSP_UIAssets.getAllOrgAssetRelationships] child URL: " + myURL.toString());
                      URLConnection myConnection = myURL.openConnection();
                      myConnection.getDoInput();
                      //System.out.println("opened query connection = "+ myConnection.toString());

                      InputStream in = myConnection.getInputStream();

                      ObjectInputStream objIn = new ObjectInputStream(in);
                      Object obj = objIn.readObject();
                      if (obj instanceof Vector) {
                          Vector v = (Vector)obj;
                          Enumeration en = v.elements();
                          while(en.hasMoreElements() ) {
                                results.addElement(en.nextElement());
                          }
                      }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } // for()
       pr.printObject(results);
    }


    /*************************************************************************************
     *
     **/
    public void getOrgAssetRelationships(AbstractPrinter pr,
                                      HttpInput query_parameters,
                                      PlanServiceContext psc,
                                      PlanServiceUtilities psu ) throws Exception
    {
       Subscription OrgSubscription = psc.getServerPlugInSupport().subscribe(this, getOrgPred());
       Enumeration en = ((CollectionSubscription)OrgSubscription).elements();

       System.out.println("[PSP_UIAssets.getOrgAssetRelationships] Invoked...");
       //ObjectOutputStream oos = new ObjectOutputStream(out);

       Vector relationships  = new Vector();
       Organization self = null;
       // FIRST PASS - OBTAIN SELF ASSET
       while( en.hasMoreElements() )
       {
           Organization org = (Organization)en.nextElement();
           if (org.isSelf()) {
             self = org;
           }
       }

       en = ((CollectionSubscription)OrgSubscription).elements();

       String THISCID = getClusterIDAsString(psc.getServerPlugInSupport().getClusterIDAsString());

       // SECOND PASS - COMPUTE RELATIONSHIPS
       if (self != null) {
         RelationshipSchedule schedule = self.getRelationshipSchedule();

         for (Iterator iterator = new ArrayList(schedule).iterator();
              iterator.hasNext();) {
           Relationship relationship = (Relationship)iterator.next();
           Organization other = 
             (Organization) schedule.getOther(relationship);

           UIOrgRelationship relation = new UIOrgRelationship();
         
           relation.setTargetOrganizationCID(self.getClusterPG().getClusterIdentifier().toString());
           relation.setProviderOrganizationCID(other.getClusterPG().getClusterIdentifier().toString());
         
           relation.setTargetOrganizationUID(self.getUID().getUID());
           relation.setProviderOrganizationUID(other.getUID().getUID());

           relation.setRelationship(schedule.getOtherRole(relationship).toString());
           relationships.addElement(relation);
         }
       }

       //oos.writeObject(relationships);
       //oos.flush();
       //oos.close();

       System.out.println("[PSP_UIAssets.getOrgAssetRelationships] returning relationships");
       pr.printObject(relationships);
    }

   /** Get rid of "<..>" prefix/suffix **/
    public String getClusterIDAsString( String rawCID)
    {
       String id  = rawCID;

       if( id.startsWith("<") ) id = id.substring(1);
       if( id.endsWith(">") ) id = id.substring(0,id.length()-1);

       return id;
    }

    /**
     * A PSP can output either HTML or XML (for now).  The server
     * should be able to ask and find out what type it is.
     **/
    public boolean returnsXML() {
        return true;
    }

    /**
     * A PSP can output either HTML or XML (for now).  The server
     * should be able to ask and find out what type it is.
     **/
    public boolean returnsHTML() {
        return false;
    }

    /**  Any PlanServiceProvider must be able to provide DTD of its
     *  output IFF it is an XML PSP... ie.  returnsXML() == true;
     *  or return null
     **/
    public String getDTD()  {
        return "";
    }


    /*************************************************************************************
     * 
     **/
    public void subscriptionChanged(Subscription subscription) {
    }

    private final static long ONE_DAY = 86400000;

}

