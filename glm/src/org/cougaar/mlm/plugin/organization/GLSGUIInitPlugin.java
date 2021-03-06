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

package org.cougaar.mlm.plugin.organization;


import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.util.UID;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.ldm.oplan.Oplan;
import org.cougaar.mlm.plugin.RandomButtonPusher;
import org.cougaar.mlm.plugin.UICoordinator;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.AspectValue;
import org.cougaar.planning.ldm.plan.ContextOfUIDs;
import org.cougaar.planning.ldm.plan.NewPrepositionalPhrase;
import org.cougaar.planning.ldm.plan.NewTask;
import org.cougaar.planning.ldm.plan.Preference;
import org.cougaar.planning.ldm.plan.ScoringFunction;
import org.cougaar.planning.ldm.plan.TimeAspectValue;
import org.cougaar.util.UnaryPredicate;

/**
 * The GLSGUIInitPlugin will create the initial GetLogSupport task
 * with the OPLAN object.
 *
 */
public class GLSGUIInitPlugin extends GLSGUIBasePlugin {
  private static final String MIN_SLEEP_PROP =
    "org.cougaar.mlm.plugin.organization.GLSGUIInitPlugin.minSleepTime";
  private static final String MAX_SLEEP_PROP =
    "org.cougaar.mlm.plugin.organization.GLSGUIInitPlugin.maxSleepTime";
  private static final int MIN_SLEEP_DFLT = 60000;
  private static final int MAX_SLEEP_DFLT = 180000;
  private static final String ENABLED_PROP =
    "org.cougaar.mlm.plugin.organization.GLSGUIInitPlugin.enabled";
  private static final String ENABLED_DFLT = "false";
  private static final String HIDDEN_PROP =
    "org.cougaar.mlm.plugin.organization.GLSGUIInitPlugin.hidden";
  private static final String HIDDEN_DFLT = "true";
  private IncrementalSubscription sendGLSRootSubscription;

  private static UnaryPredicate sendGLSRootPredicate = new UnaryPredicate() {
      public boolean execute(Object o) {
	if (o instanceof java.lang.String)
	  return ((o.toString()).equalsIgnoreCase("sendGLSRoot"));
	return false;   
      }
    };

  /** My private state **/
  private MyPrivateState myPrivateState;

  private IncrementalSubscription myPrivateStateSubscription;

  private static class MyPrivateState extends RandomButtonPusher {
    int taskNumber = 0;
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
  
  protected void sendPSP(OplanWrapper wrapper) {
    if (selfOrgAsset == null) {
      System.out.println("\n\nGLSGUIInitPlugin HAVEN'T RECEIVED SELF ORG ASSET YET.  TRY AGAIN LATER\n\n");
    } else {
      doPublishRootGLS(selfOrgAsset, wrapper.oplan);
    }
  }
  
  protected void buttonPushed(OplanWrapper wrapper) {
    if (selfOrgAsset == null) {
      System.out.println("\n\nGLSGUIInitPlugin HAVEN'T RECEIVED SELF ORG ASSET YET.  TRY AGAIN LATER\n\n");
    } else {
      publishRootGLS(selfOrgAsset, wrapper.oplan);
    }
  }

  protected boolean isPrivateStateOk() {
    return myPrivateState != null;
  }

  protected String getGLSLabelText(int nTasks) {
    if (myPrivateState.taskNumber == 0) {
      return "Ready to send GLS task";
    } else {
      return "Sent root GLS task " + myPrivateState.taskNumber;
    }
  }

  protected String getButtonText() {
    return "Send GLS Root";
  }

  protected String getGUITitle() {
    return "GLSInitPlugin";
  }

  protected void createSubscriptions() {
    myPrivateStateSubscription = RandomButtonPusher.subscribe(getDelegate(), MyPrivateState.class);
    sendGLSRootSubscription = (IncrementalSubscription)subscribe(sendGLSRootPredicate);
  }

  protected void restorePrivateState() {
    handlePrivateState(myPrivateStateSubscription.elements());
  }

  protected void createPrivateState() {
    publishAdd(new MyPrivateState()); // Prime the pump
  }

  /* This will be called every time a new task matches the above predicate */

  protected void handlePrivateState() {
    if (myPrivateStateSubscription.hasChanged()) {
      handlePrivateState(myPrivateStateSubscription.getAddedList());
    }
  }

  private void handlePrivateState(Enumeration e){
    if (myPrivateState == null && e.hasMoreElements()) {
      myPrivateState = (MyPrivateState) e.nextElement();
      if (!myPrivateState.isHidden()) {
        UICoordinator.layoutSecondRow(panel, myPrivateState.init("Random Send", getDelegate(), glsButton));
      }
      checkButtonEnable();
    }
  }
  
  public void sendThePSP()
  {
    OplanWrapper wrapper = (OplanWrapper) oplanCombo.getSelectedItem();
    if (wrapper != null) sendPSP(wrapper);
  }

  protected void additionalExecute()
  {

    Collection sendIt=sendGLSRootSubscription.getAddedCollection();
    if (sendIt!=null && sendIt.size() > 0) {
      for (Iterator iterator = sendIt.iterator();iterator.hasNext();) {
	Object object = iterator.next();
	getBlackboardService().publishRemove(object);
      }
      sendThePSP();
    }

  }
  
  public void publishRootGLS(Organization me, Oplan oplan) {
    openTransaction();
    doPublishRootGLS(me, oplan);
    closeTransactionDontReset();
  }

  private void doPublishRootGLS(Organization me, Oplan oplan) {    
    NewTask task = theLDMF.newTask();
    // ensure this is a root level task
    task.setPlan(theLDMF.getRealityPlan());
    task.setSource(this.getMessageAddress());
    task.setDestination(this.getMessageAddress());
    
    // set prepositional phrases
    Vector phrases = new Vector(3);
    NewPrepositionalPhrase newpp;

    // Removed following as being redundant with the task's context
    //      newpp = theLDMF.newPrepositionalPhrase();
    //      newpp.setPreposition(Constants.Preposition.WITH);
    //      newpp.setIndirectObject(oplan);
    //      phrases.add(newpp);

    newpp = theLDMF.newPrepositionalPhrase();
    newpp.setPreposition(Constants.Preposition.FOR);
    newpp.setIndirectObject(me);
    phrases.add(newpp);

    newpp = theLDMF.newPrepositionalPhrase();
    newpp.setPreposition("ForRoot");
    newpp.setIndirectObject(new Integer(++myPrivateState.taskNumber));
    publishChange(myPrivateState);

    phrases.add(newpp);

    task.setPrepositionalPhrases(phrases.elements());

    // verb
    task.setVerb(Constants.Verb.GetLogSupport);

    // schedule
    long startTime = currentTimeMillis();
    long endTime;
    Date endDay = oplan.getEndDay();
    if (endDay != null) {
      endTime = endDay.getTime();
    } else {
      Calendar cal = Calendar.getInstance();
      cal.setTime(new Date(startTime));
      // increment date by 3 MONTHs
      cal.add(Calendar.MONTH, 3);
      endTime = cal.getTime().getTime();
    }

    AspectValue startTav = TimeAspectValue.create(AspectType.START_TIME, startTime);
    AspectValue endTav = TimeAspectValue.create(AspectType.END_TIME, endTime);

    ScoringFunction myStartScoreFunc = ScoringFunction.createStrictlyAtValue( startTav );
    ScoringFunction myEndScoreFunc = ScoringFunction.createStrictlyAtValue( endTav );    

    Preference startPreference = theLDMF.newPreference( AspectType.START_TIME, myStartScoreFunc );
    Preference endPreference = theLDMF.newPreference( AspectType.END_TIME, myEndScoreFunc  );

    Vector preferenceVector = new Vector(2);
    preferenceVector.addElement( startPreference );
    preferenceVector.addElement( endPreference );

    task.setPreferences( preferenceVector.elements() );

    // Set the context
    try {
      UID oplanUID = oplan.getUID();
      ContextOfUIDs context = new ContextOfUIDs(oplanUID);
      System.out.println("GLSGUIInitPlugin: Setting context to: " + oplanUID);
      task.setContext(context);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    
    publishAdd(task);
    System.out.println("\n" + formatDate(System.currentTimeMillis()) + " Send Task: " + task);
  }
}
