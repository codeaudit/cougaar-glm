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

package org.cougaar.mlm.plugin.organization;

import org.cougaar.core.agent.ClusterIdentifier;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.domain.RootFactory;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.Plan;
import org.cougaar.planning.ldm.plan.Preposition;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.core.plugin.PluginAdapter;
import org.cougaar.core.plugin.SimplePlugin;
import org.cougaar.util.StateModelException;
import org.cougaar.util.UnaryPredicate;
import java.awt.*;
import java.awt.event.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import java.util.Collection;
import javax.swing.*;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.ldm.oplan.Oplan;
import org.cougaar.mlm.plugin.RandomButtonPusher;
import org.cougaar.mlm.plugin.UICoordinator;

/**
 * The GSLRescindPlugin will rescind the ?? task
 *
 */

public class GLSRescindPlugin extends GLSGUIBasePlugin {
  private static final String MIN_SLEEP_PROP =
    "org.cougaar.mlm.plugin.organization.GLSRescindPlugin.minSleepTime";
  private static final int MIN_SLEEP_DFLT = 60000;
  private static final String MAX_SLEEP_PROP =
    "org.cougaar.mlm.plugin.organization.GLSRescindPlugin.maxSleepTime";
  private static final int MAX_SLEEP_DFLT = 180000;
  private static final String ENABLED_PROP =
    "org.cougaar.mlm.plugin.organization.GLSRescindPlugin.enabled";
  private static final String ENABLED_DFLT = "false";
  private static final String HIDDEN_PROP =
    "org.cougaar.mlm.plugin.organization.GLSRescindPlugin.hidden";
  private static final String HIDDEN_DFLT = "true";
  private IncrementalSubscription myPrivateStateSubscription;
  private MyPrivateState myPrivateState = null;
  private IncrementalSubscription rescindGLSRootSubscription;

private static UnaryPredicate rescindGLSRootPredicate = new UnaryPredicate() {
	                public boolean execute(Object o) {
				if (o instanceof java.lang.String)
 		                   return ((o.toString()).equalsIgnoreCase("rescindGLSRoot"));
				return false;   
	                }
	       };

  private static class MyPrivateState extends RandomButtonPusher {
    boolean hidden;
    MyPrivateState() {
      super(Integer.getInteger(MIN_SLEEP_PROP, MIN_SLEEP_DFLT).intValue(),
            Integer.getInteger(MAX_SLEEP_PROP, MAX_SLEEP_DFLT).intValue(),
            "true".equalsIgnoreCase(System.getProperty(ENABLED_PROP, ENABLED_DFLT)));
      hidden = "true".equalsIgnoreCase(System.getProperty(HIDDEN_PROP, HIDDEN_DFLT));
    }
    public boolean isHidden() {
      return hidden;
    }
  }
  
  public void rescindPSP(OplanWrapper wrapper) {
	if (wrapper.tasks.isEmpty())
		return;
	for (Iterator i = wrapper.tasks.iterator(); i.hasNext(); ) {
		Task t = (Task) i.next();
		publishRemove(t);
		System.out.println("\n" + formatDate(System.currentTimeMillis()) + " Rescinded Task: " + t);
	}
}  

  public void buttonPushed(OplanWrapper wrapper) {
    if (wrapper.tasks.isEmpty())
      return;

    openTransaction();
    for (Iterator i = wrapper.tasks.iterator(); i.hasNext(); ) {
      Task t = (Task) i.next();
      publishRemove(t);
      System.out.println("\n" + formatDate(System.currentTimeMillis()) + " Rescinded Task: " + t);
    }
    closeTransaction(false);
  }

  protected boolean isPrivateStateOk() {
    return myPrivateState != null;
  }

  protected String getGLSLabelText(int nTasks) {
    if (nTasks > 0) {
      return nTasks + " task" + ((nTasks == 1) ? "" : "s") + " to rescind";
    } else {
      return "No tasks to rescind";
    }
  }

  protected String getButtonText() {
    return "Rescind GLS Root";
  }

  protected String getGUITitle() {
    return "GLSRescindPlugin";
  }

  protected void createSubscriptions() {
    myPrivateStateSubscription = RandomButtonPusher.subscribe(getDelegate(), MyPrivateState.class);
    rescindGLSRootSubscription = (IncrementalSubscription) getBlackboardService().subscribe(rescindGLSRootPredicate);
  }
  
  public void rescindThePSP() {
	OplanWrapper wrapper = (OplanWrapper) oplanCombo.getSelectedItem();
	if (wrapper != null) rescindPSP(wrapper);
  }

  protected void additionalExecute()
  {
	Collection rescindIt=rescindGLSRootSubscription.getAddedCollection();
	if (rescindIt!=null && rescindIt.size() > 0) {
		for (Iterator iterator = rescindIt.iterator();iterator.hasNext();) {
			Object object = iterator.next();
			getBlackboardService().publishRemove(object);
		}
		rescindThePSP();
	}
  }  

  protected void restorePrivateState() {
    handlePrivateState(myPrivateStateSubscription.elements());
  }

  protected void createPrivateState() {
    publishAdd(new MyPrivateState()); // Prime the pump
  }

  protected void handlePrivateState() {
    if (myPrivateStateSubscription.hasChanged()) {
      handlePrivateState(myPrivateStateSubscription.getAddedList());
    }
  }

  private void handlePrivateState(Enumeration e){
    if (myPrivateState == null && e.hasMoreElements()) {
      myPrivateState = (MyPrivateState) e.nextElement();
      if (!myPrivateState.isHidden()) {
        UICoordinator.layoutSecondRow(panel, myPrivateState.init("Random Rescind",
                                                                 getDelegate(),
                                                                 glsButton));
      }
      checkButtonEnable();
    }
  }
}
