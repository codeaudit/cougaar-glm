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
package org.cougaar.domain.mlm.plugin.organization;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

import java.util.*;

import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.core.cluster.IncrementalSubscription;

import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.asset.ClusterPG;
import org.cougaar.domain.planning.ldm.asset.ItemIdentificationPGImpl;
import org.cougaar.domain.planning.ldm.asset.LocationSchedulePG;
import org.cougaar.domain.planning.ldm.asset.LocationSchedulePGImpl;
import org.cougaar.domain.planning.ldm.asset.NewClusterPG;
import org.cougaar.domain.planning.ldm.asset.NewItemIdentificationPG;
import org.cougaar.domain.planning.ldm.asset.NewLocationSchedulePG;
import org.cougaar.domain.planning.ldm.asset.NewPropertyGroup;
import org.cougaar.domain.planning.ldm.asset.NewTypeIdentificationPG;
import org.cougaar.domain.planning.ldm.asset.PropertyGroup;

import org.cougaar.domain.planning.ldm.plan.AspectType;
import org.cougaar.domain.planning.ldm.plan.NewPrepositionalPhrase;
import org.cougaar.domain.planning.ldm.plan.NewRoleSchedule;
import org.cougaar.domain.planning.ldm.plan.NewSchedule;
import org.cougaar.domain.planning.ldm.plan.NewTask;
import org.cougaar.domain.planning.ldm.plan.Preference;
import org.cougaar.domain.planning.ldm.plan.Relationship;
import org.cougaar.domain.planning.ldm.plan.Role;
import org.cougaar.domain.planning.ldm.plan.Schedule;
import org.cougaar.domain.planning.ldm.plan.ScoringFunction;
import org.cougaar.domain.planning.ldm.plan.TimeAspectValue;
import org.cougaar.domain.planning.ldm.plan.LocationScheduleElement;
import org.cougaar.domain.planning.ldm.plan.LocationScheduleElementImpl;
import org.cougaar.domain.planning.ldm.plan.TaggedLocationScheduleElement;
import org.cougaar.domain.planning.ldm.plan.Location;

import org.cougaar.core.plugin.SimplePlugIn;

import org.cougaar.util.Reflect;
import org.cougaar.util.TimeSpan;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.EmptyEnumeration;


import org.cougaar.domain.glm.ldm.Constants;
import org.cougaar.domain.glm.ldm.GLMFactory;

import org.cougaar.domain.glm.ldm.asset.AssignedPG;
import org.cougaar.domain.glm.ldm.asset.AssignedPGImpl;
import org.cougaar.domain.glm.ldm.asset.NewAssignedPG;
import org.cougaar.domain.glm.ldm.asset.Organization;
import org.cougaar.domain.glm.ldm.asset.OrganizationAdapter;

import org.cougaar.domain.glm.ldm.plan.GLMRelationship;

// ADDED BY TOPS
import org.cougaar.domain.glm.ldm.asset.Facility;
import org.cougaar.domain.glm.ldm.asset.NewAssignmentPG;
import org.cougaar.domain.glm.ldm.asset.NewPositionPG;
import org.cougaar.domain.glm.ldm.asset.PositionPGImpl;
import org.cougaar.domain.glm.ldm.asset.TransportationNode;

import org.cougaar.domain.glm.ldm.plan.NewGeolocLocation;
import org.cougaar.domain.glm.ldm.plan.NewPosition;

// END ADDED BY TOPS

//Used in testing processing of remote RFS/RFD
/*
import org.cougaar.domain.planning.ldm.plan.AspectScorePoint;
import org.cougaar.domain.planning.ldm.plan.AspectValue;
import org.cougaar.domain.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.domain.planning.ldm.plan.RemotePlanElement;
*/


/**
This is a modified version of the OrgRTDataPlugIn. OrgTPRTDataPlugIn reads time-dependent superior/subordinate 
relationships from a <cluster-id>-relationships.ini file in place of the information found underneath the
[Relationships] section in the prototype-ini.dat file and puts the times associated with the relationships
in the RFS and RFD tasks in place of the DEFAULT_START_TIME and DEFAULT_END_TIME. 
If the <cluster-id>-relationships.ini is absent or has the wrong format, it defaults to the behavior 
of the OrgRTDataPlugIn.
*/


public class OrgTPRTDataPlugIn extends OrgDataPlugIn  {
}
