/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */


package org.cougaar.domain.mlm.debug.ui;

import javax.swing.JFrame;
import javax.swing.JMenuItem;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.domain.planning.ldm.asset.AggregateAsset;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.glm.ldm.asset.PhysicalAsset;
import org.cougaar.domain.glm.ldm.asset.Organization;
import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.util.OptionPane;
import org.cougaar.util.UnaryPredicate;

/** Displays information in a vertical bar graph.
 */

public class UIBarGraphDisplay implements Runnable, ActionListener, UISubscriber {
  private UIPlugIn uiPlugIn;
  private String planName;
  private ClusterIdentifier clusterId;
  private String command;
  private UIBarGraph uiBarGraph;
  private UIBarGraphSource dataSource;
  private Vector myAssets = new Vector(10);
  private boolean graphDisplayed = false;
  private String title = "";
  private UIFrame uiFrame;

  /** Contains the correspondence between display and data source.
   */

  /** Display the specified data (either assets or scheduled assets)
    in a bar graph.
    All the work is done in the run method so
    that the main user interface thread which creates this,
    isn't waiting to fetch the information needed for the bar graph.
    @param uiPlugIn this user interface plugin
    @param planName the name of the plan for which to display information
    @param clusterId the cluster for which to display information
    @param command UIDisplay.ASSET_SCHEDULE_COMMAND or UIDisplay.ASSETS_COMMAND
   */

  public UIBarGraphDisplay(UIPlugIn uiPlugIn, String planName, 
			   ClusterIdentifier clusterId, String command) {
    this.uiPlugIn = uiPlugIn;
    this.planName = planName;
    this.clusterId = clusterId;
    this.command = command;
    //    uiPlugIn.subscribe(this, assetPredicate());
  }

  private static UnaryPredicate assetPredicate() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	//System.out.println("Predicate called with: " + o.toString());
	return ( o instanceof Asset );
      }
    };
  }

  public synchronized void subscriptionChanged(IncrementalSubscription container) {
    Enumeration added = container.getAddedList();
    while (added.hasMoreElements())
      myAssets.addElement(added.nextElement());
    Enumeration removed = container.getRemovedList();
    while (removed.hasMoreElements())
      myAssets.removeElement(removed.nextElement());
    Enumeration changed = container.getChangedList();
    while (changed.hasMoreElements()) {
      Object o = changed.nextElement();
      myAssets.removeElement(o);
      myAssets.addElement(o);
    }
  }

  /** Create the data source for the bar graph, the bar graph, and its window.
    Fill in the graph here so that delays in accessing information
    from clusters don't affect the main user interface thread.
    Register as a listener for changes in the information in the bar graph.
    */

  public void run() {
    title = "";

    try {
      if (command.equals(UIDisplay.ASSET_SCHEDULE_COMMAND)) {
	// asset name is null for graph all assets
	dataSource = new UISchedule(uiPlugIn, planName, clusterId, null, this);
	title = "Asset Schedule in " + 
	  clusterId.getAddress();
      }
      else if (command.equals(UIDisplay.SINGLE_ASSET_SCHEDULE_COMMAND)) {
	Vector tmp = uiPlugIn.getPhysicalAssetNames();
	String assetNames[] = new String[tmp.size()];
	tmp.copyInto(assetNames);
	String assetChosen = 
	  (String) OptionPane.showInputDialog(new JFrame(),
                                              "Choose an asset:", "Asset",
                                              OptionPane.QUESTION_MESSAGE,
                                              null, assetNames, null);
	if (assetChosen == null)
	  return;
	// create information for bar graph schedule for chosen asset
	dataSource = new UISchedule(uiPlugIn, planName, 
				    clusterId, assetChosen, this);
	title = "Single Asset Schedule in " +
	  clusterId.getAddress();
      }
      else if (command.equals(UIDisplay.ASSETS_COMMAND)) {
	dataSource = new UIAssets(uiPlugIn, planName, clusterId, this);
	title = "Assets in " +
	  clusterId.getAddress();
      } else {
	new UIDisplayError("This request is not supported.");
	return;
      }
    } catch (UINoPlanException e) {
      new UIDisplayError(e.getMessage());
      return;
    }
    uiBarGraph = new UIBarGraph();
    // create frame with update function (if non-local) and no save function
    uiFrame = new UIFrame(title, uiBarGraph, this, false, false, false);
    //    dataSource.registerListener(this);
    dataSource.startSubscription();
  }


  /** Redisplay bar graph.
    This gets called:
    if the dataSource (a collection) is notified that it was changed or
    the user requests an update,
    i.e. this is used for changes stimulated either internally 
    (the data changed) or externally (the user wants an update).
    The reason for the duality is that we can listen for changes in the
    LOCAL cluster, but not in REMOTE clusters.
    If the change was stimulated internally, then the data source got
    the change, and updated its values, and this simply redisplays;
    if the change was stimulated externally (by the user), then this
    first calls the data source to update the values, and then redisplays.
    @param e the action event (object added, deleted or changed)
   */

  public void actionPerformed(ActionEvent e) {
    Object source = e.getSource();
    if (source instanceof UIBarGraphSource) {
      uiBarGraph.setParameters(dataSource.getNumberOfXIntervals(),
			       dataSource.getXLegend(),
			       dataSource.getXLabels(),
			       dataSource.getNumberOfYIntervals(),
			       dataSource.getYLegend(),
			       dataSource.getYLabels(),
			       dataSource.getLegend(),
			       dataSource.getValues());
    } else if (e.getActionCommand().equals(UIFrame.UPDATE_COMMAND)) {
	dataSource.update();
	uiBarGraph.setParameters(dataSource.getNumberOfXIntervals(),
			       dataSource.getXLegend(),
			       dataSource.getXLabels(),
			       dataSource.getNumberOfYIntervals(),
			       dataSource.getYLegend(),
			       dataSource.getYLabels(),
			       dataSource.getLegend(),
			       dataSource.getValues());
    } else
	System.out.println("UIBarGraphDisplay: unknown notification.");
  }


}



