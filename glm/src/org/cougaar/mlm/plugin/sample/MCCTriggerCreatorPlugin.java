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

package org.cougaar.mlm.plugin.sample;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.CargoVehicle;
import org.cougaar.mlm.plugin.UICoordinator;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.asset.AbstractAsset;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.NewPrepositionalPhrase;
import org.cougaar.planning.ldm.plan.NewTask;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.RoleSchedule;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.planning.ldm.trigger.Trigger;
import org.cougaar.planning.ldm.trigger.TriggerAction;
import org.cougaar.planning.ldm.trigger.TriggerMonitor;
import org.cougaar.planning.ldm.trigger.TriggerPredicateBasedMonitor;
import org.cougaar.planning.plugin.legacy.PluginDelegate;
import org.cougaar.planning.plugin.legacy.SimplePlugin;
import org.cougaar.util.UnaryPredicate;

public class MCCTriggerCreatorPlugin extends SimplePlugin
{
    
    JFrame frame;

    /** Create a label for feedback on if the root task was sent **/
    protected JLabel triggerLabel = new JLabel();
    
    private IncrementalSubscription myAssets;
    private IncrementalSubscription maintAssets;
    private ArrayList myVehicles = new ArrayList();
    private static long ONE_DAY = 86400000L;
    private AbstractAsset maintenanceAsset;

    //
    // Begin inner class MCCTriggerAction
    //
    private static class MCCTriggerAction implements TriggerAction
    {
        // Empty Constructor
        MCCTriggerAction() {}

        public void Perform( Object[] objects, PluginDelegate pid ) 
        {
            PlanningFactory ldmf = pid.getFactory();
            List al = Arrays.asList( objects );
            ListIterator li = al.listIterator();
            while ( li.hasNext() ) {
                Object o = li.next();
                if ( o instanceof Allocation ) {
                    Allocation alloc = (Allocation)o;
                    Asset asset = alloc.getAsset();
                    if (!(asset.getTypeIdentificationPG().getTypeIdentification().equals("MAINTENANCE"))) {
                        // This Allocation is conflicting with the new MAINTENANCE Allocation, so
                        // we want to rescind this Allocation and create a failed Disposition in its place
                        Task t = alloc.getTask();
                        pid.publishRemove( alloc );
                        AllocationResult ar =
                            ldmf.newAllocationResult(0.0, false, new int[1], new double[1]);
                        PlanElement fa =
                            ldmf.createFailedDisposition(ldmf.getRealityPlan(), t, ar);
                        pid.publishAdd( fa );
                    }
                }
            }
        }

    }
    //
    // End inner class MCCTriggerAction
    //
    
    
    //
    // Begin inner class TriggerButtonListener
    //
    class TriggerButtonListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e) 
        {
            String ac = e.getActionCommand();
            openTheTransaction();
            tweakTrigger();
            closeTheTransaction();
            triggerLabel.setText("HETs in Maintenance");
        }    
    }
    //
    // End inner class TriggerButtonListener
    //
                

    // Provide these methods for benefit of inner classes
    public void openTheTransaction() 
    {
        openTransaction();
    }

    public void closeTheTransaction() 
    {
        closeTransaction();
    }

    PlanningFactory getLDMF() { return theLDMF; }

    private static UnaryPredicate triggPred() {
      return new UnaryPredicate() {
	public boolean execute( Object o ) {
	  if ( o instanceof Allocation ) {
            /*
              // disable calling isPotentialConflict for now
	    if ( ((Allocation)o).isPotentialConflict() ) {
	      return true;
	    }
            */
	  }
	  return false;
	}
      };
    }

    // Predicate for our transportation Assets (ships, HETS, and the like)...
    private static UnaryPredicate assetPred() {
      return new UnaryPredicate() {
	public boolean execute( Object o ) {
	  return ( o instanceof CargoVehicle );
	}
      };
    }

    // Predicate to keep track of our (AbstractAsset) Maintenance orgs
    private static UnaryPredicate maintAssetPred() {
      return new UnaryPredicate() {
	public boolean execute(Object o) {
	  if (o instanceof AbstractAsset) {
	    if (((AbstractAsset)o).getTypeIdentificationPG().getTypeIdentification().equals("MAINTENANCE"))
	      return true;
	  }
	  return false;
	}
      };
    }

    private void createGUI() 
    {
        frame = new JFrame("MCCTriggerCreatorPlugin");
        frame.getContentPane().setLayout(new FlowLayout());
        JPanel panel = new JPanel();
        // Create the button
        JButton triggerButton = new JButton("Maintain HETs");
        
        // Register a listener for the radio buttons
        TriggerButtonListener myTListener = new TriggerButtonListener();
        triggerButton.addActionListener(myTListener);
        UICoordinator.layoutButtonAndLabel(panel, triggerButton, triggerLabel);
        frame.setContentPane(panel);
        frame.pack();
	UICoordinator.setBounds(frame);
        frame.setVisible(true);
    }
    
    private boolean tweakTrigger() 
    {
        if (myVehicles.size() == 0) return false;
        ListIterator li = myVehicles.listIterator();
        long now = currentTimeMillis();
        while (li.hasNext()) {
            Asset asset = (Asset)li.next();
            RoleSchedule rs = asset.getRoleSchedule();
            long startTime = now;
            long endTime = now + ONE_DAY;
            Task maintTask = createTask(asset);
            int[] aspectarray = new int[2];
            double[] resultsarray = new double[2];
            aspectarray[0] = AspectType.START_TIME;
            aspectarray[1] = AspectType.END_TIME;
            resultsarray[0] = (double) startTime;
            resultsarray[1] = (double) endTime;
            AllocationResult ar =
	      theLDMF.newAllocationResult(0.0, true, aspectarray, resultsarray);
            Allocation alloc =
	      theLDMF.createAllocation(theLDMF.getRealityPlan(),
				       maintTask,
				       asset,
				       ar,
				       Constants.Role.MAINTAINER);
            publishAdd(alloc);
        }
        return true;
    }

    private Task createTask(Asset asset) 
    {
        NewTask nt = theLDMF.newTask();
        nt.setPlan(theLDMF.getRealityPlan());
        nt.setVerb(Verb.get(Constants.Verb.MAINTAIN));
        nt.setDirectObject(asset);
        NewPrepositionalPhrase npp = theLDMF.newPrepositionalPhrase();
        npp.setPreposition(Constants.Preposition.USING);
        npp.setIndirectObject(maintenanceAsset);
        nt.setPrepositionalPhrases(npp);
        publishAdd(nt);
        return nt;
    }

    private void checkAssets(Enumeration assets) 
    {
        while (assets.hasMoreElements()) {
            Asset asset = (Asset)assets.nextElement();
            addAsset(asset);
        }
    }
    
    private void addAsset(Asset asset) 
    {
        if (myVehicles.contains(asset)) return;
        myVehicles.add(asset);
    }

    private void removeAsset( Asset asset ) 
    {
        if (!myVehicles.contains(asset)) return;
        myVehicles.remove(asset);
    }

    private void checkMaintAssets()
    {
        if (maintenanceAsset == null) {
            Enumeration tmp = maintAssets.getAddedList();
            while (tmp.hasMoreElements()) {
                // Really only expecting one AbstractAsset here
                maintenanceAsset = (AbstractAsset)tmp.nextElement();
                continue;
            }
        }
    }

    public void setupSubscriptions() 
    {
        getSubscriber().setShouldBePersisted(false);
        myAssets = (IncrementalSubscription)subscribe(assetPred());
        maintAssets = (IncrementalSubscription)subscribe(maintAssetPred());
        if (!didRehydrate()) {
          createTriggers();
        }
        createGUI();
    }
    
    /**
     * Setup the Triggers and add them to the LogPlan
     */
    public void execute() 
    {
        if (maintAssets.hasChanged())
            checkMaintAssets();
        checkAssets(myAssets.getAddedList());
        checkAssets(myAssets.getChangedList());
        Enumeration e = myAssets.getRemovedList();
        while (e.hasMoreElements()) {
            removeAsset((Asset)e.nextElement());
        }
    }
    
    /**
     * Create the Triggers
     */
    private void createTriggers() 
    {
        TriggerMonitor tm = new TriggerPredicateBasedMonitor( triggPred() );
        // We don't need a Tester, as all objects in the Monitor will be
        // elibible for action.
        TriggerAction ta = new MCCTriggerAction();
        Trigger myTrigger = new Trigger( tm, null, ta );
        publishAdd( myTrigger );
    }

    
}
