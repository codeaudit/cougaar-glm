/*
 * <copyright>
 *  Copyright 1999-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.plugin.assessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.text.MessageFormat;

import org.cougaar.util.TimeSpan;
import org.cougaar.util.UnaryPredicate;

import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.core.cluster.IncrementalSubscription;

import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.asset.ItemIdentificationPG;

import org.cougaar.domain.planning.ldm.plan.NewReport;
import org.cougaar.domain.planning.ldm.plan.Relationship;
import org.cougaar.domain.planning.ldm.plan.RelationshipSchedule;
import org.cougaar.domain.planning.ldm.plan.Role;

import org.cougaar.core.plugin.SimplePlugIn;
import org.cougaar.core.plugin.PlugInAdapter;

import org.cougaar.domain.glm.Constants;

import org.cougaar.domain.glm.asset.Organization;

/**
 * The SupportingUnitAssessorPlugIn monitors organization assets and generates
 * an Report if one is added/modified/deleted.
 *
 * @author       ALPINE <alpine-software@bbn.com>
 * @version $Id: SupportingUnitAssessorPlugIn.java,v 1.1 2000-12-15 20:17:46 mthome Exp $ */

public class SupportingUnitAssessorPlugIn extends SimplePlugIn {

  private IncrementalSubscription mySelfOrgSubscription;
  private String []myMessageArgs;

  /* Changes to formats ==> changes to getMessageArgs()
   */
  private final MessageFormat myFormat = 
    new MessageFormat("Cluster {0}: {1} has changed: supporting and subordinate relationships {2}.");

  private final MessageFormat myRemoveFormat = 
    new MessageFormat("Cluster {0}: {1} has been removed: supporting and subordinate relationships {2}.");


  /**
   * selfOrgPred - returns an UnaryPredicate to find self organizations.
   *
   * @return UnaryPredicate 
   */
  private UnaryPredicate selfOrgPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        boolean match = false;

        if (o instanceof Organization) {
          Organization org = (Organization)o;

          return (org.isSelf());
        } else {
          return false;
        }
      }
    };
  }

  /**
   * execute - CCV2 execute method - called whenever IncrementalSubscription
   * has changed. 
   *
   */
  public synchronized void execute() {
    if (mySelfOrgSubscription.hasChanged()) {
      checkAdd(mySelfOrgSubscription.getAddedCollection());
      checkChange(mySelfOrgSubscription.getChangedCollection());
      checkRemove(mySelfOrgSubscription.getRemovedCollection());
    } 
  }

  /**
   * setUpSubscriptions - sets up subscription to screen for supporting
   * organization assets
   */
  protected void setupSubscriptions() {
    initMessageArgs();
    mySelfOrgSubscription = 
      (IncrementalSubscription)subscribe(selfOrgPred());
  }

  /**
   * initMessageArgs - initializes message argument array. Changes to
   * message formats (myAddFormat, myModifyFormat, myRemoveFormat) must be
   * accompanied with cahnges to initMessageArgs.
   *
   * Using one arg array for all formats at this point. Cluster name at 
   * index 0.
   */
  private void initMessageArgs() {
    myMessageArgs = new String[3];

    myMessageArgs[0] = getClusterIdentifier().cleanToString();
  }

  /**
   * checkAdd - handle new self organizations
   * Generates an Report for each.
   *
   * @param newOrgs Collection of the added self orgs
   */
  private void checkAdd(Collection newOrgs) {
    if (newOrgs == null) {
      return;
    }

    Iterator iterator = newOrgs.iterator();

    while (iterator.hasNext()) {
      Organization org = (Organization)iterator.next();
      Collection supportingRelationships = findSupportingRelationships(org);

      if (supportingRelationships.size() > 0) {
        NewReport report = getFactory().newReport();
        
        report.setDate(new Date(getCluster().currentTimeMillis()));
        report.setText(myFormat.format(getMessageArgs(org, 
                                                      supportingRelationships)));
        
        System.out.println(report.getText());
        
        if (!publishAdd(report)) {
          throw new RuntimeException("SupportingUnitAssessorPlugin: unable to " + 
                                     "publish add Report for " + org);
        }
      }
    }
  }

  /**
   * checkChange - handle all the modified self orgs
   * Generates an Report for each.
   * BOZO - do we really want to know about all changes?
   *
   * @param changedOrgs Collection of all the modified self Orgs.
   */
  private void checkChange(Collection changedOrgs)  {
    if (changedOrgs == null) {
      return;
    }

    Iterator iterator = changedOrgs.iterator();

    while (iterator.hasNext()) {
      Organization org = (Organization)iterator.next();
      Collection supportingRelationships = findSupportingRelationships(org);

      if (supportingRelationships.size() > 0) {
        NewReport report = getFactory().newReport();
        
        report.setDate(new Date(getCluster().currentTimeMillis()));
        report.setText(myFormat.format(getMessageArgs(org, 
                                                      supportingRelationships)));
        
        System.out.println(report.getText());
        if (!publishAdd(report)) {
          throw new RuntimeException("SupportingUnitAssessorPlugin: unable to " + 
                                     "publish modify Report for " + org);
        }
      }
    }
  }

  /**
   * checkRemove - handle all the removed self orgs
   * Generates an Report for each.
   *
   * @param removedOrgs Collection of all the removed self orgs
   */
  private void checkRemove(Collection removedOrgs)  {
    if (removedOrgs == null) {
      return;
    }

    Iterator iterator = removedOrgs.iterator();

    while (iterator.hasNext()) {
      Organization org = (Organization)iterator.next();
      Collection supportingRelationships = findSupportingRelationships(org);

      if (supportingRelationships.size() > 0) {
        NewReport report = getFactory().newReport();
        
        report.setDate(new Date(getCluster().currentTimeMillis()));
        report.setText(myRemoveFormat.format(getMessageArgs(org, 
                                                            supportingRelationships)));
        
        System.out.println(report.getText());
        
        if (!publishAdd(report)) {
          throw new RuntimeException("SupportingUnitAssessorPlugin: unable to " + 
                                     "publish remove Report for " + org);
        }
      }
    }
  }

  private static Role SUPERIOR = Role.getRole("Superior");

  /**
   * findSupportingRelationships - returns all supporting relationships for 
   * the specified organization
   *
   * @param org Organization
   * @return Collection supporting relationships
   */
  private Collection findSupportingRelationships(Organization org) {
    RelationshipSchedule schedule = org.getRelationshipSchedule();

    Collection subordinates = org.getSubordinates(TimeSpan.MIN_VALUE,
                                                  TimeSpan.MAX_VALUE);

    Collection providers = 
      schedule.getMatchingRelationships(Constants.RelationshipType.PROVIDER_SUFFIX,
                                        TimeSpan.MIN_VALUE,
                                        TimeSpan.MAX_VALUE); 

    ArrayList supporting = new ArrayList(providers);

    for (Iterator iterator = subordinates.iterator();
         iterator.hasNext();) {
      supporting.add(iterator.next());
    }

    return supporting;
  }

  /**
   * getMessageArgs - returns array of message arguments to be used 
   * with message formats.
   * Changes to message formats must be coordinated with changes to
   * getMessageArgs.
   *
   * @param org Organization 
   * @return String[] info from org which will be used by the message 
   * formattor.
   */
  private String []getMessageArgs(Organization org, 
                                  Collection supportingRelationships) {
    //myMessageArgs[0] set to cluster name
    
    myMessageArgs[1] = getName(org);

    boolean first = true;
    String supportInfo = "";
    RelationshipSchedule schedule = org.getRelationshipSchedule();
    for (Iterator iterator = supportingRelationships.iterator();
         iterator.hasNext();) {
      Relationship relationship = (Relationship) iterator.next();
      String start = (relationship.getStartTime() == TimeSpan.MIN_VALUE) ?
        "TimeSpan.MIN_VALUE" : new Date(relationship.getStartTime()).toString();
      String end = (relationship.getEndTime() == TimeSpan.MAX_VALUE) ?
        "TimeSpan.MAX_VALUE" : new Date(relationship.getEndTime()).toString();

      String text = supportInfo + " " + schedule.getOtherRole(relationship) + 
        " " +  schedule.getOther(relationship) + 
        " start:" + start + " end:" + end;
      if (first) {
        supportInfo = text;
        first = false;
      } else {
        supportInfo = supportInfo + ", " + text;
      }
    }
     
    myMessageArgs[2] = supportInfo;
    return myMessageArgs;
  }

  /**
   * getName - return organization's name
   *
   * @param org Organization
   * @return String
   */
  private static String getName(Organization org) {
    ItemIdentificationPG itemIdentificationPG = org.getItemIdentificationPG();
    
    return itemIdentificationPG.getNomenclature();
  }
}







