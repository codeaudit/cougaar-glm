/*
 * <copyright>
 *  
 *  Copyright 1997-2004 TASC 
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
package org.cougaar.glm.plugins.multiplesuppliers;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.ldm.asset.OrganizationPG;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.Allocation;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.Disposition;
import org.cougaar.planning.ldm.plan.HasRelationships;
import org.cougaar.planning.ldm.plan.NewPrepositionalPhrase;
import org.cougaar.planning.ldm.plan.NewTask;
import org.cougaar.planning.ldm.plan.Preference;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Relationship;
import org.cougaar.planning.ldm.plan.RelationshipSchedule;
import org.cougaar.planning.ldm.plan.Role;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Workflow;
import org.cougaar.util.TimeSpan;

/**
 * This Utility class consists of "public final static" methods that perform various useful Cougaar tasks.
 * In particular, this class contains methods for cloning tasks, creating subtasks, allocation results and failed allocations.
 * There are also methods that return specific Organization assets with desired capabilities, roles or even by name.
 */
public final class Utility {

  /**
   * Obtain reference to last subtask in a workflow. Useful if you need to add a new task to a workflow
   * with aspects of the new task based on that last task or its allocation results.
   *
   * @param   workflow to find last subtask of.
   * @return  the last task in the workflow
   */
  public final static Task getLastSubtaskInWorkflow( Workflow workflow) {
    Enumeration subtasks = workflow.getTasks();
    Task lastSubtask = null;
    while ( subtasks.hasMoreElements()) {
      lastSubtask = (Task)subtasks.nextElement();
    }
    return lastSubtask;
  }

  /**
   * Create a new subtask using the grammatical elements of the input task,
   * further establishing the input task as the parent of the new subtask.
   *
   * @param factory reference to cluster's PlanningFactory
   * @param task the task whose grammatical elements will be used
   *             to create a new Task object.
   * @return A newly created Task patterned from an input task.
   */
  public final static NewTask createSubtaskFromTask( PlanningFactory factory, Task task) {
    return createSubtaskFromTask( factory, task, null);
  }

  /**
   * Create a new subtask using the grammatical elements of the input task,
   * further establishing the input task as the parent of the new subtask.
   *
   * @param factory reference to cluster's PlanningFactory
   * @param task the task whose grammatical elements will be used
   *             to create a new Task object.
   * @param prepositionsToExclude a list of prepositional phrases that should not be copied from
   *        the original task to the new task.
   * @return A newly created Task patterned from an input task.
   */
  public final static NewTask createSubtaskFromTask( PlanningFactory factory, Task task, String[] prepositionsToExclude) {
    NewTask subtask = cloneTask( factory, task, prepositionsToExclude);
    subtask.setParentTaskUID( task.getUID());
    subtask.setWorkflow( null);
    return subtask;
  }

  /**
   * Create a new Task object using the grammatical elements of the input task.
   * For prepositional phrases with indirect objects that are instances of Vector,
   * a new Vector is created for the clone. The contents of the original Vector are
   * copied by reference to the new Vector. This way, the new Vector's content can
   * be changed without effecting the original Vector.
   *
   * @param factory reference to cluster's PlanningFactory
   * @param task the task whose grammatical elements will be used
   *             to create a new Task object.
   * @return A newly created Task patterned from an input task.
   */
  public final static NewTask cloneTask( PlanningFactory factory, Task task) {
    return cloneTask( factory, task, null);
  }

  /**
   * Create a new Task object using the grammatical elements of the input task.
   * For prepositional phrases with indirect objects that are instances of Vector,
   * a new Vector is created for the clone. The contents of the original Vector are
   * copied by reference to the new Vector. This way, the new Vector's content can
   * be changed without effecting the original Vector.
   *
   * @param factory reference to cluster's PlanningFactory
   * @param task the task whose grammatical elements will be used
   *             to create a new Task object.
   * @param prepositionsToExclude a list of prepositional phrases that should not be copied from
   *        the original task to the new task.
   * @return A newly created Task patterned from an input task.
   */
  public final static NewTask cloneTask( PlanningFactory factory, Task task, String[] prepositionsToExclude) {
    Enumeration enum = null;
    NewTask subtask = factory.newTask();
    subtask.setContext( task.getContext());
    subtask.setPlan( task.getPlan());
    subtask.setParentTaskUID( task.getParentTaskUID());
    subtask.setVerb( task.getVerb());
    subtask.setDirectObject( task.getDirectObject());
    subtask.setWorkflow( task.getWorkflow());

    enum = task.getPrepositionalPhrases();
    if ( enum != null) {
      Vector pPhrases = new Vector();
      NewPrepositionalPhrase clonePhrase;
      while ( enum.hasMoreElements()) {
        PrepositionalPhrase pPhrase = (PrepositionalPhrase)enum.nextElement();

        // Don't include prepositional phrases in the new task that are listed in prepositionsToExclude.
        if ( prepositionsToExclude != null) {
          boolean skipPreposition = false;
          String preposition = pPhrase.getPreposition();
          for ( int i = 0; i < prepositionsToExclude.length && !skipPreposition; i++) {
            String tmpPreposition = (String)prepositionsToExclude[ i];
            if ( tmpPreposition != null && tmpPreposition.equals( preposition)) {
              skipPreposition = true;
            }
          }
          if ( skipPreposition) {
            continue;
          }
        }

        clonePhrase = factory.newPrepositionalPhrase();
        clonePhrase.setPreposition( pPhrase.getPreposition());
        Object object = pPhrase.getIndirectObject();
        if ( object instanceof Vector) {
          Vector orgVector = (Vector)object;
          Class vectorClass = object.getClass();
          Vector newVector = null;
          try {
            newVector = (Vector)vectorClass.newInstance();
          } catch ( Exception ex) {
            System.out.println( "PlanElements.cloneTask: unable to create newVector instance");
            return null;
          }
          Enumeration tmpEnum = orgVector.elements();
          while ( tmpEnum.hasMoreElements()) {
            newVector.add( tmpEnum.nextElement());
          }
          clonePhrase.setIndirectObject( newVector);
        } else {
          clonePhrase.setIndirectObject( object);
        }
        pPhrases.addElement( clonePhrase);
      }
      subtask.setPrepositionalPhrases( pPhrases.elements());
    }

    enum = task.getPreferences();
    if ( enum != null) {
      Vector preferences = new Vector();
      Preference clonePreference;
      while ( enum.hasMoreElements()) {
        Preference preference = (Preference)enum.nextElement();
        clonePreference = factory.newPreference( preference.getAspectType(),
                                                  preference.getScoringFunction());
        preferences.addElement( clonePreference);
      }
      subtask.setPreferences( preferences.elements());
    }

    return subtask;
  }

  /**
   * Create an Failed Disposition object associated with a task.
   *
   * @param factory reference to cluster's PlanningFactory
   * @param task the task to be associated with the Disposition.
   * @return a Failed Disposition object associated with the task
   */
  public final static Disposition makeFailedDisposition( PlanningFactory factory, Task task) {
    AllocationResult failedAR = createAllocationResult( factory, false,
      task.getPreferredValue( AspectType.START_TIME),
      task.getPreferredValue( AspectType.END_TIME), 0, 0);
    Disposition falloc = factory.createFailedDisposition( task.getPlan(), task, failedAR);
    return falloc;
  }

  /**
   * Create an Allocation object (incorporating COST, QUANTITY and SCHEDULE).
   *
   * @param factory reference to cluster's PlanningFactory
   * @param task the task being disposed of by this Allocation.
   * @param asset used to perform the task
   * @param cost the value to set the COST aspect value to
   * @param quantity the value to set the QUANTITY aspect value to
   *
   * @return an Allocation object which is the disposition of the input task
   */
  public final static Allocation createAllocation( PlanningFactory factory,
                                        Task task, Asset asset, double cost, double quantity) {
    return createAllocation( factory, task, asset, cost, quantity, null);
  }

  /**
   * Create an Allocation object (incorporating COST, QUANTITY, SCHEDULE, and associated Role).
   *
   * @param factory reference to cluster's PlanningFactory
   * @param task the task being disposed of by this Allocation.
   * @param asset used to perform the task
   * @param cost the value to set the COST aspect value to
   * @param quantity the value to set the QUANTITY aspect value to
   * @param roleOfAsset the role of the asset performing this task. If roleOfAsset is null,
   *        Role.BOGUS will be used.
   * @return an Allocation object which is the disposition of the input task
   */
  public final static Allocation createAllocation( PlanningFactory factory,
                                        Task task, Asset asset, double cost, double quantity, Role roleOfAsset) {
    AllocationResult estAllocResult = null;
    if ( !(asset instanceof Organization) || task.getVerb().toString().equals( Constants.Verb.INFORM)) {
      double startTime = task.getPreferredValue( AspectType.START_TIME);
      double endTime   = task.getPreferredValue( AspectType.END_TIME);
      estAllocResult = createAllocationResult( factory, true, startTime, endTime, cost, quantity);
    }

    Role role = roleOfAsset;
    if ( role == null) {
      role = Role.BOGUS;
    }
    Allocation allocation = factory.createAllocation(
                            factory.getRealityPlan(), task, asset, estAllocResult, role);
    return allocation;
  }

  /**
   * Create an AllocationResult object (incorporating START_TIME, END_TIME, COST and QUANTITY).
   * This method sets confidence rating to 1.0.
   *
   * @param factory reference to cluster's PlanningFactory
   * @param successFlag success value to associate with this allocation result.
   * @param startTime of this allocation
   * @param endTime of this allocation
   * @param cost the value to set the COST aspect value to
   * @param quantity the value to set the QUANTITY aspect value to
   * @return an AllocationResult object with aspect set to input values
   */
  public final static AllocationResult createAllocationResult( PlanningFactory factory, boolean successFlag,
                                double startTime, double endTime, double cost, double quantity) {
    double[] resultsarray = new double[4];

    resultsarray[ 0] = startTime;
    resultsarray[ 1] = endTime;
    resultsarray[ 2] = cost;
    resultsarray[ 3] = quantity;
    return factory.newAllocationResult( 1.0, successFlag, AspectType.CommonAspects, resultsarray);
  }

  /**
   * Return Task with the given UID.
   *
   * @param tasks list of Tasks to choose from.
   * @param uid the UID of the desired Task.
   * @return Task with given UID.
   */
  public final static Task getTaskWithUID( Enumeration tasks, String uid) {
    Task retTask = null;
    while ( tasks.hasMoreElements() && retTask == null) {
      Task tmpTask = (Task)tasks.nextElement();
      if ( tmpTask.getUID().toString().equals( uid)) {
        retTask = tmpTask;
      }
    }
    return retTask;
  }

  /**
   * Return Organization with the given name.
   *
   * @param organizationName the name of the desired organization.
   * @param organizationAssets list of Organizations to choose from.
   * @return Organization with given name.
   */
  public final static Organization findNamedOrganization( String organizationName, Enumeration organizationAssets) {
    Organization retOrganization = null;
    while ( retOrganization == null && organizationAssets.hasMoreElements()) {
      Organization org = (Organization)organizationAssets.nextElement();
      if ( org.getItemIdentificationPG().getNomenclature().equals( organizationName)) {
        retOrganization = org;
      }
    }
    return retOrganization;
  }

  /**
   * Return Collection of Organization's roles.
   *
   * @param organization the Organization to find Roles for.
   * @return list of Roles associated with input Organization.
   */
  public final static Collection getOrganizationRoles( Organization organization) {
    OrganizationPG organizationPG = organization.getOrganizationPG();
    if ( organizationPG != null) {
      return organizationPG.getRoles();
    }
    return null;
  }

  /**
   * Does the input Organization have the desired Role?
   *
   * @param organization the Organization to test for desired Role.
   * @param testrole the desired Role to test for.
   * @return true/false - Does the input Organization have the desired Role?
   */
  public final static boolean testCapableRole( Organization organization, String testrole) {
    Collection caproles = getOrganizationRoles( organization);
    if ( caproles != null && caproles.contains( Role.getRole( testrole))) {
      return true;
    }
    return false;
  }

  /**
   * Obtain a list or Organizations with a particular Role.
   *
   * @param testCapableRole the Role to find matching Organizations for.
   * @param organizationAssets list of Organizations to choose from.
   * @return list or Organizations with a desired Role.
   */
  public final static Enumeration findOrgsWithCapability( String testCapableRole, Enumeration organizationAssets) {
    Vector orgsWithCapableRole = new Vector();
    while (organizationAssets.hasMoreElements()) {
      Organization org = (Organization)organizationAssets.nextElement();
      if ( testCapableRole( org, testCapableRole) == true) {
        orgsWithCapableRole.add(org);
      }
    }
    return orgsWithCapableRole.elements();
  }

  /**
   * Obtain list of Administrative Subordinates for an organization.
   *
   * @param org whose Administrative Subordinates to find
   * @return the list of Administrative Subordinates
   */
  public final static Enumeration getAdministrativeSubordinates( Organization org) {
    Vector subs = new Vector();

    RelationshipSchedule schedule = org.getRelationshipSchedule();
    Collection orgCollection = schedule.getMatchingRelationships(Constants.Role.ADMINISTRATIVESUBORDINATE, TimeSpan.MIN_VALUE, TimeSpan.MAX_VALUE);

    if ( orgCollection.size() > 0) {
      for (Iterator relIterator = orgCollection.iterator(); relIterator.hasNext();) {
        Relationship relationship = (Relationship) relIterator.next();
        HasRelationships sub = schedule.getOther(relationship);
        if (!subs.contains(sub)) {
          subs.add(sub);
        }
      }
    }
    return subs.elements();
  }

  /**
   * Obtain list of Administrative Superiors for an organization.
   *
   * @param org whose Administrative Superiors to find
   * @return the list of Administrative Superiors
   */
  public final static Enumeration getAdministrativeSuperiors( Organization org) {
    Vector subs = new Vector();

    RelationshipSchedule schedule = org.getRelationshipSchedule();
    Collection orgCollection = schedule.getMatchingRelationships(Constants.Role.ADMINISTRATIVESUPERIOR, TimeSpan.MIN_VALUE, TimeSpan.MAX_VALUE);

    if ( orgCollection.size() > 0) {
      for (Iterator relIterator = orgCollection.iterator(); relIterator.hasNext();) {
        Relationship relationship = (Relationship) relIterator.next();
        HasRelationships sub = schedule.getOther(relationship);
        if (!subs.contains(sub)) {
          subs.add(sub);
        }
      }
    }
    return subs.elements();
  }

  /**
   * Determine if two AllocationResult objects are equal by comparing
   * START_TIME, END_TIME, COST and QUNATITY.
   *
   * @param ar1 1st AllocationResult in comparison.
   * @param ar2 2nd AllocationResult in comparison.
   * @return true/false are the AllocationResults equals?
   */
  public final static boolean equalResults( AllocationResult ar1, AllocationResult ar2) {
    if ( ar1 == null && ar2 == null) {
      return true;
    } else if ( ar1 == null && ar2 != null || ar1 != null && ar2 == null) {
      return false;
    }

    int[] aspects = ar1.getAspectTypes();
    for (int i = 0; i < aspects.length; i++) {
      if ( ar1.isDefined( AspectType.START_TIME)) {
        if ( ar2.isDefined( AspectType.START_TIME)) {
          if ( ar1.getValue( AspectType.START_TIME) != ar2.getValue( AspectType.START_TIME)) {
            return false;
          }
        } else {
          return false;
        }
      }
      if ( ar1.isDefined( AspectType.END_TIME)) {
        if ( ar2.isDefined( AspectType.END_TIME)) {
          if ( ar1.getValue( AspectType.END_TIME) != ar2.getValue( AspectType.END_TIME)) {
            return false;
          }
        } else {
          return false;
        }
      }
      if ( ar1.isDefined( AspectType.QUANTITY)) {
        if ( ar2.isDefined( AspectType.QUANTITY)) {
          if ( ar1.getValue( AspectType.QUANTITY) != ar2.getValue( AspectType.QUANTITY)) {
            return false;
          }
        } else {
          return false;
        }
      }
      if ( ar1.isDefined( AspectType.COST)) {
        if ( ar2.isDefined( AspectType.COST)) {
          if ( ar1.getValue( AspectType.COST) != ar2.getValue( AspectType.COST)) {
            return false;
          }
        } else {
          return false;
        }
      }
    }
    return true;
  }
}
