/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.psp.transportation;

import org.cougaar.core.cluster.Subscription;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.plan.Relationship;
import org.cougaar.domain.planning.ldm.plan.RelationshipSchedule;
import org.cougaar.lib.planserver.PSPState;
import org.cougaar.lib.planserver.PSP_BaseAdapter;
import org.cougaar.lib.planserver.PlanServiceContext;
import org.cougaar.lib.planserver.PlanServiceProvider;
import org.cougaar.lib.planserver.PlanServiceUtilities;
import org.cougaar.lib.planserver.UISubscriber;
import org.cougaar.lib.planserver.RuntimePSPException;
import org.cougaar.lib.planserver.HttpInput;
import org.cougaar.util.UnaryPredicate;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.lang.Integer;

import org.cougaar.domain.glm.ldm.Constants;
import org.cougaar.domain.glm.ldm.asset.Organization;
import org.cougaar.domain.glm.ldm.asset.GLMAsset;
import org.cougaar.domain.glm.ldm.plan.GeolocLocation;
import org.cougaar.domain.glm.util.AssetUtil;

public class PSP_AssetPopulation extends PSP_BaseAdapter
                              implements PlanServiceProvider, UISubscriber
{
  public static final boolean DEBUG = false;

  /** 
   * A zero-argument constructor is required for dynamically loaded PSPs,
   * required by Class.newInstance()
   **/
  public PSP_AssetPopulation() {
    super();
  }

  public PSP_AssetPopulation(String pkg, String id) throws RuntimePSPException 
  {
    setResourceLocation(pkg, id);
  }

  public boolean test(HttpInput query_parameters, PlanServiceContext sc)
  {
    super.initializeTest(); // IF subclass off of PSP_BaseAdapter.java
    return false;  // This PSP is only accessed by direct reference.
  }


  private static UnaryPredicate getSelfPred(){
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof Organization) {
          return ((Organization)o).isSelf();
        }
        return false;
      }
    };
  }

  private static UnaryPredicate interestingAsset() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof GLMAsset) {
	  GLMAsset a = (GLMAsset) o;
          return (a.hasPersonPG() || a.hasPhysicalPG());
        }
        return false;
      }
    };
  }

  /**
   * @param out the output stream that the XML text will go to
   * @param query_parameters - parameters as above
   * @param psc - context of the psp
   * @param psu - unused psp utilities
   **/
  public void execute(PrintStream out,
                      HttpInput query_parameters,
                      PlanServiceContext psc,
                      PlanServiceUtilities psu) throws Exception
  {
    // This should return a collection containing only the self organization
    Collection orgs = 
      psc.getServerPlugInSupport().queryForSubscriber(getSelfPred());
    Organization organization = null;
    Iterator iter = orgs.iterator();
    while (iter.hasNext()) {
	organization = (Organization)iter.next();
    }

    Collection assets = 
      psc.getServerPlugInSupport().queryForSubscriber(interestingAsset());

    writeAssets(organization, assets, out);
  }

  private void writeAssets(Organization organization, Collection assets, PrintStream out)
  {
    out.println ("<?xml version='1.0'?>");

    GeolocLocation geoloc = AssetUtil.getOrgLocation(organization);
    out.println("<Location geoloc=\"" + geoloc.getGeolocCode() + "\" " +
				"lat=\"" + geoloc.getLatitude ().getDegrees () + "\" " +
				"lon=\"" + geoloc.getLongitude ().getDegrees () +
				"\">");

	Map typeToInstances = new HashMap ();
    Map typeToQuantity = mapAssets(assets, typeToInstances);
    Map typeToName = mapNames(assets);
    Iterator i = typeToQuantity.keySet().iterator();
    while (i.hasNext()) {
	  String typeID = (String)i.next();
	  String name = (String)typeToName.get(typeID);
	  int quantity = ((Integer)typeToQuantity.get(typeID)).intValue();
	  out.println("\t<Asset nomenclature=\"" + name + "\"");
	  out.println("\t\ttypeID=\"" + typeID + "\"");
	  out.println("\t\tquantity=\"" + quantity + "\">");
	  
	  List instances = (List) typeToInstances.get (typeID);
	  
	  for (int j = 0; j < instances.size (); j++) {
		String uid = (String) instances.get(j);
		out.println("\t\t<instance uid=\"" + uid + "\"/>");
	  }

	  out.println("\t</Asset>");
    }
    out.println("</Location>");
  }
  
  private Map mapAssets(Collection assets, Map typeToInstances) {
    Map typeToQuantity = new HashMap();

    Iterator iter = assets.iterator();
    while (iter.hasNext()) {
	  GLMAsset asset = (GLMAsset) iter.next();
	  String typeID = asset.getTypeIdentificationPG().getTypeIdentification();
	  if (typeToQuantity.get(typeID) == null)
	    typeToQuantity.put(typeID, new Integer(1));
	  else
	    typeToQuantity.put(typeID, 
						   new Integer(((Integer)typeToQuantity.get(typeID)).intValue() + 1));
	  List instances;
	  
	  if ((instances = (List) (typeToInstances.get (typeID))) == null)
		typeToInstances.put (typeID, instances = new ArrayList());
	
	  instances.add (asset.getUID().getUID().intern());
    }

    return typeToQuantity;
  }

  private Map mapNames(Collection assets) {
    Map typeToName = new HashMap();

    Iterator iter = assets.iterator();
    while (iter.hasNext()) {
	GLMAsset asset = (GLMAsset) iter.next();
	String typeID = asset.getTypeIdentificationPG().getTypeIdentification();
	String name = asset.getTypeIdentificationPG().getNomenclature();
	if (typeToName.get(typeID) == null)
	    typeToName.put(typeID, name);
    }

    return typeToName;
  }

  /** required */
  public void subscriptionChanged(Subscription subscription) {}

  /**
   * A PSP can output either HTML or XML (for now).  The server
   * should be able to ask and find out what type it is.
   **/
  public boolean returnsXML() {
    return true;
  }

  public boolean returnsHTML() {
    return false;
  }

  /**  
   *  Any PlanServiceProvider must be able to provide DTD of its
   *  output IFF it is an XML PSP... ie.  returnsXML() == true;
   *  or return null
   **/
  public String getDTD()  {
    return null;
  }

}




