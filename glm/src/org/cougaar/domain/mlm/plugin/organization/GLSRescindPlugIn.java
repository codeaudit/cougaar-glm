/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.plugin.organization;

import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.domain.planning.ldm.RootFactory;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.plan.Plan;
import org.cougaar.domain.planning.ldm.plan.Preposition;
import org.cougaar.domain.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.Verb;
import org.cougaar.core.plugin.PlugInAdapter;
import org.cougaar.core.plugin.SimplePlugIn;
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
import javax.swing.*;
import org.cougaar.domain.glm.Constants;
import org.cougaar.domain.glm.asset.Organization;
import org.cougaar.domain.glm.oplan.Oplan;
import org.cougaar.domain.mlm.plugin.RandomButtonPusher;
import org.cougaar.domain.mlm.plugin.UICoordinator;

/**
 * The GSLRescindPlugIn will rescind the ?? task
 *
 */

public class GLSRescindPlugIn extends GLSGUIBasePlugIn {
  private IncrementalSubscription myPrivateStateSubscription;
  private MyPrivateState myPrivateState = null;

  private static class MyPrivateState extends RandomButtonPusher {
    MyPrivateState() {
      super(Integer.getInteger("org.cougaar.domain.mlm.plugin.organization.GLSRescindPlugIn.minSleepTime", 60000).intValue(),
            Integer.getInteger("org.cougaar.domain.mlm.plugin.organization.GLSRescindPlugIn.maxSleepTime", 180000).intValue(),
            ((System.getProperty("org.cougaar.domain.mlm.plugin.organization.GLSRescindPlugIn.enabled") != null) ?
             "true".equalsIgnoreCase(System.getProperty("org.cougaar.domain.mlm.plugin.organization.GLSRescindPlugIn.enabled")) :
	     false));
    }
  };

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
    return "GLSRescindPlugIn";
  }

  protected void createSubscriptions() {
    myPrivateStateSubscription = RandomButtonPusher.subscribe(getDelegate(), MyPrivateState.class);
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
      UICoordinator.layoutSecondRow(panel, myPrivateState.init("Random Rescind",
                                                               getDelegate(),
                                                               glsButton));
      checkButtonEnable();
    }
  }
}
