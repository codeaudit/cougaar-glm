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

package org.cougaar.mlm.plugin.generic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.cougaar.core.blackboard.AnonymousChangeReport;
import org.cougaar.core.blackboard.ChangeReport;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.planning.ldm.plan.RelationshipSchedule;
import org.cougaar.planning.ldm.plan.Transferable;
import org.cougaar.planning.ldm.plan.TransferableTransfer;
import org.cougaar.planning.plugin.legacy.SimplePlugin;
import org.cougaar.util.ConfigFinder;
import org.cougaar.util.Filters;
import org.cougaar.util.UnaryPredicate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * PropagationPlugin propagates Transferables based on xml files in parameter list
 *
 * @author  ALPINE <alpine-software@bbn.com>
 *
 */

public class PropagationPlugin extends SimplePlugin
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
      throw new RuntimeException( "PropagationPlugin requires a parameter" );
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
            if ((changeReports != AnonymousChangeReport.SET) &&
                (changeReports != null)) {
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

      try {
        doc = ConfigFinder.getInstance().parseXMLConfigFile(xmlfilename);
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
                                           getAgentIdentifier());

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



