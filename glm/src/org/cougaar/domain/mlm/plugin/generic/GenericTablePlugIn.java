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


import java.util.Collection;
import java.util.Enumeration;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.*;

import org.cougaar.domain.glm.Constants;
import org.cougaar.domain.glm.*;
import org.cougaar.domain.glm.plan.*;
import org.cougaar.domain.glm.asset.*;
import org.cougaar.domain.glm.oplan.*;

import org.cougaar.core.cluster.ClusterIdentifier;

import org.cougaar.core.cluster.IncrementalSubscription;

import org.cougaar.domain.planning.ldm.RootFactory;
import org.cougaar.domain.planning.ldm.plan.*;
import org.cougaar.domain.planning.ldm.asset.*;
import org.cougaar.core.plugin.SimplePlugIn;
import org.cougaar.core.plugin.util.*;

import org.cougaar.core.society.MessageAddress;

import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.TimeSpan;

import org.xml.sax.InputSource;
import org.w3c.dom.*;

import com.ibm.xml.parser.Parser;
import com.ibm.xml.parsers.*;

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
public class GenericTablePlugIn extends SimplePlugIn {

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

  public synchronized void execute() {
//     if (organizationSub.hasChanged()) {
//       AllocatorHelper.updateAllocationResult(organizationSub);
//     }

    for (int i = 0; i < tasksSub.length; i++) {
      if (!(tasksSub[i].hasChanged())) 
        continue;
      Enumeration eTasks = tasksSub[i].getAddedList();
      if (!(eTasks.hasMoreElements()))
        continue;
      CommandInfo c = allCommands[i];
      if (c.type_id == CommandInfo.TYPE_ALLOCATE) {
        // allocate
        Organization capableOrg = null;
        do {
          Task theTask = (Task) eTasks.nextElement();
          if (theTask.getPlanElement() == null) {
            if (capableOrg == null) {
              capableOrg = 
                findCapableOrganization((AllocateCommandInfo)c);
              if (capableOrg == null) {
                // no capable org found!
                break;
              }
            }
            doAllocation(capableOrg, theTask);
          }
        } while (eTasks.hasMoreElements());
      } else if (c.type_id == CommandInfo.TYPE_EXPAND) {
        // expand
        TaskInfo[] toTasks = ((ExpandCommandInfo)c).expandTasks;
        do {
          Task theTask = (Task) eTasks.nextElement();
          if (theTask.getPlanElement() == null) {
            for (int j = 0; j < toTasks.length; j++) {
              doExpansion(toTasks[j], theTask);
            }
          }
        } while (eTasks.hasMoreElements());
      } else {
        // no other commands!
      }
    }

    for (int i = 0; i < allocationsSub.length; i++) {
      if (allocationsSub[i].hasChanged()) {
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
            AllocatorHelper.createEstimatedAllocationResult(
                task, getFactory());
    Allocation myalloc = getFactory().createAllocation(
            task.getPlan(), task, org, 
            allocResult, Role.BOGUS);
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
    System.err.println("******************************************");
    System.err.println("DEBUG GenericTablePlugIn.isCapable for Exceptions!!!");
    System.err.println("Cluster: "+getClusterIdentifier());
    System.err.println("Arguments:");
    System.err.println("  org: "+org);
    if (name != null)
      System.err.println("  name: "+name);
    if (roles != null)
      System.err.println("  roles["+roles.length+"]: "+roles);
    try {
      if (name != null) {
        // If name is specified, it alone qualifies!
        System.err.println("check name:");
        System.err.println("  org.getClusterIdentifier(): ");
        System.err.println("    "+org.getClusterIdentifier());
        System.err.println("  .getAddress(): ");
        System.err.println("    "+
          ((MessageAddress)org.getClusterIdentifier()).getAddress());
        System.err.println("  .equals("+name+"): ");
        String clustername = 
          ((MessageAddress)org.getClusterIdentifier()).getAddress();
        System.err.println("    "+
           clustername.equals(name));
        return clustername.equals(name);
      }

      boolean direct = true;
  
      if (roles != null) {
        RelationshipSchedule schedule = org.getRelationshipSchedule();
        for (int i=0; i<roles.length; i++) {
          String findRole = roles[i];
          System.err.println("  findRole["+i+
              " of "+roles.length+"]: "+findRole);

          Collection orgCollection = 
            schedule.getMatchingRelationships(Role.getRole(roles[i]).getConverse(),
                                              TimeSpan.MIN_VALUE,
                                              TimeSpan.MAX_VALUE);
          if (orgCollection.size() == 0) {
            return false;
          }

          System.err.println("Matching relationships: " + orgCollection);
        }
        // Good: Org has all the roles
      }

      System.err.println(" NO ERRORS???");
      System.err.println("******************************************");
      return true;
    } catch (Exception e) {
      System.err.println("**** PREVIOUS CAUSED EXCEPTION "+e+" ****");
      e.printStackTrace();
      System.err.println("******************************************");
      return false;
    }
  }

  protected void doExpansion (TaskInfo toTask, Task parentTask) {
    RootFactory f = getFactory();
    NewWorkflow wf = f.newWorkflow();
    wf.setParentTask(parentTask);
                    
    NewTask newTask = f.newTask();
    newTask.setParentTask(parentTask);
    newTask.setPlan(parentTask.getPlan());
    newTask.setDirectObject(parentTask.getDirectObject());
    newTask.setPreferences(parentTask.getPreferences());
    newTask.setVerb(Verb.getVerb(toTask.verb));
                    
    NewPrepositionalPhrase npp = f.newPrepositionalPhrase();
    npp.setPreposition(Constants.Preposition.FOR);
    ClusterIdentifier ci = new ClusterIdentifier(toTask.taskFor);
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
      ExpanderHelper.createEstimatedAllocationResult(newTask, theLDMF);
    Expansion exp =
      f.createExpansion(parentTask.getPlan(), newTask, wf, ar);
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
        publishChange(pe);
      }
    }
  }

  protected void readXMLFile(String xmlFileString) {
    try {
      Document document = getCluster().getConfigFinder().parseXMLConfigFile(xmlFileString);
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
          if (t.getPlanElement() == null) {
            if ((t.getVerb()).equals(verb)) {
              if (checkForOfType(t, tfor, oftype)) {
                return true;
              }
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
      if (indObj instanceof ClusterIdentifier)
        hasFor = ((MessageAddress)indObj).toString();
      else if (indObj instanceof Organization)
        hasFor = ((Organization)indObj).getClusterIdentifier().toString();
      else
        hasFor = null;
      if (!(pfor.equals(hasFor)))
        return false;
    }
    return true;
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
