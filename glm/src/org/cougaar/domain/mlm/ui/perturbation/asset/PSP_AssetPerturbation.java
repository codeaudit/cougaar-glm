/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.perturbation.asset;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.*;

import org.cougaar.core.cluster.*;
import org.cougaar.domain.planning.ldm.RootFactory;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.measure.Latitude;
import org.cougaar.domain.planning.ldm.measure.Longitude;
import org.cougaar.domain.planning.ldm.plan.*;
import org.cougaar.core.plugin.Assessor;
import org.cougaar.core.society.UID;
import org.cougaar.lib.planserver.*;
import org.cougaar.core.util.*;
import org.cougaar.util.*;


import org.cougaar.domain.glm.*;
import org.cougaar.domain.glm.ldm.*;
import org.cougaar.domain.glm.ldm.asset.*;
import org.cougaar.domain.glm.ldm.oplan.*;
import org.cougaar.domain.glm.ldm.plan.*;
import org.cougaar.domain.glm.ldm.policy.*;

/**
 * PSP_AssetPerturbation - PSP for retrieving info messages
 * Optionally allows caller to specify a since date for the messages
 * Currently returns messages as HTML
 */

public class PSP_AssetPerturbation extends PSP_BaseAdapter 
  implements PlanServiceProvider, UISubscriber, Assessor {

  private static boolean debug = false;

  private Calendar myCalendar = Calendar.getInstance();
  private UID myModifyUID;

  /** 
   * Constructor -  A zero-argument constructor is required for dynamically 
   * loaded PSPs by Class.newInstance()
   **/
  public PSP_AssetPerturbation() {
    super();
  }
    
  /**
   * Constructor -
   *
   * @param pkg String specifying package id
   * @param id String specifying PSP name
   * @throw org.cougaar.lib.planserver.RuntimePSPException
   */
  public PSP_AssetPerturbation(String pkg, String id) 
    throws RuntimePSPException {
    setResourceLocation(pkg, id);
  }
  
  /**
   * allAssetPred - subscribes for all Assets
   */
  private static UnaryPredicate allAssetPred = new UnaryPredicate() {
    public boolean execute(Object o) {
      return (o instanceof CargoVehicle);
    }
  };            

    /**
     * assetByUIDPred - subscribes for to a specific Asset
     */
    private UnaryPredicate assetByUIDPred = new UnaryPredicate() {
        public boolean execute(Object o) {
          if (o == null) {
            if (debug) {
              System.out.println ("Huh? Got null object in predicate?");
            }
          } else if (o instanceof Asset) {
            if (((Asset)o).getUID() == null) {
              if (debug) { 
                System.out.println ("Huh? Got null UID in predicate?");
              }
            } else if (((Asset)o).getUID().equals(myModifyUID))
            return true;
          }
          return false;
        }
    };  
  
  /**
   * test - Always returns false as currently implemented.
   * See doc in org.cougaar.lib.planserver.PlanServiceProvider
   *
   * @param queryParamaters HttpInput
   * @param sc PlanServiceContext
   */
  public boolean test(HttpInput queryParameters, PlanServiceContext sc) {
    super.initializeTest();
    return false;  // This PSP is only accessed by direct reference.
  }
  
  
  /**
   * execute - creates HTML with the relevant Assets
   *
   * @param out PrintStream to which output will be written
   * @param queryParameters HttpInput with screen date parameter. Parameter
   * is optional.
   * @param psc PlanServiceContext
   * @param psu PlanServiceUtilities
   **/
  public void execute(PrintStream out,
                      HttpInput queryParameters,
                      PlanServiceContext psc,
                      PlanServiceUtilities psu) throws Exception {
    if (queryParameters.hasBody()) {
      String postData = queryParameters.getBodyAsString();
      ParsePosition parsePosition = new ParsePosition(0);
      String command = 
        AssetPerturbationMessage.parseParameter(postData, 
                                                AssetPerturbationMessage.COMMAND_LABEL, 
                                                AssetPerturbationMessage.DELIM,
                                                parsePosition);
      
      if (command.equals(AssetPerturbationMessage.QUERY_COMMAND)) {
        listAssets(out, psc, psu);
      } else if (command.equals(AssetPerturbationMessage.MODIFY_COMMAND)) {
        modifyAsset(postData, parsePosition, out, psc, psu);
      } else {
        throw new RuntimePSPException("Unrecognized command - " + postData);
      }
    }      
  }

  
  /**
   * returnsXML - returns true if PSP can output XML.  Currently always false.
   * 
   * @return boolean 
   **/
  public boolean returnsXML() {
    return false;
  }
  
  /**
   * returnsHTML - returns true if PSP can output HTML.  Currently always true.
   * 
   * @return boolean 
   **/
  public boolean returnsHTML() {
    return true;
  }
  
  /** 
   * getDTD - returns null. PSP does not return XML.
   * Any PlanServiceProvider must be able to provide DTD of its
   * output IFF it is an XML PSP... ie.  returnsXML() == true;
   *
   * @return String
   **/
  public String getDTD() {
    return null;
  }

  /**
   * subscriptionChanged - doesn't do anything. All the data currently returned
   * by execute.
   *
   * @param subscription Subscription
   */
  public void subscriptionChanged(Subscription subscription) {
  }

  private void listAssets(PrintStream out,
                            PlanServiceContext psc,
                            PlanServiceUtilities psu) throws Exception {
    Subscription subscription = 
      psc.getServerPlugInSupport().subscribe(this, allAssetPred);
    Enumeration assets = ((CollectionSubscription)subscription).elements();

    myCalendar.set(1995, 0, 1, 0, 0, 0);
    long startTime = myCalendar.getTime().getTime();

    myCalendar.set(2005, 11, 31, 0, 0, 0);
    long endTime = myCalendar.getTime().getTime();

    while (assets.hasMoreElements()) {
      Asset asset = (Asset)assets.nextElement();
      RoleSchedule roleSchedule = asset.getRoleSchedule();
      Collection overlapSchedule = 
          roleSchedule.getOverlappingRoleSchedule(startTime, endTime);
      if (!overlapSchedule.isEmpty()) {
        String name = asset.toString();
        
        UID uid = asset.getUID();
        String info = AssetPerturbationMessage.NAME_LABEL + name + 
          AssetPerturbationMessage.DELIM + 
          AssetPerturbationMessage.UID_LABEL + uid + 
          AssetPerturbationMessage.DELIM;
        
        out.println("<HTML><BODY> <FONT color=#CC0000>" + 
                    info + 
                    "</FONT></BODY></HTML><p>");
      }
    }
    out.close();
  }
  
  private void modifyAsset(String modifyArgs, 
                           ParsePosition parsePosition,
                           PrintStream out,
                           PlanServiceContext psc,
                           PlanServiceUtilities psu) throws RuntimePSPException {
    
    String uidStr = 
      AssetPerturbationMessage.parseParameter(modifyArgs, 
                                              AssetPerturbationMessage.UID_LABEL, 
                                              AssetPerturbationMessage.DELIM, 
                                              parsePosition);
    if (uidStr.equals("")) {
      throw new RuntimePSPException("No UID for alert.");
    }
    myModifyUID = new UID(uidStr);
      
    if (debug)
        System.out.println ("myModifyUID is " + myModifyUID);

    String startStr = 
      AssetPerturbationMessage.parseParameter(modifyArgs, 
                                              AssetPerturbationMessage.START_LABEL, 
                                              AssetPerturbationMessage.DELIM,
                                              parsePosition);
    if (startStr.equals("")) {
      throw new RuntimePSPException("No start date.");
    }

    Date startDate = null;
    try {
      startDate = AssetPerturbationMessage.getDateFormat().parse(startStr);
    } catch (ParseException pe) {
      throw new RuntimePSPException("Unable to parse start date = " + startStr);
    }

    String endStr = 
      AssetPerturbationMessage.parseParameter(modifyArgs, 
                                              AssetPerturbationMessage.END_LABEL, 
                                              AssetPerturbationMessage.DELIM, 
                                              parsePosition);
    if (endStr.equals("")) {
      throw new RuntimePSPException("No end date.");
    }

    Date endDate = null;
    try {
      endDate = AssetPerturbationMessage.getDateFormat().parse(endStr);
    } catch (ParseException pe) {
      throw new RuntimePSPException("Unable to parse end date = " + endStr);
    }

    ServerPlugInSupport serverPluginSupport = psc.getServerPlugInSupport();
    Subscription subscription = 
      serverPluginSupport.subscribe(this, assetByUIDPred);
    Collection container = 
      ((CollectionSubscription)subscription).getCollection();
    

    if (container.size() == 0) {
      System.out.println("PSP_AssetPerturbation.execute(): query by UID " + 
                         myModifyUID + 
                         " returned no assets.");
      throw new RuntimePSPException("UID " + myModifyUID + 
                                    " does not exist.");
    } else if (container.size() > 1) {         
      System.out.println("PSP_AssetPerturbation.execute(): query by UID " + 
                         myModifyUID + 
                         " returned multiple alerts.");
      throw new RuntimePSPException("UID " + myModifyUID + " not unique.");
    }          
    
    Enumeration assetsByUID  = new Enumerator(container);
    Asset asset = (Asset)assetsByUID.nextElement();

    RoleSchedule roleSchedule = asset.getRoleSchedule();
    Collection overlapSchedule = 
      roleSchedule.getOverlappingRoleSchedule(startDate.getTime(), 
                                              endDate.getTime());
    if (!overlapSchedule.isEmpty()) {
      Enumeration planElements = new Enumerator(overlapSchedule);
      if (debug) {
        System.out.println("PSP_AssetPerturbation - removing plan elements involving "  + asset);
      }
      Vector stuffToRemove = new Vector ();
      Vector originalTasks = new Vector ();
      publishRemoveUntilBoundary(psc, planElements, stuffToRemove, 
                                 originalTasks);
      for (int i = 0; i < stuffToRemove.size (); i++) {
          serverPluginSupport.publishRemoveForSubscriber(stuffToRemove.elementAt(i));
      }
      for (int i = 0; i < originalTasks.size (); i++) {
          NewTask oTask = (NewTask)originalTasks.elementAt(i);
          if (debug && (oTask.getWorkflow () != null)) {
            System.out.println ("original task " + oTask + " had non-null workflow");
          }
          oTask.setWorkflow (null);
          serverPluginSupport.publishChangeForSubscriber(oTask);
      }
    }

    RootFactory ldmFactory = 
      serverPluginSupport.getFactoryForPSP();
    
    NewTask newTask = createUnavailableTask(asset, startDate, endDate, 
                                            ldmFactory);
    ClusterIdentifier clusterID = 
      ClusterIdentifier.getClusterIdentifier(serverPluginSupport.getClusterIDAsString());
    newTask.setSource(clusterID);
    newTask.setDestination(clusterID);
    serverPluginSupport.publishAddForSubscriber(newTask);

    AllocationResult allocationResult = 
      createEstimatedAllocationResult(newTask, ldmFactory);
    Allocation allocation = 
      ldmFactory.createAllocation(ldmFactory.getRealityPlan(),
                                  newTask,
                                  asset,
                                  allocationResult,
                                  Constants.Role.OUTOFSERVICE);
    serverPluginSupport.publishAddForSubscriber(allocation);

    if (debug) {
      System.out.println("\nSchedule immediately after publish.\n" + 
                         "Should be unavailable from " + startDate + 
                         " to " + endDate + 
                         "\nAllocation is " + allocation + 
                         " to asset " + asset);
    }

    myCalendar.set(1999, 0, 1, 0, 0, 0);
    long startTime = myCalendar.getTime().getTime();

    myCalendar.set(1999, 11, 31, 23, 59, 0);
    long endTime = myCalendar.getTime().getTime();

    roleSchedule = asset.getRoleSchedule();
    Collection encapSchedule = 
      roleSchedule.getEncapsulatedRoleSchedule(startTime, endTime);
    if (!encapSchedule.isEmpty()) {
      Enumeration planElements = new Enumerator(encapSchedule);
      while (planElements.hasMoreElements()) {
        PlanElement planElement = (PlanElement)planElements.nextElement();
        AllocationResult result = planElement.getEstimatedResult();
        if (result != null) {
          // make sure that the start and end time aspect values are defined.
          // remember that they may change due to notification feedback
          if ((result.isDefined(AspectType.START_TIME)) && 
              (result.isDefined(AspectType.END_TIME))) {
            Date eStart = 
              new Date((long)result.getValue(AspectType.START_TIME));
            Date eEnd = new Date((long)result.getValue(AspectType.END_TIME));
            if (debug) {
                System.out.println(planElement + " start: " + eStart + 
                                   " end: " + eEnd);
            }
          }
        }
      } 
    }

    serverPluginSupport.unsubscribeForSubscriber(subscription);    
  }
    
  /**
   *
   */
  protected void publishRemoveUntilBoundary (PlanServiceContext psc,
                                             Enumeration planElements, 
                                             Vector stuffToRemove,
                                             Vector originalTasks) {
    ServerPlugInSupport serverPluginSupport = psc.getServerPlugInSupport();
    Vector parentPEs = new Vector ();
    if (debug) {
      System.out.println("PSP_AssetPerturbation - Level --->");
    }

    while (planElements.hasMoreElements()) {
      PlanElement planElement = (PlanElement)planElements.nextElement();
      Task task = planElement.getTask();
      Verb taskVerb = task.getVerb();
      if (!taskVerb.equals(Constants.Verb.IDLE) &&
          !taskVerb.equals(Constants.Verb.MAINTAIN)) { 
        if (debug) {
          System.out.println("PSP_AssetPerturbation - removing " + planElement.getUID () + 
                             "-" + planElement);
        }
        if (!stuffToRemove.contains(planElement))
          stuffToRemove.add(planElement);
      }
      
      if (task.getSource().equals(task.getDestination())) {
        if (task instanceof MPTask) {
          Enumeration mpParents = ((MPTask)task).getParentTasks();
          while (mpParents.hasMoreElements()) {
            parentPEs.add(((Task)mpParents.nextElement()).getPlanElement());
          }
        } 
        else {
          System.err.println("PSP_AssetPerturbation plugin is broken - tried to get planelement of parenttask");
          //Try getting parent task from workflow instead.
          //parentPEs.add(task.getParentTask().getPlanElement());
        }
        
        if (!stuffToRemove.contains(task)) {
          stuffToRemove.add (task);
        }

        if (debug) {
          System.out.println("PSP_AssetPerturbation - removing task " + task);
        }
      } else {
        if (debug) {
          System.out.println("PSP_AssetPerturbation - FOUND initial task " + task);
        }
        if (!originalTasks.contains(task)) {
          originalTasks.add (task);
        }
      }
    }

    if (!parentPEs.isEmpty()) {
      publishRemoveUntilBoundary(psc, parentPEs.elements (), 
                                 stuffToRemove, originalTasks);
    }
  }
  
  private AllocationResult createEstimatedAllocationResult(Task task,
                                                           RootFactory ldmFactory) {
    Enumeration preferences = task.getPreferences();
    if (preferences != null) {
      // Walk pref enum to find out count. Otherwise we end up having to 
      // convert aspect and result vectors to arrays as post processing.
      // Safe because no one else could modify the task.
      int numPreferences;
      for (numPreferences = 0; preferences.hasMoreElements(); 
           numPreferences++, preferences.nextElement()) {
      }

      // No way to reset the enum so we need to get again.
      // I hate Enumerations
      preferences = task.getPreferences();
      // do something really simple for now.
      int[] aspectArray = new int[numPreferences];
      double[] resultArray = new double[numPreferences];
      for (int index = 0; index < numPreferences; index++) {
        Preference pref = (Preference)preferences.nextElement();
        aspectArray[index] = pref.getAspectType();

        ScoringFunction sf = pref.getScoringFunction();
        // allocate as if you can do it at the "Best" point
        resultArray[index] = ((AspectScorePoint)sf.getBest()).getValue();

        if (debug)
            if (aspectArray[index] < 2)
                System.out.println ("Aspect " + aspectArray[index] + 
                                    " - " + new Date((long)resultArray[index]));
      }
      AllocationResult estimate = ldmFactory.newAllocationResult(100.0, 
                                                                 true, 
                                                                 aspectArray, 
                                                                 resultArray);
      return estimate;
    }
    // if there were no preferences...return a null estimate for the allocation result
    // (for now)
    return null;
  }


  private NewTask createUnavailableTask(Asset asset, Date startDate, Date endDate, 
                                        RootFactory ldmFactory) {
    //So how do I make asset unavailable??
    //Make a task
    NewTask newTask = ldmFactory.newTask();
    newTask.setVerb(new Verb(Constants.Verb.IDLE));
    
    newTask.setPlan(ldmFactory.getRealityPlan());

    Vector preps = new Vector ();
    NewPrepositionalPhrase newpp = ldmFactory.newPrepositionalPhrase();
    newpp.setPreposition(Constants.Preposition.FOR);
    newpp.setIndirectObject(asset);
    preps.add (newpp);

    newpp = ldmFactory.newPrepositionalPhrase();
    newpp.setPreposition(Constants.Preposition.FROM);

    NewGeolocLocation loc = ALPFactory.newGeolocLocation();
    loc.setGeolocCode("NNNN");
    loc.setLatitude (Latitude.newLatitude  (90.0d));
    loc.setLongitude(Longitude.newLongitude(0.0d));

    newpp.setIndirectObject(loc);
    preps.add (newpp);

    newpp = ldmFactory.newPrepositionalPhrase();
    newpp.setPreposition(Constants.Preposition.TO);
    newpp.setIndirectObject(loc);
    preps.add (newpp);

    newTask.setPrepositionalPhrases (preps.elements ());

    ScoringFunction startScoringFunc = 
      ScoringFunction.createStrictlyAtValue(new TimeAspectValue(AspectType.START_TIME, 
                                                                startDate));
    Preference startPreference = 
      ldmFactory.newPreference(AspectType.START_TIME, startScoringFunc);

    ScoringFunction endScoringFunc = 
      ScoringFunction.createStrictlyAtValue(new TimeAspectValue(AspectType.END_TIME, 
                                                                endDate));    
    Preference endPreference = 
      ldmFactory.newPreference(AspectType.END_TIME, endScoringFunc);

    Vector preferences = new Vector();
    preferences.addElement(startPreference);
    preferences.addElement(endPreference);
    newTask.setPreferences(preferences.elements());
    
    return newTask;
  }
}






