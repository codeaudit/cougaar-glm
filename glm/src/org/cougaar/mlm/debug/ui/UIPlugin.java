/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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


package org.cougaar.mlm.debug.ui;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.ldm.asset.PhysicalAsset;
import org.cougaar.planning.ldm.ClusterServesPlugin;
import org.cougaar.planning.ldm.asset.AggregateAsset;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.ItemIdentificationPG;
import org.cougaar.planning.ldm.plan.ClusterObjectFactory;
import org.cougaar.planning.ldm.plan.Plan;
import org.cougaar.planning.plugin.legacy.SimplePlugin;
import org.cougaar.util.UnaryPredicate;

  /** This is an example of a User Interface Plugin.
    It allows the user to specify the cluster from which
    to obtain information, and the type of information to display:
    log plan, expandable tasks, allocatable workflows, 
    assets (assigned to the cluster), and asset schedules (schedules
    for assets allocated by the cluster).
    The plan, tasks, and workflows are displayed as trees, and the
    assets and scheduled assets are displayed as bar graphs.
    The information displayed is monitored (if from a local cluster) for
    additions, changes, and deletions, and the displays are updated.
    This currently assumes there is only one plan ("Reality").
    Future: When multiple plans are supported, the plans in each
    cluster should be displayed, and the user should
    select a plan for which to obtain information.

    This extends GenericPluginAdapter which provides the plugin
    state transition methods.  It implements the 
    UserInterfacePluginServesComponent interface which has one method, 
    serveUserInterfaceComponent which gets a reference to the component,
    used later to obtain cluster object factories and cluster collections.
    */

public class UIPlugin extends SimplePlugin
  implements UISubscriber {

  private ClusterServesPlugin uiComponent = null;
  private Object componentLock = new Object();
  private ClusterObjectFactory clusterObjectFactory = null;
  private MessageAddress clusterIdentifier; // this cluster
  private Plan plan;
  private Hashtable subscribers = new Hashtable();
  private Vector clusters = new Vector(10); // clusters we know about
  private Vector assets = new Vector(10); // assets we know about

  /** Called during intialization to pass a reference to UIComponent.
    Implements a method defined in UserInterfacePlugin.
    Passes a reference to the UIComponent which this plugin, plugs in to.
    This is called after the start method is called.
    All methods that get information from the cluster wait
    until this method has been called.
    @param uiComponent user interface component to interact with
    @return the state of the plugin
    */

  protected void setupSubscriptions() {
    getSubscriber().setShouldBePersisted(false);
    clusterObjectFactory = getFactory();
    plan = clusterObjectFactory.getRealityPlan();
    synchronized (componentLock) {
      this.uiComponent = getCluster();
      componentLock.notifyAll();
    }
    rawSubscribe(this, assetPredicate());

    UIDisplay uiDisplay = new UIDisplay(this, getDelegate());
    Thread gui = new Thread(uiDisplay);
    gui.setName("uiPlugin:uiDisplay:" + gui.getName());
    gui.start();
  }


  /** Get the plan.
   */

  Plan getPlan() {
    waitUntilReady();
    return plan;
  }

  /** Get named plan.  Return the only plan for now.
   */

  Plan getPlan(String planName) {
    waitUntilReady();
    return plan;
  }

  /** Subscribe to assets so we can get cluster ids for
    tasks that the user creates and assets for displaying single
    asset schedules.
    */

  /** Get this cluster's cluster assets. */

  private static UnaryPredicate assetPredicate() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	//System.out.println("Predicate called with: " + o.toString());
	return ( o instanceof Asset );
      }
    };
  }

  /** UISubscriber interface.
    Notified when an asset is added, removed, or changed
    and update our lists of all assets and cluster assets.
    */

  public synchronized void subscriptionChanged(IncrementalSubscription container) {
    //System.out.println("Container changed");
    Enumeration added = container.getAddedList();
    Enumeration removed = container.getRemovedList();
    while (added.hasMoreElements()) {
      Object o = added.nextElement();
      assets.addElement(o);
      if (o instanceof Organization)
	clusters.addElement(o);
    }
    while (removed.hasMoreElements()) {
      Object o = removed.nextElement();
      assets.removeElement(o);
      if (o instanceof Organization)
	clusters.removeElement(o);
    }
  }

  public Vector getClusterNames() {
    Vector clusterNames = new Vector(clusters.size());
    for (int i = 0; i < clusters.size(); i++) {
      Organization cluster = (Organization)clusters.elementAt(i);
	ItemIdentificationPG itemIdProp =  
	  cluster.getItemIdentificationPG();
	if (itemIdProp != null)
	  clusterNames.addElement(cluster.getMessageAddress());
    }
    return clusterNames;
  }

  public Vector getPhysicalAssetNames() {
    Vector assetNames = new Vector(assets.size());
    String s = "";
    for (int i = 0; i < assets.size(); i++) {
      Asset a = (Asset)assets.elementAt(i);
      if (a instanceof PhysicalAsset)
	s = UIAsset.getName(a);
      else if (a instanceof AggregateAsset) {
	AggregateAsset aa = (AggregateAsset) a;
	if (aa.getAsset() != null)
	  s = UIAsset.getName(aa.getAsset());
	else
	  continue;
      } else
	continue;
      if ((s == null) || s.equals(""))
	continue;
      assetNames.addElement(s);
    }
    return assetNames;
  }

  public MessageAddress getClusterIdFromName(String clusterName) {
    String s = "";
    for (int i = 0; i < clusters.size(); i++) {
      Organization cluster = (Organization)clusters.elementAt(i);
      MessageAddress cid = cluster.getMessageAddress();
      if (cid != null && cid.getAddress().equals(clusterName)) 
        return cid;
    }
    return null;
  }


  /** Wait until component this is plugged into identifies itself.
   */

  private void waitUntilReady() {
    synchronized (componentLock) {
      while (uiComponent == null) {
	try {
	  componentLock.wait();
	} catch (InterruptedException e) {}
      }
    }
  }

  /** Called to subscribe to a cluster collection specified by the predicate.
    When the cluster collection has changed, the uiPlugin will notify the
    subscriber.  Thus, all uiPlugin threads call this method to subscribe
    to collections of their choice, and the uiPlugin notifies each when
    its subscription has changed.
    Synchronized to prevent execute from running before subscribe
    "records" the subscriber and their subscription.
    Carefully opens a transaction and then calls the lowlevel routine
    rawSubscribe.
    */
  

  void subscribe(UISubscriber uiSubscriber, UnaryPredicate predicate) {
    openTransaction();
    rawSubscribe(uiSubscriber, predicate);
    closeTransactionDontReset();
  }
  
  /** do the work of subscribe() above.  It is up to the caller to
   * be careful of transaction issues.
   **/

  void rawSubscribe(UISubscriber uiSubscriber, UnaryPredicate predicate) {
    IncrementalSubscription container = (IncrementalSubscription)subscribe(predicate);
    // record the container and the interested subscriber
    synchronized (subscribers) {
      subscribers.put(container, uiSubscriber);
    }
  }


  /** Called when any cluster collection has changed.  Check which
    cluster collections have changed and notify the subscriber.
    Synchronized to prevent execute from running before subscribe
    "records" the subscriber and their subscription.
    */

  public void execute()
  {
    //System.out.println("Execute called");
    synchronized (subscribers) {
    Enumeration containers = subscribers.keys();
    while (containers.hasMoreElements()) {
      IncrementalSubscription container = (IncrementalSubscription)containers.nextElement();
      if (container.hasChanged()) {
	UISubscriber uiSubscriber = (UISubscriber)subscribers.get(container);
	// if the user closed the display, there's no object associated
	// with the container, so we remove it
	if (uiSubscriber == null) {
	  //	  System.out.println("Removing container");
	  subscribers.remove(container);
	}
	else
	  uiSubscriber.subscriptionChanged(container);
      }
    }
  }
  }
}

