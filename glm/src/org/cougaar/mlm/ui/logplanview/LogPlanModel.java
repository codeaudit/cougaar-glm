/*
 * <copyright>
 *  Copyright 2001 BBNT Solutions, LLC
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
package org.cougaar.mlm.ui.logplanview;

import org.cougaar.*;
import org.cougaar.core.agent.*;
import org.cougaar.core.domain.*;
import org.cougaar.core.blackboard.*;
import org.cougaar.core.mts.Message;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.plugin.*;
import org.cougaar.core.domain.*;
import org.cougaar.planning.ldm.asset.*;
import org.cougaar.planning.ldm.measure.*;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.util.*;
import org.cougaar.lib.util.*;

import org.cougaar.core.util.UID;

import org.cougaar.glm.*;
import org.cougaar.glm.ldm.*;
import org.cougaar.glm.ldm.plan.*;
import org.cougaar.glm.ldm.oplan.*;
import org.cougaar.glm.ldm.asset.*;
import org.cougaar.glm.plugins.projection.*;
import org.cougaar.glm.plugins.TaskUtils;

import java.util.*;
import java.text.*;

import javax.swing.*;
import javax.swing.tree.*;

/**
 * Storage class (model) of LogPlan content for LogPlan Viewer UI.
 */
public class LogPlanModel extends DefaultTreeModel {
  LogPlanModelServerPlugIn logPlanModelServerPlugIn = null;
  Vector skipVerbs = new Vector();

  boolean             displayDates = true;
  boolean        displayTaskDetail = true;
  boolean    displayAssetTransfers = true;
  boolean            displayTaskID = false;
  boolean displaySourceDestination = true;

  DefaultMutableTreeNode theRootNode = null;

  Vector removedTaskList = new Vector();

  Hashtable elementIdToNodeMap = new Hashtable();

  public LogPlanModel( LogPlanModelServerPlugIn logPlanModelServerPlugIn, String clusterName, boolean didRehydrate, Enumeration skipVerbsEnum) {
    super( new DefaultMutableTreeNode( "LogPlan", true));
    this.logPlanModelServerPlugIn = logPlanModelServerPlugIn;
    theRootNode = (DefaultMutableTreeNode)getRoot();

    while ( skipVerbsEnum.hasMoreElements()) {
      skipVerbs.add( skipVerbsEnum.nextElement());
    }

    if ( didRehydrate) {
      rehydrate();
    }
  }

  /**
   * Inner class of LogPlanModel. Representation of element in the tree of LogPlan content.
   */
  class LogPlanItem {
    Object object;

    LogPlanItem( Object object) {
      this.object = object;
    }

    public Object getObject() { return object; }

    public boolean equals( Object object) {
      if ( this.object == object)
        return true;
      else
        return false;
    }

    public String toString() {
      String retVal = "";
      if ( object instanceof String) {
        retVal = (String)object;
      }
      if ( object instanceof PlanElement) {
        PlanElement planElement = (PlanElement)object;
        if ( planElement instanceof Allocation) {
          Allocation allocation = (Allocation)planElement;
          Asset asset = allocation.getAsset();

          if ( asset instanceof Organization) {
            retVal = "[Allocation Results]";
          } else if ( asset instanceof AssetGroup) {
            retVal = "[Allocated Item] Asset Group";
          } else {
            String assetName = asset.getTypeIdentificationPG().getNomenclature();
            String assetID = asset.getItemIdentificationPG().getItemIdentification();
            if ( !(asset instanceof Person)) {
              retVal = "[Allocated Item] " + assetName;
            } else {
              String nameOfPerson = asset.getItemIdentificationPG().getNomenclature();
              String jobDescription  = null;
              String afsc            = null;
              Person person = (Person)asset;
              PersonPG personPG = (PersonPG)person.getPersonPG();
              Collection skills = personPG.getSkills();
              Iterator iter = skills.iterator();
              while ( iter.hasNext()) {
                Skill skill = (Skill)iter.next();
                if ( skill instanceof AircrewSkill) {
                  AircrewSkill aircrewSkill = (AircrewSkill) skill;
                  jobDescription  = aircrewSkill.getPosition();
                  afsc            = aircrewSkill.getQualification();
                  break;
                }
              }
              retVal = "[Allocated Person] " + jobDescription + ":" + afsc + " <" + nameOfPerson + ">";
            }
            if ( assetID != null) {
              retVal += " <" + assetID + ">";
            }
          }
        } else {
          if ( planElement instanceof Disposition) {
            AllocationResult allocationResult = planElement.getEstimatedResult();
            if ( allocationResult.isSuccess() == false) {
              retVal = "[Failed Allocation]";
            }
          }
        }
      } else if ( object instanceof Task) {
        Task task = (Task)object;
        if ( displaySourceDestination) {
          if ( task.getParentTaskUID() == null) {
            retVal += "(ROOT) ";
          } else {
            String source       = task.getSource().getAddress();
            String destination  = task.getDestination().getAddress();
            if ( displayTaskID) {
              retVal += "(" + task.getUID() + ")";
            }
            if ( source.equals( destination) == false) {
              retVal += "<" + source + " to " + destination + ">";
            }
          }
        }
        PlanElement planElement = task.getPlanElement();
        if ( planElement != null) {
          Asset asset = null;
          if ( planElement instanceof AssetTransfer && displayAssetTransfers) {
            retVal += "(AssetTransfer): ";
            AssetTransfer assetTransfer = (AssetTransfer)planElement;
            retVal += getDescription( assetTransfer.getAsset());
            retVal += " to " + getDescription( assetTransfer.getAssignee());
            Object directObject = task.getDirectObject();
            if ( directObject != null) {
              retVal += " " + getDescription( directObject);
            }
            PrepositionalPhrase pPhrase = UTILPrepPhrase.getPrepNamed( task, Constants.Preposition.FOR);
            if ( pPhrase != null) {
              retVal += " For " + getDescription( pPhrase.getIndirectObject());
            }
          } else {
            if ( planElement instanceof Allocation) {
              asset = ((Allocation)planElement).getAsset();
              if ( asset instanceof Organization) {
                if ( task.getParentTaskUID() != null) {
                  retVal += "(Allocated to " + getDescription( asset) + ")";
                }
              }
            }
            retVal += task.getVerb().toString();

            if ( displayDates) {
              Date date = new Date( (long)(task.getPreferredValue( AspectType.START_TIME)));
              Calendar calendar = new GregorianCalendar();
              calendar.setTime( date);
              int month = calendar.get( Calendar.MONTH);
              int day = calendar.get( Calendar.DAY_OF_MONTH);
              int year = calendar.get( Calendar.YEAR);
              int hour = calendar.get( Calendar.HOUR_OF_DAY);
              int minute = calendar.get( Calendar.MINUTE);
              String am_pm = (hour < 12) ? "am" : "pm";
              retVal += "[";
              if ( hour > 12) {
                hour -= 12;
              }
              retVal += month + "/" + day + "/"+ year + " ";
              if ( hour <= 9) {
                retVal += "0";
              }
              retVal += hour + ":";
              if ( minute <= 9) {
                retVal += "0";
              }
              retVal += minute + am_pm;
              retVal += " thru ";
              date = new Date( (long)(task.getPreferredValue( AspectType.END_TIME)));
              calendar = new GregorianCalendar();
              calendar.setTime( date);
              month = calendar.get( Calendar.MONTH);
              day = calendar.get( Calendar.DAY_OF_MONTH);
              year = calendar.get( Calendar.YEAR);
              hour = calendar.get( Calendar.HOUR_OF_DAY);
              minute = calendar.get( Calendar.MINUTE);
              am_pm = (hour < 12) ? "am" : "pm";
              if ( hour > 12) {
                hour -= 12;
              }
              retVal += month + "/" + day + "/"+ year + " ";
              if ( hour <= 9) {
                retVal += "0";
              }
              retVal += hour + ":";
              if ( minute <= 9) {
                retVal += "0";
              }
              retVal += minute + am_pm + "]";
            }

            Preference preference = task.getPreference( AspectType.QUANTITY);
            if ( preference != null) {
              ScoringFunction sf = preference.getScoringFunction();
              double score = ((AspectScorePoint)sf.getBest()).getValue();
              if ( score >= 1) {
                retVal += " [qty=" + (int)score;
              } else {
                retVal += " [qty=" + score;
              }
              AspectScorePoint aspectScorePoint = sf.getBest();
              AspectValue aspectValue = aspectScorePoint.getAspectValue();
              if ( aspectValue instanceof TypedQuantityAspectValue) {
                TypedQuantityAspectValue typedQuantityAV = (TypedQuantityAspectValue)aspectValue;
                String assetName = typedQuantityAV.getAsset().getTypeIdentificationPG().getNomenclature();
                retVal += "," + assetName;
              }
              retVal += "]";
            }

            Object directObject = task.getDirectObject();
            if ( directObject != null)
              retVal += " " + getDescription( directObject);
            if ( displayTaskDetail) {
              Enumeration pPhrases = task.getPrepositionalPhrases();
              boolean first = true;
              while ( pPhrases.hasMoreElements()) {
                if ( !first) {
                  retVal += ",";
                } else {
                  first = false;
                }
                PrepositionalPhrase pPhrase = (PrepositionalPhrase)pPhrases.nextElement();
                retVal += " " + pPhrase.getPreposition() + " " + getDescription( pPhrase.getIndirectObject());
              }
            }
          }
        } else {
          retVal += task.getVerb().toString();

          Preference preference = task.getPreference( AspectType.QUANTITY);
          if ( preference != null) {
            ScoringFunction sf = preference.getScoringFunction();
            double score = ((AspectScorePoint)sf.getBest()).getValue();
            if ( score >= 1) {
              retVal += " [qty=" + (int)score;
            } else {
              retVal += " [qty=" + score;
            }
            AspectScorePoint aspectScorePoint = sf.getBest();
            AspectValue aspectValue = aspectScorePoint.getAspectValue();
            if ( aspectValue instanceof TypedQuantityAspectValue) {
              TypedQuantityAspectValue typedQuantityAV = (TypedQuantityAspectValue)aspectValue;
              String assetName = typedQuantityAV.getAsset().getTypeIdentificationPG().getNomenclature();
              retVal += "," + assetName;
            }
            retVal += "]";
          }

          Object directObject = task.getDirectObject();
          if ( directObject != null) {
            retVal += " " + getDescription( directObject);
          }
          if ( displayTaskDetail) {
            Enumeration pPhrases = task.getPrepositionalPhrases();
            boolean first = true;
            while ( pPhrases.hasMoreElements()) {
              if ( !first) {
                retVal += ",";
              } else {
                first = false;
              }
              PrepositionalPhrase pPhrase = (PrepositionalPhrase)pPhrases.nextElement();
              retVal += " " + pPhrase.getPreposition() + " " + getDescription( pPhrase.getIndirectObject());
            }
          }
        }
      }
      return retVal;
    }
  }

  /**
   * Format the String representing a PenaltyValue's value.
   *
   * @param value the value to be represented as a String
   * @return the value of a PenaltyValue as a String.
   */
  public String formatPenaltyValue( double value) {
    String retval = Double.toString( value);
    int pointIdx = retval.indexOf( ".");
    if ( pointIdx != -1) {
      int strLength = retval.length();

      if ( pointIdx <= strLength - 4) {
        retval = retval.substring( 0, pointIdx + 4);
      }
    }

    return retval;
  }

  /**
   * Determine if a tree node is present in the tree.
   *
   * @param node that may or may not already be in the model's tree
   * @return true/false for whether or not the input node is in the
   * tree by establishing that the node has parentage up to the theRootNode of the tree.
   */
  private boolean nodeIsInTree( DefaultMutableTreeNode node) {
    DefaultMutableTreeNode tmpNode = node;
    while ( tmpNode != null) {
      if ( tmpNode == theRootNode)
        return true;
      tmpNode = (DefaultMutableTreeNode)tmpNode.getParent();
    }
    return false;
  }

  /**
   * Remove a node from the model's tree associated with a task.
   *
   * @param task that should be removed from the model's tree
   */
  public void handleRemovedTask( Task task) {
    removedTaskList.addElement( task);
    Object object = elementIdToNodeMap.remove( task.getUID());
    if ( object instanceof DefaultMutableTreeNode) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)object;
      removeNodeFromParent( node);
    }
  }

  /**
   * Create and add a node to the model's tree associated with a task.
   *
   * @param task that should have a node created and added to the model's tree
   */
  public void handleAddedTask( Task task) {
    Object object = null;

    String verbStr = task.getVerb().toString();
    // Subclass may have indicated to skip the display of certain verbs
    if ( skipVerbs.contains( verbStr)) {
      return;
    }

    UID parentTaskUid = task.getParentTaskUID();

    // Handle Root Tasks and InputTasks.
    if ( parentTaskUid == null || task.getWorkflow() == null) {
      // If this task isn't already in the tree, then put it in.
      if ( elementIdToNodeMap.get( task.getUID()) == null) {
        LogPlanItem newLogPlanItem = new LogPlanItem( task);
        DefaultMutableTreeNode taskChild = new DefaultMutableTreeNode( newLogPlanItem, true);
        insertNodeInto( taskChild, theRootNode, theRootNode.getChildCount());
        elementIdToNodeMap.put( task.getUID(), taskChild);
      }
    }

    // If there is a parent task, put this task in as a child
    if ( parentTaskUid != null) {
      // There are two situations where the parent tasks' node wasn't found:
      // 1. The parent task's node was removed before the this task arrived.
      // 2. This task arrived before the ParentTask.
      // In either case, do nothing
      object = elementIdToNodeMap.get( parentTaskUid);
      if ( object != null) {
        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)object;
        // make sure the parent node is still part of the tree
        if ( nodeIsInTree( parentNode)) {
          // If this task isn't already in the tree, then put it in.
          if ( elementIdToNodeMap.get( task.getUID()) == null) {
              LogPlanItem newLogPlanItem = new LogPlanItem( task);
            DefaultMutableTreeNode taskChild = new DefaultMutableTreeNode( newLogPlanItem, true);
            insertNodeInto( taskChild, parentNode, parentNode.getChildCount());
            elementIdToNodeMap.put( task.getUID(), taskChild);
          }
        }
      }
    }

    // If this task has a PlanElement that is an Expansion,
    // put all of the Expansion's subtasks in the tree.
    PlanElement planElement = task.getPlanElement();
    if ( planElement != null && planElement instanceof Expansion) {
      Expansion expansion = (Expansion)planElement;
      Workflow wf = expansion.getWorkflow();
      Enumeration subtasks = wf.getTasks();
      while ( subtasks.hasMoreElements()) {
        Task subtask = (Task)subtasks.nextElement();
        if ( elementIdToNodeMap.get( subtask.getUID()) == null) {
          handleAddedTask( subtask);
        }
      }
    }

    // If this task has an asset allocated for it, put the allocation in the tree.
    if ( planElement != null && planElement instanceof Allocation) {
      handleAddedAllocations( (Allocation)planElement);
    }
  }

  /**
   * Create and add a node to the model's tree associated with an Allocation.
   *
   * @param allocation that should have a node created and added to the model's tree
   */
  public void handleAddedAllocations( Allocation allocation) {
    // If this allocation is already in the tree, just return
    DefaultMutableTreeNode tmpNode = (DefaultMutableTreeNode)elementIdToNodeMap.get( allocation.getUID());
    if ( tmpNode != null && nodeIsInTree( tmpNode)) {
      return;
    }

    Task task = allocation.getTask();

    // Subclass may have indicated to skip the display of certain verbs
    String verbStr = task.getVerb().toString();
    if ( skipVerbs.contains( verbStr)) {
      return;
    }

    DefaultMutableTreeNode taskNode = (DefaultMutableTreeNode)elementIdToNodeMap.get( task.getUID());
    if ( nodeIsInTree( taskNode)) {
      LogPlanItem newLogPlanItem = null;
      DefaultMutableTreeNode allocationNode = null;
      DefaultMutableTreeNode allocationChild = null;
      AllocationResult allocationResult = null;
      ScheduleElement scheduleElement = null;

      double cost = 0.0;
      String costString = "";
      double dateValue = 0.0;
      Date date = null;
      String dateString = "";
      double quantity = 0.0;
      String quantityString = "";
      String startDateStr = "";
      String endDateStr = "";

      Asset asset = allocation.getAsset();

      newLogPlanItem = new LogPlanItem( allocation);
      allocationNode = new DefaultMutableTreeNode( newLogPlanItem, true);
      // add the allocation/allocationNode pair to the HashMap
      elementIdToNodeMap.put( allocation.getUID(), allocationNode);

      if ( !(asset instanceof Organization)) {
        // insert the allocationNode as a child node of its associated task
        insertNodeInto( allocationNode, taskNode, taskNode.getChildCount());

        String assetName = asset.getTypeIdentificationPG().getNomenclature();
        allocationResult = allocation.getEstimatedResult();

	if (allocationResult != null) {
	  newLogPlanItem = new LogPlanItem( "Estimated Values (Success: " + allocationResult.isSuccess() + ")");
	  allocationChild = new DefaultMutableTreeNode( newLogPlanItem, true);
	  insertNodeInto( allocationChild, allocationNode, allocationNode.getChildCount());

	  if ( allocationResult.isDefined( AspectType.COST)) {
	    cost = allocationResult.getValue( AspectType.COST);
	    costString = formatPenaltyValue( cost);
	    newLogPlanItem = new LogPlanItem( "     Cost: " + costString);
	    allocationChild = new DefaultMutableTreeNode( newLogPlanItem, true);
	    insertNodeInto( allocationChild, allocationNode, allocationNode.getChildCount());
	  }
	  if ( allocationResult.isDefined( AspectType.QUANTITY)) {
	    quantity = allocationResult.getValue( AspectType.QUANTITY);
	    quantityString = formatPenaltyValue( quantity);
	    newLogPlanItem = new LogPlanItem( "     Qty:" + " " + quantityString);
	    allocationChild = new DefaultMutableTreeNode( newLogPlanItem, true);
	    insertNodeInto( allocationChild, allocationNode, allocationNode.getChildCount());
	  }
	  if ( allocationResult.isDefined( AspectType.START_TIME)) {
	    dateValue = allocationResult.getValue( AspectType.START_TIME);
	    date = new Date( (long)dateValue);
	    dateString = date.toString();
	    newLogPlanItem = new LogPlanItem( "     Start: " + dateString);
	    allocationChild = new DefaultMutableTreeNode( newLogPlanItem, true);
	    insertNodeInto( allocationChild, allocationNode, allocationNode.getChildCount());
	  }
	  if ( allocationResult.isDefined( AspectType.END_TIME)) {
	    dateValue = allocationResult.getValue( AspectType.END_TIME);
	    date = new Date( (long)dateValue);
	    dateString = date.toString();
	    newLogPlanItem = new LogPlanItem( "     End: " + dateString);
	    allocationChild = new DefaultMutableTreeNode( newLogPlanItem, true);
	    insertNodeInto( allocationChild, allocationNode, allocationNode.getChildCount());
	  }
	}
      } else {
        // insert the allocationNode as a child node of its associated task
        insertNodeInto( allocationNode, taskNode, 0);
        allocationResult = allocation.getEstimatedResult();
        if ( allocationResult != null) {

          newLogPlanItem = new LogPlanItem( "Estimated Values (Success: " + allocationResult.isSuccess() + ")");
          allocationChild = new DefaultMutableTreeNode( newLogPlanItem, true);
          insertNodeInto( allocationChild, allocationNode, allocationNode.getChildCount());

          if ( allocationResult.isDefined( AspectType.COST)) {
            cost = allocationResult.getValue( AspectType.COST);
            costString = formatPenaltyValue( cost);
            newLogPlanItem = new LogPlanItem( "     Cost:" + " " + costString);
            allocationChild = new DefaultMutableTreeNode( newLogPlanItem, true);
            insertNodeInto( allocationChild, allocationNode, allocationNode.getChildCount());
          }
          if ( allocationResult.isDefined( AspectType.QUANTITY)) {
            quantity = allocationResult.getValue( AspectType.QUANTITY);
            quantityString = formatPenaltyValue( quantity);
            newLogPlanItem = new LogPlanItem( "     Qty:" + " " + quantityString);
            allocationChild = new DefaultMutableTreeNode( newLogPlanItem, true);
            insertNodeInto( allocationChild, allocationNode, allocationNode.getChildCount());
          }
          if ( allocationResult.isDefined( AspectType.START_TIME)) {
            dateValue = allocationResult.getValue( AspectType.START_TIME);
            date = new Date( (long)dateValue);
            dateString = date.toString();
            newLogPlanItem = new LogPlanItem( "     Start: " + dateString);
            allocationChild = new DefaultMutableTreeNode( newLogPlanItem, true);
            insertNodeInto( allocationChild, allocationNode, allocationNode.getChildCount());
          }
          if ( allocationResult.isDefined( AspectType.END_TIME)) {
            dateValue = allocationResult.getValue( AspectType.END_TIME);
            date = new Date( (long)dateValue);
            dateString = date.toString();
            newLogPlanItem = new LogPlanItem( "     End: " + dateString);
            allocationChild = new DefaultMutableTreeNode( newLogPlanItem, true);
            insertNodeInto( allocationChild, allocationNode, allocationNode.getChildCount());
          }
        }
        costString = "";
        dateString = "";
        quantityString = "";
        startDateStr = "";
        endDateStr = "";
        allocationResult = allocation.getReportedResult();

        if ( allocationResult != null) {

          newLogPlanItem = new LogPlanItem( "Reported Values (Success: " + allocationResult.isSuccess() + ")");
          allocationChild = new DefaultMutableTreeNode( newLogPlanItem, true);
          insertNodeInto( allocationChild, allocationNode, allocationNode.getChildCount());

          if ( allocationResult.isDefined( AspectType.COST)) {
            cost = allocationResult.getValue( AspectType.COST);
            costString = formatPenaltyValue( cost);
            newLogPlanItem = new LogPlanItem( "     Cost:" + " " + costString);
            allocationChild = new DefaultMutableTreeNode( newLogPlanItem, true);
            insertNodeInto( allocationChild, allocationNode, allocationNode.getChildCount());
          }
          if ( allocationResult.isDefined( AspectType.QUANTITY)) {
            quantity = allocationResult.getValue( AspectType.QUANTITY);
            quantityString = formatPenaltyValue( quantity);
            newLogPlanItem = new LogPlanItem( "     Qty:" + " " + quantityString);
            allocationChild = new DefaultMutableTreeNode( newLogPlanItem, true);
            insertNodeInto( allocationChild, allocationNode, allocationNode.getChildCount());
          }

          if ( allocationResult.isDefined( AspectType.START_TIME)) {
            dateValue = allocationResult.getValue( AspectType.START_TIME);
            date = new Date( (long)dateValue);
            dateString = date.toString();
            newLogPlanItem = new LogPlanItem( "     Start: " + dateString);
            allocationChild = new DefaultMutableTreeNode( newLogPlanItem, true);
            insertNodeInto( allocationChild, allocationNode, allocationNode.getChildCount());
          }
          if ( allocationResult.isDefined( AspectType.END_TIME)) {
            dateValue = allocationResult.getValue( AspectType.END_TIME);
            date = new Date( (long)dateValue);
            dateString = date.toString();
            newLogPlanItem = new LogPlanItem( "     End: " + dateString);
            allocationChild = new DefaultMutableTreeNode( newLogPlanItem, true);
            insertNodeInto( allocationChild, allocationNode, allocationNode.getChildCount());
          }
        }
      }
    }
  }

  /**
   * Create and add a node to the model's tree associated with a non-Allocation Disposition.
   *
   * @param disposition that should have a node created and added to the model's tree
   */
  public void handleAddedNonAllocationDispositions( Disposition disposition) {
    Task task = disposition.getTask();

    // Subclass may have indicated to skip the display of certain verbs
    String verbStr = task.getVerb().toString();
    if ( skipVerbs.contains( verbStr)) {
      return;
    }

    DefaultMutableTreeNode taskNode = (DefaultMutableTreeNode)elementIdToNodeMap.get( task.getUID());
    if ( nodeIsInTree( taskNode)) {
      LogPlanItem newLogPlanItem = null;
      DefaultMutableTreeNode dispositionNode = null;
      AllocationResult allocationResult = disposition.getEstimatedResult();

      // really only handling failed allocations
      if ( allocationResult.isSuccess() == false) {
        newLogPlanItem = new LogPlanItem( disposition);
        dispositionNode = new DefaultMutableTreeNode( newLogPlanItem, true);
        insertNodeInto( dispositionNode, taskNode, taskNode.getChildCount());
      }
    }
  }

  /**
   * Create and add a node to the model's tree associated with an Allocation.
   * This method handles changed allocations, so the original allocation node is removed
   * then a node representing the changed allocation is created, then added.
   *
   * @param allocation that should have a node created and added to the model's tree
   */
  public void handleChangedAllocations( Allocation allocation) {
    // remove the one that's in the HashMap and in the tree,
    // then add the changed one as if it were new.
    Object object = elementIdToNodeMap.remove( allocation.getUID());
    if ( object instanceof DefaultMutableTreeNode) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)object;
      DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)node.getParent();
      removeNodeFromParent( node);
      if ( nodeIsInTree( parentNode)) {
        handleAddedAllocations( allocation);
      }
    }
  }

  /**
   * Remove a node from the model's tree associated with an Allocation.
   *
   * @param allocation that should be removed from the model's tree
   */
  public void handleRemovedAllocations( Allocation allocation) {
    // remove the one that's in the HashMap and in the tree.
    Object object = elementIdToNodeMap.remove( allocation.getUID());
    if ( object instanceof DefaultMutableTreeNode) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)object;
      removeNodeFromParent( node);
    }
  }

  /**
   * Create a description String representation of a LogPlan object.
   *
   * @param o the object needing a String representation.
   * @return representation of LogPlan object as a displayable String.
   */
  public String getDescription( Object o) {
    String retVal = "";
    try {
      if ( o == null) {
      } else if ( o instanceof String) {
        retVal = "\"" + (String)o + "\"";
      } else if ( o instanceof AssetGroup) {
        retVal = "Manifest";
      } else if ( o instanceof GeolocLocation) {
        GeolocLocation geolocLocation = (GeolocLocation)o;
        retVal = geolocLocation.getName();
      } else if ( o instanceof Schedule) {
        Schedule schedule = (Schedule)o;
        try {
          //retVal = schedule.getSimpleScheduleStartDate().toString();
          Date startDate = new Date( schedule.getStartTime());
          retVal = startDate.toString();
        } catch ( Exception e) {
          boolean firstItem = true;
          Enumeration enum = schedule.getAllScheduleElements();
          while ( enum.hasMoreElements()) {
            Object obj = enum.nextElement();
            if ( obj instanceof ItineraryElement) {
              if ( firstItem) {
                retVal = "[";
                firstItem = false;
              } else {
                retVal += ", ";
              }
              ItineraryElement itineraryElement = (ItineraryElement)obj;
              GeolocLocation geoloc = (GeolocLocation)itineraryElement.getStartLocation();
              retVal += geoloc.getName() + ":";
              retVal += itineraryElement.getStartDate().toString() + "-";
              retVal += itineraryElement.getEndDate().toString();
              if ( !(enum.hasMoreElements())) {
                retVal += "]";
              }
            }
          }
        }
      } else if ( o instanceof AggregateAsset) {
        AggregateAsset aggregateAsset = (AggregateAsset)o;
        Asset asset = aggregateAsset.getAsset();

        long aggregateCount = aggregateAsset.getQuantity();
        retVal = " [" + Long.toString( aggregateCount) + "] ";
        retVal += getDescription( asset);
      } else if ( o instanceof PhysicalAsset || o instanceof AbstractAsset) {
        Asset asset = (Asset)o;
        retVal = "'" + asset.getTypeIdentificationPG().getNomenclature() + "'";
        if ( !( asset instanceof FixedWingAircraftWeapon)) {
          retVal += "=" + asset.getTypeIdentificationPG().getTypeIdentification();
        } else {
          ItemIdentificationPG itemID = asset.getItemIdentificationPG();
          if ( itemID != null && itemID.getItemIdentification() != null) {
            retVal += ":" + itemID.getItemIdentification();
          }
        }
      } else if ( o instanceof Person) {
        Asset asset = (Asset)o;
        String jobDescription  = null;
        String afsc            = null;
        Person person = (Person)asset;
        try {
          PersonPG personPG = (PersonPG)person.getPersonPG();
          Collection skills = personPG.getSkills();
          Iterator iter = skills.iterator();
          while ( iter.hasNext()) {
            Skill skill = (Skill)iter.next();
            if ( skill instanceof AircrewSkill) {
              AircrewSkill aircrewSkill = (AircrewSkill) skill;
              jobDescription  = aircrewSkill.getPosition();
              afsc            = aircrewSkill.getQualification();
              break;
            }
          }
          retVal = jobDescription + ":" + afsc;
        } catch ( Exception Ex) {
          retVal = person.getTypeIdentificationPG().getTypeIdentification();
        }
      } else if ( o instanceof Organization) {
        Organization org = (Organization)o;
        retVal = org.getItemIdentificationPG().getNomenclature();
      } else if ( o instanceof Task) {
        Task task = (Task)o;
        retVal = task.getUID().toString();
      } else if ( o instanceof ClusterIdentifier) {
        ClusterIdentifier cid = (ClusterIdentifier)o;
        String cidString = cid.getAddress();
        if ( retVal == "")
          retVal = cidString;
      } else if ( o instanceof Vector) {
        boolean firstItem = true;
        retVal = "[";
        Enumeration enum = ((Vector)o).elements();
        while ( enum.hasMoreElements()) {
          if ( !firstItem) {
            retVal += ", ";
          }
          Object item = enum.nextElement();
          if ( item instanceof ItineraryElement) {
            ItineraryElement itineraryElement = (ItineraryElement)item;
            GeolocLocation geoloc = (GeolocLocation)itineraryElement.getStartLocation();
            retVal += geoloc.getName() + ":";
            retVal += itineraryElement.getStartDate().toString() + "-";
            retVal += itineraryElement.getEndDate().toString();
          } else if ( item instanceof Organization) {
            Organization org = (Organization)item;
            retVal += org.getItemIdentificationPG().getNomenclature();
          } else {
            retVal += item.toString();
          }
          firstItem = false;
        }
        retVal += "]";
      } else if ( o instanceof ConsumerSpec) {
        ConsumerSpec consumerSpec = (ConsumerSpec)o;
        retVal = "'" + consumerSpec.getConsumedType() + "'";
      } else {
        retVal = "LogPlanModel.getDescription():Unhandled type " + o;
      }
    } catch ( Exception ex) {
      System.out.println( "Exception thrown: LogPlanModel.getDescription for object " + o);
    }
    return retVal;
  }

  /**
   * Update model with adds, changes and removes of LogPlan elements.
   */
  public void logPlanUpdate() {
    Enumeration tasksAdded          = logPlanModelServerPlugIn.getTasksAdded();
    Enumeration tasksChanged        = logPlanModelServerPlugIn.getTasksChanged();
    Enumeration tasksRemoved        = logPlanModelServerPlugIn.getTasksRemoved();

    Enumeration allocationsAdded    = logPlanModelServerPlugIn.getAllocationsAdded();
    Enumeration allocationsChanged  = logPlanModelServerPlugIn.getAllocationsChanged();
    Enumeration allocationsRemoved  = logPlanModelServerPlugIn.getAllocationsRemoved();

    Enumeration nonAllocationDispositionsAdded  = logPlanModelServerPlugIn.getNonAllocationDispositionsAdded();

    while ( tasksAdded.hasMoreElements())
      handleAddedTask( (Task)tasksAdded.nextElement());
    while ( tasksRemoved.hasMoreElements())
      handleRemovedTask( (Task)tasksRemoved.nextElement());

    while ( allocationsAdded.hasMoreElements())
      handleAddedAllocations( (Allocation)allocationsAdded.nextElement());
    while ( allocationsChanged.hasMoreElements())
      handleChangedAllocations( (Allocation)allocationsChanged.nextElement());
    while ( allocationsRemoved.hasMoreElements())
      handleRemovedAllocations( (Allocation)allocationsRemoved.nextElement());

    while ( nonAllocationDispositionsAdded.hasMoreElements())
      handleAddedNonAllocationDispositions( (Disposition)nonAllocationDispositionsAdded.nextElement());
  }

  /**
   * If Society is in rehydrate mode, repopulate the model with previous content.
   */
  private void rehydrate() {
    Enumeration tasks       = logPlanModelServerPlugIn.getTasks();
    Enumeration allocations = logPlanModelServerPlugIn.getAllocations();

    while ( tasks.hasMoreElements())
      handleAddedTask( (Task)tasks.nextElement());

    while ( allocations.hasMoreElements())
      handleAddedAllocations( (Allocation)allocations.nextElement());
  }

}

