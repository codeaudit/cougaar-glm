/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.plugin.generic;

import java.util.*;

import org.cougaar.core.cluster.ChangeReport;
import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.domain.planning.ldm.plan.Transferable;
import org.cougaar.domain.glm.ldm.oplan.Oplan;
import org.cougaar.domain.glm.ldm.oplan.OplanContributor;
import org.cougaar.domain.planning.ldm.plan.TransferableTransfer;
import org.cougaar.domain.planning.ldm.plan.RelationshipSchedule;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.glm.ldm.asset.Organization;
import org.cougaar.core.plugin.SimplePlugIn;
import org.cougaar.util.UnaryPredicate;

import java.io.*;

import org.cougaar.util.Filters;

import com.ibm.xml.parser.*;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * PropagationPlugIn propagates Transferables based on xml files in parameter list
 *
 * @author  ALPINE <alpine-software@bbn.com>
 * @version $Id: PropagationPlugIn.java,v 1.7 2001-03-28 15:45:37 ngivler Exp $
 */

public class PropagationPlugIn extends SimplePlugIn
{


  private Vector roles = new Vector();
  
  private IncrementalSubscription organizations;

  private ArrayList subscriptionCollection;

  private static UnaryPredicate allOrganizationPred = new UnaryPredicate() {
    public boolean execute(Object o) { 
      if (o instanceof Organization) {
        return true;
      } else {
        return false;
      }
    }
  };

  protected void setupSubscriptions() {
    
    organizations = (IncrementalSubscription)subscribe(allOrganizationPred);

    subscriptionCollection = new ArrayList(13);
    Vector pv = getParameters();
    if ( (pv == null) || (pv.size() ==0) ) {
      throw new RuntimeException( "PropagationPlugIn requires a parameter" );
    } else {
      for (Iterator pi = pv.iterator(); pi.hasNext();) {
	String xmlfilename = (String) pi.next();
	parseFile(xmlfilename);
      }
    }
  }


  protected void transfer(Transferable t, Organization org) {
    if ((t == null) || (org == null))
      return;

    TransferableTransfer tt;
    try {
      tt = theLDMF.createTransferableTransfer(t, org);
    }catch (Exception e) {
      return;
    }
    if (tt == null){
    }
    else {
      publishAdd(tt);
    }
  }

  public synchronized void execute() {

    for ( Iterator subIt = subscriptionCollection.iterator(); subIt.hasNext(); ) {
      TransferableSubscriptions currentSubscriptions = (TransferableSubscriptions)subIt.next();
      
      UnaryPredicate 
        destPred = currentSubscriptions.getDestinationPredicate();
      ArrayList alreadySent = new ArrayList();

      if (organizations.hasChanged()) {
        Collection adds = organizations.getAddedCollection();
        if (adds != null) {
          for (Iterator iterator = Filters.filter(adds, destPred).iterator(); 
               iterator.hasNext();) {
            Organization org = (Organization)iterator.next();
            
            // Transfer all existing transferables to a new destination
            for (Iterator transferables = 
                   currentSubscriptions.getTransferableSubscription().getCollection().iterator(); 
                 transferables.hasNext();) {
              transfer((Transferable)transferables.next(), org);
            }
            alreadySent.add(org);
          }
        }

        // BOZO - Missing logic to rescind transferables from destinations
        // which are no longer valid. Also sending all transferables each
        // time a valid destination changes.
        Collection changes = organizations.getChangedCollection();
        if (changes != null) {
          for (Iterator iterator = 
                 Filters.filter(changes, destPred).iterator(); 
               iterator.hasNext();) {
            Organization org = (Organization)iterator.next();
            boolean isRelationshipChange = false;
            Set changeReports = organizations.getChangeReports(org);
            if (changeReports != null) {
              for (Iterator i = changeReports.iterator(); i.hasNext(); ) {
                ChangeReport changeReport = (ChangeReport) i.next();
                if (changeReport instanceof RelationshipSchedule.RelationshipScheduleChangeReport) {
                  isRelationshipChange = true;
                  break;
                }
              }
            }
            if (isRelationshipChange) {
              for (Iterator transferables = 
                     currentSubscriptions.getTransferableSubscription().getCollection().iterator(); 
                   transferables.hasNext();) {
                transfer((Transferable)transferables.next(), org);
              }
              alreadySent.add(org);
            }
          }
        }

        // BOZO - No logic to rescind transferable from organizations which 
        // have been removed

      }


      IncrementalSubscription ts = 
        currentSubscriptions.getTransferableSubscription();

      if (ts.hasChanged()) {
        Collection destinations = 
          Filters.filter(organizations.getCollection(), destPred);

        Collection adds = ts.getAddedCollection();
        if (adds != null) {
          for (Iterator transferables = ts.getAddedCollection().iterator(); 
               transferables.hasNext();) {
            Transferable t = (Transferable)transferables.next();
            
            Iterator iterator = destinations.iterator();
            while (iterator.hasNext()) {
              Organization org = (Organization)iterator.next();
              // we already sent these
              if (!alreadySent.contains(org))
                transfer(t,org);
            }
          }
        }
        
        // send modified transferable to existing destinations;
        Collection changes = ts.getChangedCollection();
        if (changes != null) {
          for (Iterator transferables = ts.getChangedCollection().iterator(); 
               transferables.hasNext();) {
            Transferable t = (Transferable)transferables.next();
            
            Iterator iterator = destinations.iterator();
            while (iterator.hasNext()) {
              Organization org = (Organization)iterator.next();
              transfer(t,org);
            }
          }
        }
      }
    }
  }

  private void parseFile(String xmlfilename) {
      Document doc;
      InputStream is;
      Parser parser;
      try {
        doc = getCluster().getConfigFinder().parseXMLConfigFile(xmlfilename);
	if (doc == null) {
	  System.err.println("XML Parser could not handle file " + xmlfilename);
	  return ;
	}
      } catch (Exception e) {
	e.printStackTrace();
	return;
      }

      Element root = doc.getDocumentElement();
      Hashtable transferableSubscriptions = new Hashtable(13);

      if( root.getNodeName().equals( "Propagations" )){
	NodeList nlist = root.getChildNodes();
	int nlength = nlist.getLength();
	for (int i=0; i<nlength; i++) {
	  Node propNode = nlist.item(i);
	  if (propNode.getNodeType() == Node.ELEMENT_NODE) {
	    if (propNode.getNodeName().equals("TransferablePropagation")) {
	      String className  = 
		propNode.getAttributes().getNamedItem("class").getNodeValue();

              TransferablePredicate tp = null;
              try {
                Class cl = theLDM.getLDMClassLoader().loadClass(className);
                /*
                if (OplanContributor.class.isAssignableFrom(cl)) {
                  System.err.println("Warning: "+xmlfilename+
                                     " specifies propagation of OplanContributor \""+
                                     className+"\":  Will ignore.");
                  // leave tp alone to bail out.
                  } else { */
                  tp = new TransferablePredicate(cl);
                  //}
              } catch (Exception e) {
                e.printStackTrace();
              }
              if (tp != null) {
                parseProp(propNode);
                String caps[] = new String[roles.size()];
                caps = (String [])roles.toArray(caps);

                DestinationPredicate dp = 
                  new DestinationPredicate(caps, 
                                           getCluster().getClusterIdentifier());

                IncrementalSubscription ts
                  = (IncrementalSubscription)transferableSubscriptions.get(tp);
                if (ts == null){
                  ts = (IncrementalSubscription)subscribe(tp);
                  transferableSubscriptions.put(tp, ts);
                }

                TransferableSubscriptions fh = new TransferableSubscriptions(ts, dp);
                subscriptionCollection.add(fh);
              }
	    }
	  }
	}
      }
  }


  private void parseProp(Node propNode) {
    roles = new Vector();
    NodeList nlist = propNode.getChildNodes();
    int nlength = nlist.getLength();
    for (int i=0; i<nlength; i++) {
      Node subNode = nlist.item(i);
      if (subNode.getNodeType() == Node.ELEMENT_NODE) {
	if (subNode.getNodeName().equals("role")) {
	  String value = subNode.getFirstChild().getNodeValue();
	  roles.add(value.trim());
	} else
	  System.out.println(subNode.getNodeName());
      }
    }
  }
}



