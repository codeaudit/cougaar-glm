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
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import org.cougaar.core.blackboard.AnonymousChangeReport;
import org.cougaar.core.blackboard.ChangeReport;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.service.LoggingService;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.NewTypeIdentificationPG;
import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.Expansion;
import org.cougaar.planning.ldm.plan.HasRelationships;
import org.cougaar.planning.ldm.plan.NewPrepositionalPhrase;
import org.cougaar.planning.ldm.plan.NewTask;
import org.cougaar.planning.ldm.plan.NewWorkflow;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Predictor;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Relationship;
import org.cougaar.planning.ldm.plan.RelationshipSchedule;
import org.cougaar.planning.ldm.plan.Role;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.planning.plugin.legacy.SimplePlugin;
import org.cougaar.planning.plugin.util.ExpanderHelper;
import org.cougaar.planning.plugin.util.PluginHelper;
import org.cougaar.util.ConfigFinder;
import org.cougaar.util.TimeSpan;
import org.cougaar.util.UnaryPredicate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * For now this plugin only subscribes to tasks from an XMLFile. 
 * The XMLFile to be read should be passed in as an argument.
 * <p>
 * Expected DTD is:
 * <pre>
 * &lt;!ELEMENT commands (command)*&gt;
 * &lt;!ELEMENT command (task, (expand|allocate))&gt;
 * &lt;!ELEMENT expand (task)*&gt;
 * &lt;!ELEMENT allocate (((cluster)|((role)*)),default?)&gt;
 * &lt;!ELEMENT default  (((cluster)|((role)*)),default?)&gt;
 * &lt;!ELEMENT cluster EMPTY&gt;
 * &lt;!ATTLIST cluster name CDATA #REQUIRED&gt;
 * &lt;!ELEMENT role EMPTY&gt;
 * &lt;!ATTLIST role name CDATA #REQUIRED&gt;
 * &lt;!ELEMENT task (phrase)?&gt;
 * &lt;!ATTLIST task verb CDATA #REQUIRED&gt;
 * &lt;!ELEMENT phrase EMPTY&gt;
 * &lt;!ATTLIST phrase
 *   for CDATA #REQUIRED
 *   oftype CDATA #REQUIRED&gt;
 * </pre>
 * <p>
 * Here is an example.  It first tries to allocate all Transport tasks 
 * to any StrategicTransportProvider. If none of the Organizations match, it then 
 * tries to allocate all the Transport tasks to any Superior Organization.
 * <p>
 * <pre>
 * &lt;commands&gt;
 *   &lt;command&gt;
 *     &lt;task verb="Transport"/&gt;
 *     &lt;allocate&gt;
 *       &lt;role name="StrategicTransportationProvider"/&gt;
 *       &lt;default&gt;
 *         &lt;role name="Superior"/&gt;
 *       &lt;default&gt;
 *     &lt;allocate&gt;
 *   &lt;command&gt;
 * &lt;commands&gt;
 * </pre>
 */
public class GenericTablePlugin extends SimplePlugin {

  protected CommandInfo[] allCommands;
  protected UnaryPredicate[] taskPreds;
  protected UnaryPredicate[] allocPreds;

  protected IncrementalSubscription organizationSub;
  protected IncrementalSubscription[] tasksSub;
  protected IncrementalSubscription[] allocationsSub;
        
  protected void setupSubscriptions() {
    Vector myParams = getParameters();
    String xmlFileString = (String)myParams.elementAt(0);
    initializeTables(xmlFileString);
    initializeSubscriptions();
  }

  protected void initializeTables(String xmlFileString) {
    readXMLFile(xmlFileString);

    taskPreds = createTaskPredicates(allCommands);
    allocPreds = createAllocationPredicates(taskPreds);

    tasksSub = 
      new IncrementalSubscription[taskPreds.length];
    allocationsSub = 
      new IncrementalSubscription[allocPreds.length];
  }

  protected void initializeSubscriptions() {
    makeSubscriptions();
  }

  protected void makeSubscriptions() {
    for (int i = 0; i < taskPreds.length; i++) {
      tasksSub[i] =
        (IncrementalSubscription)subscribe(taskPreds[i]);
    }

    for (int i = 0; i < allocPreds.length; i++) {
      allocationsSub[i] =
        (IncrementalSubscription)subscribe(allocPreds[i]);
    }

    organizationSub =
      (IncrementalSubscription)subscribe(newOrganizationPred());
  }

  /** used by subclass **/
  protected void cancelSubscriptions() {
    for (int i = 0; i < taskPreds.length; i++) {
      unsubscribe(tasksSub[i]);
    }

    for (int i = 0; i < allocPreds.length; i++) {
      unsubscribe(allocationsSub[i]);
    }

    unsubscribe(organizationSub);
  }

  /** rely upon load-time introspection to set these services - don't worry about revokation. */
  public final void setLoggingService (LoggingService logger) { this.logger = logger; }

  /**
   * Everybody needs a logger
   **/
  protected LoggingService logger;


  public synchronized void execute() {
    boolean checkUnallocated = false;
    if (organizationSub.hasChanged()) {
      Organization selfOrg = null;

      for (Iterator orgIter = organizationSub.getCollection().iterator(); 
	   orgIter.hasNext(); ) {
	Organization org = (Organization) orgIter.next();
	if (org.isSelf()) {
	  selfOrg = org;
	  break;
	}
      }
	
      if (selfOrg != null) {
	Collection changeReports = organizationSub.getChangeReports(selfOrg);
	if ((changeReports != AnonymousChangeReport.SET) &&
	    (changeReports != null)) {
	  for (Iterator reportIterator = changeReports.iterator();
	       reportIterator.hasNext();) {
	    ChangeReport report = (ChangeReport) reportIterator.next();
	    if (report instanceof RelationshipSchedule.RelationshipScheduleChangeReport) {
	      checkUnallocated = true;
	      break;
	    }
	  }
	  
	  if (checkUnallocated) {
	    for (int i = 0; i < tasksSub.length; i++) {
	      CommandInfo c = allCommands[i];
	      if (c.type_id == CommandInfo.TYPE_ALLOCATE) {
		if (logger.isDebugEnabled())
		  logger.debug(getAgentIdentifier() + " had RelatSched change on self org, so re-alloc all allocatable tasks.");
		allocate((AllocateCommandInfo) c, tasksSub[i], i);
	      }
	    }
	  }
	}
      }
    }

    for (int i = 0; i < tasksSub.length; i++) {
      if (tasksSub[i].hasChanged()) {
	CommandInfo c = allCommands[i];
	if (c.type_id == CommandInfo.TYPE_ALLOCATE) {
	  // If the above block already called allocate on all the tasksSubs,
	  // then don't call it again.
	  if (! checkUnallocated) {
	    if (logger.isDebugEnabled())
	      logger.debug(getAgentIdentifier() + " has changed task supposed to allocate - will alloc all added");
	    allocate((AllocateCommandInfo) c, tasksSub[i].getAddedCollection(), i);
	  }
	} else if (c.type_id == CommandInfo.TYPE_EXPAND) {
	  // expand
	  TaskInfo[] toTasks = ((ExpandCommandInfo)c).expandTasks;
	  for (Iterator iterator = tasksSub[i].getAddedCollection().iterator();
	       iterator.hasNext();) {
	    Task task = (Task) iterator.next();
	    // FIXME FIXME FIXME FIXME: Expansions won't
	    // Be in this sub on expansions!
	    // Need new sub to expansions of these tasks,
	    // And pass in that collection instead
	    //	    if (getTaskPlanElement(task, allocationsSub[i]) == null) {
	    if (task.getPlanElement() == null) {
	      for (int j = 0; j < toTasks.length; j++) {
		if (logger.isDebugEnabled())
		  logger.debug(getAgentIdentifier() + " had un-planned expandable Task added to sub - will expand: " + task);
		doExpansion(toTasks[j], task);
	      }
	    }
	  }
	} else {
	  if (logger.isDebugEnabled()) {
	    logger.debug("execute: unrecognized command type - " + c.type_id + 
			 " - command ignored.");
	  }
	}
      }
    }
      

    for (int i = 0; i < allocationsSub.length; i++) {
      if (allocationsSub[i].hasChanged()) {
	if (logger.isDebugEnabled())
	  logger.debug(getAgentIdentifier() + " had changed Alloc -- will update results.");
        updateAllocationResult(allocationsSub[i]);
      }
    }
  }

  protected void doAllocation(Organization org, Task task) {
    Predictor allocPred = org.getClusterPG().getPredictor();
    AllocationResult allocResult;
    if (allocPred != null)
      allocResult = allocPred.Predict(task, getDelegate());
    else
      allocResult = 
            PluginHelper.createEstimatedAllocationResult(
                task, getFactory(), 0.0, true);
    Allocation myalloc = getFactory().createAllocation(
            task.getPlan(), task, org, 
            allocResult, Role.BOGUS);
    if (logger.isDebugEnabled())
      logger.debug(getAgentIdentifier() + " adding alloc of " + task + " to " + org.getClusterPG().getMessageAddress());
    publishAdd(myalloc);
  }

  /** maybe memoize? **/
  protected Organization findCapableOrganization(AllocateCommandInfo allocC) {
    // find ourself first
    for (Iterator orgIter = organizationSub.getCollection().iterator(); 
         orgIter.hasNext(); ) {
      Organization org = (Organization) orgIter.next();
      if (org.isSelf()) {
        while (allocC != null) {
          // All HasRelationships having all roles
          Collection intersection = new ArrayList();
          boolean first = true;
          RelationshipSchedule schedule = org.getRelationshipSchedule();

          for (int i = 0; i < allocC.allocateRoles.length; i++) {
            Role role = Role.getRole(allocC.allocateRoles[i]);
            Collection orgCollection = 
              schedule.getMatchingRelationships(role,
                                                TimeSpan.MIN_VALUE,
                                                TimeSpan.MAX_VALUE);
            for (Iterator iter = orgCollection.iterator(); iter.hasNext(); ) {
              HasRelationships other = 
                schedule.getOther((Relationship) iter.next());

              if (other instanceof Organization) {
                if (first) {
                  intersection.add(other);
                } else {
                  if (!intersection.contains(other)) {
                    iter.remove();
                  }
                }
              }
            }
            first = false;
          }

          // If we found any matches return one, else try the default 
          if (intersection.size() > 0) {
            return (Organization) intersection.iterator().next();
          } else {
            allocC = allocC.defaultAlloc;
          }
        }
      }
    }
    return null;
  }

  /**
   * Ugly debug tool!
   */
  protected boolean DEBUGisCapable(Organization org,
      String name, String[] roles) {
    logger.debug("******************************************");
    logger.debug("DEBUG GenericTablePlugin.isCapable for Exceptions!!!");
    logger.debug("Cluster: "+getMessageAddress());
    logger.debug("Arguments:");
    logger.debug("  org: "+org);
    if (name != null)
      logger.debug("  name: "+name);
    if (roles != null)
      logger.debug("  roles["+roles.length+"]: "+roles);
    try {
      if (name != null) {
        // If name is specified, it alone qualifies!
        logger.debug("check name:");
        logger.debug("  org.getMessageAddress(): ");
        logger.debug("    "+org.getMessageAddress());
        logger.debug("  .getAddress(): ");
        logger.debug("    "+
          ((MessageAddress)org.getMessageAddress()).getAddress());
        logger.debug("  .equals("+name+"): ");
        String clustername = 
          ((MessageAddress)org.getMessageAddress()).getAddress();
        logger.debug("    "+
           clustername.equals(name));
        return clustername.equals(name);
      }

      boolean direct = true;
  
      if (roles != null) {
        RelationshipSchedule schedule = org.getRelationshipSchedule();
        for (int i=0; i<roles.length; i++) {
          String findRole = roles[i];
          logger.debug("  findRole["+i+
              " of "+roles.length+"]: "+findRole);

          Collection orgCollection = 
            schedule.getMatchingRelationships(Role.getRole(roles[i]).getConverse(),
                                              TimeSpan.MIN_VALUE,
                                              TimeSpan.MAX_VALUE);
          if (orgCollection.size() == 0) {
            return false;
          }

          logger.debug("Matching relationships: " + orgCollection);
        }
        // Good: Org has all the roles
      }

      logger.debug(" NO ERRORS???");
      logger.debug("******************************************");
      return true;
    } catch (Exception e) {
      logger.error("**** PREVIOUS CAUSED EXCEPTION "+e+" ****");
      e.printStackTrace();
      logger.error("******************************************");
      return false;
    }
  }

  protected void doExpansion (TaskInfo toTask, Task parentTask) {
    PlanningFactory f = getFactory();
    NewWorkflow wf = f.newWorkflow();
    wf.setParentTask(parentTask);
                    
    NewTask newTask = f.newTask();
    newTask.setParentTask(parentTask);
    newTask.setPlan(parentTask.getPlan());
    newTask.setDirectObject(parentTask.getDirectObject());
    newTask.setPreferences(parentTask.getPreferences());
    newTask.setVerb(Verb.get(toTask.verb));
                    
    NewPrepositionalPhrase npp = f.newPrepositionalPhrase();
    npp.setPreposition(Constants.Preposition.FOR);
    MessageAddress ci = MessageAddress.getMessageAddress(toTask.taskFor);
    npp.setIndirectObject(ci);

    NewPrepositionalPhrase npp1 = f.newPrepositionalPhrase();
    npp1.setPreposition(Constants.Preposition.OFTYPE);

    Asset myasset = f.createAsset("Asset");
    NewTypeIdentificationPG ti = 
      (NewTypeIdentificationPG) myasset.getTypeIdentificationPG();
    ti.setTypeIdentification(toTask.ofType);
                      
    npp1.setIndirectObject(myasset);

    Vector v = new Vector();
    v.addElement(npp);
    v.addElement(npp1);
  
    newTask.setPrepositionalPhrases(v.elements());
    newTask.setWorkflow(wf);
  
    AllocationResult ar =
      PluginHelper.createEstimatedAllocationResult(newTask, theLDMF, 0.0, true);
    Expansion exp =
      f.createExpansion(parentTask.getPlan(), newTask, wf, ar);
    if (logger.isDebugEnabled())
      logger.debug(getAgentIdentifier() + " adding exp of a task!");
    publishAdd(exp);
  }

  protected void updateAllocationResult(IncrementalSubscription sub) {
    Enumeration changedPEs = sub.getChangedList();
    while (changedPEs.hasMoreElements()) {
      PlanElement pe = (PlanElement)changedPEs.nextElement();
      AllocationResult repar;
      AllocationResult estar;
      if (((repar = pe.getReportedResult()) != null) &&
          ((estar = pe.getEstimatedResult()) != null) &&
          //!repar.isEqual(estar))  <--BROKEN in AllocatorHelper!
          (repar != estar)) {
        pe.setEstimatedResult(repar);

	// We always force the reported and estimated to be ==
	// But only publishChange it if they are not .isEqual
	// That is, it is more correct to only require .isEqual, but try to only minimally rock-the-boat for now
	if (repar.isEqual(estar)) {
	  // Turn this down from Warn to DEBUG after ensuring we dont see this with the ReceiveNotificationLP change in place.
	  if (logger.isDebugEnabled())
	    logger.debug(getAgentIdentifier() + " NOT pubChanging AllocResult due to RepAR.isEqual(EstAR) but not ==. For PE: " + pe + ". Rep was != Est. Rep: " + repar);
	} else {
	  publishChange(pe);
	}
      }
    }
  }

  protected void readXMLFile(String xmlFileString) {
    try {
      Document document = ConfigFinder.getInstance().parseXMLConfigFile(xmlFileString);
      Element root = document.getDocumentElement();
      allCommands = parseCommands(root);
    } catch (java.io.IOException ioe) {
      ioe.printStackTrace();
    }

  }

  protected CommandInfo[] parseCommands(Element root) {
    Element[] commandElems = findChildElements(root, "command");
    CommandInfo[] commands = new CommandInfo[commandElems.length];

    for (int i = 0; i < commandElems.length; i++) {
      Element command = commandElems[i];
      Element task = findChildElement(command, "task");
      TaskInfo sourceTask = parseTaskInfo(task);
      Element commandAction;
      if ((commandAction =
                  findChildElement(command, "allocate")) != null) {
        commands[i] = parseAllocate(commandAction, sourceTask);
      } else if ((commandAction =
           findChildElement(command, "expand")) != null) {
        commands[i] = parseExpand(commandAction, sourceTask);
      }
    }

    return commands;
  }

  protected TaskInfo parseTaskInfo(Element task) {
    String taskVerb = task.getAttribute("verb");
    Element taskPhrase = findChildElement(task, "phrase");
    if (taskPhrase != null) {
      String oftype = taskPhrase.getAttribute("oftype");
      if (oftype != null) {
        oftype = oftype.trim();
        if (oftype.length() <= 0)
          oftype = null;
      }
      String taskfor = taskPhrase.getAttribute("for");
      if (taskfor != null) {
        taskfor = taskfor.trim();
        if (taskfor.length() <= 0)
          taskfor = null;
      }
      return new TaskInfo(taskVerb, oftype, taskfor);
    } else {
      return new TaskInfo(taskVerb);
    }
  }

  protected AllocateCommandInfo parseAllocate(Node alloc, TaskInfo sourceTask) {
    AllocateCommandInfo retCom = new AllocateCommandInfo(sourceTask);
    Element clusterElem = findChildElement(alloc, "cluster");
    String clusterName;
    if ((clusterElem != null) &&
        ((clusterName = 
          clusterElem.getAttribute("name")) != null)) {
      // cluster name given
      retCom.allocateCluster = clusterName;
    } else {
      // get (role*)
      Element[] roleElems = findChildElements(alloc, "role");
      String[] allocateRoles = new String[roleElems.length];
      for (int j=0; j<roleElems.length; j++) {
        String r = roleElems[j].getAttribute("name");
        allocateRoles[j] = r;
      }
      retCom.allocateRoles = allocateRoles;
    }
    Element defaultElem = findChildElement(alloc, "default");
    if (defaultElem != null) {
      retCom.defaultAlloc = parseAllocate(defaultElem, sourceTask);
    }
    return retCom;
  }
    
  protected ExpandCommandInfo parseExpand(Node expand, TaskInfo sourceTask) {
    Element[] taskElements = findChildElements(expand, "task");
    TaskInfo[] expandTasks = new TaskInfo[taskElements.length];
    for (int j=0; j<taskElements.length; j++)
      expandTasks[j] = parseTaskInfo(taskElements[j]);
    ExpandCommandInfo retCom = new ExpandCommandInfo(sourceTask);
    retCom.expandTasks = expandTasks;
    return retCom;
  }

  protected UnaryPredicate[] createTaskPredicates(CommandInfo[] fromCommands) {
    int npreds = fromCommands.length;
    UnaryPredicate[] tPreds = new UnaryPredicate[npreds];

    for (int i = 0; i < npreds; i++) {
      CommandInfo c = fromCommands[i];
      TaskInfo st = c.sourceTask;
      if ((st.taskFor != null) || (st.ofType != null)) {
        tPreds[i] = newTaskPredicate(st.verb, st.taskFor, 
                                        st.ofType);
      } else {
        tPreds[i] = newTaskPredicate(st.verb);
      }
    }

    return tPreds;
  }

  protected UnaryPredicate[] createAllocationPredicates(
      UnaryPredicate[] tPreds) {
    int npreds = tPreds.length;
    UnaryPredicate[] aPreds = new UnaryPredicate[npreds];

    for (int i = 0; i < npreds; i++) {
      aPreds[i] = newAllocationPredicate(tPreds[i]);
    }

    return aPreds;
  }

  protected static UnaryPredicate newTaskPredicate(
      final String verb) {
    return new UnaryPredicate () {
      public boolean execute(Object o) {
        if (o instanceof Task) {
          Task t = (Task)o;
          if ((t.getVerb()).equals(verb)) {
            return true;
          }
        }
        return false;
      }
    };
  }

  protected UnaryPredicate newTaskPredicate(
      final String verb, final String tfor,
      final String oftype) {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof Task) {
          Task t = (Task) o;
          if ((t.getVerb()).equals(verb)) {
            if (checkForOfType(t, tfor, oftype)) {
              return true;
            }
          }
        }
        return false;
      }
    };
  }

  protected UnaryPredicate newAllocationPredicate(
      final UnaryPredicate taskPred) {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof Allocation)
          return taskPred.execute(((Allocation)o).getTask());
        return false;
      }
    };
  }

  protected Element findChildElement(Node parent, String childname) {
    NodeList nlist = parent.getChildNodes();
    int nlength = nlist.getLength();
    for (int i = 0; i < nlength; i++) {
      Node tnode = (Node)nlist.item(i);
      if ((tnode.getNodeType() == Node.ELEMENT_NODE) && 
          (tnode.getNodeName().equals(childname)))  {
        return (Element)tnode;
      }
    }
    return null;
  }

  protected Element[] findChildElements(Node parent, String childname) {
    NodeList nlist = parent.getChildNodes();
    int nlength = nlist.getLength();
    Element[] sparseChildren = new Element[nlength];
    int nChildren = 0;
    for (int i = 0; i < nlength; i++) {
      Node tnode = (Node)nlist.item(i);
      if ((tnode.getNodeType() == Node.ELEMENT_NODE) && 
          (tnode.getNodeName().equals(childname)))  {
        sparseChildren[nChildren++] = (Element)tnode;
      }
    }
    Element[] fullChildren = new Element[nChildren];
    System.arraycopy(sparseChildren,0,fullChildren,0,nChildren);
    return fullChildren;
  }

  protected boolean checkForOfType(Task t, String pfor, String oftype) {
    if (oftype != null) {
      if (!(ExpanderHelper.isOfType(t, Constants.Preposition.OFTYPE, oftype)))
        return false;
    }
    if (pfor != null) {
      PrepositionalPhrase p = 
        (PrepositionalPhrase)t.getPrepositionalPhrase(Constants.Preposition.FOR);
      if (p == null)
        return false;
      String hasFor;
      Object indObj = p.getIndirectObject();
      if (indObj instanceof MessageAddress)
        hasFor = ((MessageAddress)indObj).toString();
      else if (indObj instanceof Organization)
        hasFor = ((Organization)indObj).getMessageAddress().toString();
      else
        hasFor = null;
      if (!(pfor.equals(hasFor)))
        return false;
    }
    return true;
  }

  // Search a single collection of allocations for one that allocates the given task
  protected PlanElement getTaskPlanElement(Task t, Collection allocs) {
    PlanElement tpe = null;
    Collection bPEs = null;
    if (logger.isInfoEnabled()) {
      tpe = t.getPlanElement();
      final Task pt = t;
      bPEs = query(new UnaryPredicate() {
	  public boolean execute (Object o) {
	    if (o instanceof Allocation) {
	      Allocation a = (Allocation)o;
	      return (a.getTask() == pt);
	    }
	    return false;
	  }
	});
    }

    Iterator iter = allocs.iterator();
    while (iter.hasNext()) {
      PlanElement pe = (PlanElement)iter.next();
      if (pe.getTask().equals(t)) {
	if (logger.isInfoEnabled()) {
	  boolean bboardHasPE = true;
	  boolean bboardHasTPE = true;

	  if (bPEs != null) {
	    bboardHasPE = bPEs.contains(pe);
	    bboardHasTPE = bPEs.contains(tpe);
	    bPEs.remove(pe);
	    bPEs.remove(tpe);
	    if (! bPEs.isEmpty()) {
	      logger.info("Blackboard has " + bPEs.size() + " Allocs of the task other than the one on GTPI sub or pointed to by the task: " + t);

	      Iterator biter = bPEs.iterator();
	      while (biter.hasNext()) {
		Allocation ba = (Allocation)biter.next();
		logger.info("GTPI found BBoard Alloc " + ba.getUID() + ":" + ba);
	      }
	    }
	  }

	  if (tpe == null) {
	    logger.info("GTPI found Alloc on sub " + (bboardHasPE ? "also on blackboard" : "BUT NOT on blackboard") + " but task has NONE. Task: " + t + ", Sub Alloc: " + pe.getUID() + ":" + pe);
	  } else if (tpe != pe) {
	    boolean there = allocs.contains(tpe);
	    // Does removed list contain the task's pe?
	    boolean onRemoved = ((IncrementalSubscription)allocs).getRemovedCollection().contains(tpe);
	    
	    logger.info("GTPI found one Alloc on sub for task, but task lists another" + (there ? " that is ALSO on the sub" : " that is NOT on the sub") + (onRemoved ? ", and IS on the Sub's Removed list" : "") + ". My Sub's Alloc " + (bboardHasPE ? "IS" : "is NOT") + "on the blackboard. The Task's PE " + (bboardHasTPE ? "IS" : "is NOT") + "on the blackboard. Task: " + t + ", Tasks pe: " + tpe.getUID() + ":" + tpe + ", Sub's PE: " + pe.getUID() + ":" + pe);
	  } else {
	    // Task and sub match
	    if (!bboardHasPE)
	      logger.info("GTPI found Alloc on sub also on Task, but it's not on the BBoard! Task: " + t + ", PE: " + pe.getUID() + ":" + pe);
	  }

	  // Does removed list contain this pe?
	  boolean onRemoved = ((IncrementalSubscription)allocs).getRemovedCollection().contains(pe);
	  if (onRemoved) {
	    logger.info("GTPI found alloc of task on sub, but the alloc is also on my Removed list! " + (bboardHasPE ? "Blackboard has the alloc!" : "(Blackboard also doesn't have the alloc.)") + " Task: " + t + ", found PE: " + pe.getUID() + ":" + pe);
	  }
	} // end of Info logging
	return pe;
      } // end of block to handle found PE
    } // end of while loop over subscription

    // Found no Alloc on sub - will allocate the task

    if (logger.isInfoEnabled()) {
      if (tpe != null) {
	boolean tPBB = (bPEs != null && bPEs.contains(tpe));
	boolean onRemoved = ((IncrementalSubscription)allocs).getRemovedCollection().contains(tpe);
	logger.info("No Alloc on GTPIs sub for a task, but the task lists a PE" + (onRemoved ? " which is on my sub's removed list" : "") + (tPBB ? " which IS on the blackboard" : " which is NOT on the blackboard") + ". Task: " + t + ", Task's PE: " + tpe.getUID() + ":" + tpe);
      } 

      // Found no PE on sub
      if (bPEs != null && !bPEs.isEmpty()) {
	logger.info("No Alloc on GTPIs sub for a task, but blackboard has " + bPEs.size() + " allocs for the task: " + t);
	// Print out the PEs we did find!
	Iterator biter = bPEs.iterator();
	while (biter.hasNext()) {
	  Allocation ba = (Allocation)biter.next();
	  logger.info("GTPI found BBoard Alloc " + ba.getUID() + ":" + ba);
	}
      }

    } // end of info logging

    return null;
  }

  protected void allocate(AllocateCommandInfo commandInfo, Collection tasks, int subNum) {
    Organization capableOrg = null;
    for (Iterator iterator = tasks.iterator();
	 iterator.hasNext();) {
      Task task = (Task) iterator.next();
      if (getTaskPlanElement(task, allocationsSub[subNum]) == null) {
	if (capableOrg == null) {
	  capableOrg = 
	    findCapableOrganization(commandInfo);
	  if (capableOrg == null) {
	    // no capable org found!
	    if (logger.isDebugEnabled()) {
	      logger.debug("allocate: Unable to find provider for " + 
			   commandInfo.allocateRoles);
	    }
	    break;
	  }
	}
	doAllocation(capableOrg, task);
      } else {
	// sub has PE for this task
	
      }
    }
  }

  protected static UnaryPredicate newOrganizationPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        return (o instanceof Organization);
      }
    };
  }

  protected static class TaskInfo {
    public String verb;
    public String ofType;
    public String taskFor;

    TaskInfo(String verb) {this.verb = verb;}

    TaskInfo(String verb, String ofType, String taskFor) {
      this.verb = verb;
      this.ofType = ofType;
      if (taskFor != null) {
        if (!(taskFor.startsWith("<")))
          taskFor = "<"+taskFor;
        if (!(taskFor.endsWith(">")))
          taskFor = taskFor+">";
      }
      this.taskFor = taskFor;
    }

    public String toString() {
      String s = "verb: {"+verb+"}";
      if (ofType != null)
        s += " ofType: {"+ofType+"}";
      if (taskFor != null)
        s += " for: {"+taskFor+"}";
      return s;
    }
  }

  protected static class CommandInfo {
    public TaskInfo sourceTask;
    public int type_id;

    public static final int TYPE_NONE = -1;
    public static final int TYPE_ALLOCATE = 0;
    public static final int TYPE_EXPAND = 1;

    private CommandInfo() {
      type_id = TYPE_NONE;
    }

    public CommandInfo(TaskInfo sourceTask) {
      this();
      this.sourceTask = sourceTask;
    }

    public String toString() {
      String s = "Command\n";
      s += "  source: {"+sourceTask+"}\n";
      s += "  type: {";
      switch (type_id) {
        default:
        case TYPE_NONE:
          s += "none";
          break;
        case TYPE_ALLOCATE:
          s += "allocate";
          break;
        case TYPE_EXPAND:
          s += "expand";
          break;
      }
      s += "}\n";
      return s;
    }
  }

  protected static class ExpandCommandInfo extends CommandInfo {
    public TaskInfo[] expandTasks;

    public ExpandCommandInfo(TaskInfo sourceTask) {
      super(sourceTask);
      type_id = TYPE_EXPAND;
    }

    public String toString() {
      String s = super.toString();
      s += "  Expand\n";
      if (expandTasks != null) {
        s += "  expand["+expandTasks.length+"]: {\n";
      for (int i=0;i<expandTasks.length;i++)
        s += "    {"+expandTasks[i]+"}\n";
        s += "  }.\n";
      }
      return s;
    }
  }

  protected static class AllocateCommandInfo extends CommandInfo {
    public String allocateCluster;
    public String[] allocateRoles;
    public AllocateCommandInfo defaultAlloc;

    public AllocateCommandInfo(TaskInfo sourceTask) {
      super(sourceTask);
      type_id = TYPE_ALLOCATE;
    }

    public String toString() {
      String s = super.toString();
      s += "  Allocate\n";
      if (allocateCluster != null)
        s += "    cluster: {"+allocateCluster+"}.\n";
      else {
        if (allocateRoles != null) {
          s += "    roles["+allocateRoles.length+"]: {\n";
          for (int i=0;i<allocateRoles.length;i++)
            s += "    {"+allocateRoles[i]+"}\n";
          s += "  }.\n";
        }
      }
      if (defaultAlloc != null) 
        s += "DEFAULT: {\n"+defaultAlloc+"}\n";
      return s;
    }
  }

}
