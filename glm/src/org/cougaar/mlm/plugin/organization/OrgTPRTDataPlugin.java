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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.GLMFactory;
import org.cougaar.glm.ldm.asset.AssignedPGImpl;
import org.cougaar.glm.ldm.asset.Facility;
import org.cougaar.glm.ldm.asset.NewAssignedPG;
import org.cougaar.glm.ldm.asset.NewAssignmentPG;
import org.cougaar.glm.ldm.asset.NewPositionPG;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.ldm.asset.OrganizationAdapter;
import org.cougaar.glm.ldm.asset.PositionPGImpl;
import org.cougaar.glm.ldm.asset.TransportationNode;
import org.cougaar.glm.ldm.plan.GLMRelationship;
import org.cougaar.glm.ldm.plan.NewGeolocLocation;
import org.cougaar.glm.ldm.plan.NewPosition;
import org.cougaar.planning.ldm.asset.LocationSchedulePG;
import org.cougaar.planning.ldm.asset.LocationSchedulePGImpl;
import org.cougaar.planning.ldm.asset.NewClusterPG;
import org.cougaar.planning.ldm.asset.NewItemIdentificationPG;
import org.cougaar.planning.ldm.asset.NewLocationSchedulePG;
import org.cougaar.planning.ldm.asset.NewPropertyGroup;
import org.cougaar.planning.ldm.asset.NewTypeIdentificationPG;
import org.cougaar.planning.ldm.asset.PropertyGroup;
import org.cougaar.planning.ldm.plan.AspectType;
import org.cougaar.planning.ldm.plan.AspectValue;
import org.cougaar.planning.ldm.plan.Location;
import org.cougaar.planning.ldm.plan.NewPrepositionalPhrase;
import org.cougaar.planning.ldm.plan.NewRoleSchedule;
import org.cougaar.planning.ldm.plan.NewSchedule;
import org.cougaar.planning.ldm.plan.NewTask;
import org.cougaar.planning.ldm.plan.Preference;
import org.cougaar.planning.ldm.plan.Relationship;
import org.cougaar.planning.ldm.plan.Role;
import org.cougaar.planning.ldm.plan.Schedule;
import org.cougaar.planning.ldm.plan.ScoringFunction;
import org.cougaar.planning.ldm.plan.TaggedLocationScheduleElement;
import org.cougaar.planning.ldm.plan.TimeAspectValue;
import org.cougaar.planning.plugin.legacy.SimplePlugin;
import org.cougaar.util.ConfigFinder;
import org.cougaar.util.EmptyEnumeration;
import org.cougaar.util.Reflect;
import org.cougaar.util.TimeSpan;

// END ADDED BY TOPS

//Used in testing processing of remote RFS/RFD
/*
import org.cougaar.planning.ldm.plan.AspectScorePoint;
import org.cougaar.planning.ldm.plan.AspectValue;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.RemotePlanElement;
*/


/**
This is a modified version of the OrgRTDataPlugin. OrgTPRTDataPlugin reads time-dependent superior/subordinate 
relationships from a <cluster-id>-relationships.ini file in place of the information found underneath the
[Relationships] section in the prototype-ini.dat file and puts the times associated with the relationships
in the RFS and RFD tasks in place of the DEFAULT_START_TIME and DEFAULT_END_TIME. 
If the <cluster-id>-relationships.ini is absent or has the wrong format, it defaults to the behavior 
of the OrgRTDataPlugin.
*/


public class OrgTPRTDataPlugin extends SimplePlugin  {
  private static TrivialTimeSpan ETERNITY = 
    new TrivialTimeSpan(TimeSpan.MIN_VALUE,
                        TimeSpan.MAX_VALUE);

  private final static String UTC = "UTC/RTOrg";

  private static Calendar myCalendar = Calendar.getInstance();

  private static long DEFAULT_START_TIME = -1;
  private static long DEFAULT_END_TIME = -1;

  static {
    myCalendar.set(1990, 0, 1, 0, 0, 0);
    DEFAULT_START_TIME = myCalendar.getTime().getTime();

    myCalendar.set(2010, 0, 1, 0, 0, 0);
    DEFAULT_END_TIME = myCalendar.getTime().getTime();   
  }

  private Vector organization_vector = new Vector();
  private Organization selfOrg;
  private GLMFactory aldmf;

  // Used only for testing remote RFS/RFD
  /*
  private static boolean once = false;
  private IncrementalSubscription orgAssets;

  public static UnaryPredicate orgPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof Organization) {
          return true;
        } else {
          return false;
        }
      }
    };
  }
  */

  protected void setupSubscriptions() {
    aldmf = (GLMFactory)getFactory("glm");
    getSubscriber().setShouldBePersisted(false);

    if (!didRehydrate()) {
      processOrganizations();	// Objects should already exist after rehydration
    }

    //Used in testing processing of remote RFS/RFD
    /*
    orgAssets = (IncrementalSubscription)subscribe(orgPred());
    */
  }

  //Used in testing processing of remote RFS/RFD
  protected void execute() {
    /*
    if (orgAssets.hasChanged()) {
      Collection collection = orgAssets.getCollection();

      if ((!once) && 
          (collection.size() > 2)) {
        Organization self = null;
        Organization orgA = null;
        Organization orgB = null;
        System.out.println(101);
        for (Iterator iterator = collection.iterator(); iterator.hasNext();) {
          Organization org = (Organization) iterator.next();

          if (org.isSelf()) {
            self = org;
          } else if (orgA == null) {
            orgA = org;
          } else if ((orgB == null) &&
                     (!org.equals(orgA))) {
            orgB = org;
            break;
          }
        }

        Organization orgAClone = (Organization)getFactory().cloneInstance(orgA);
        Organization orgBClone = (Organization)getFactory().cloneInstance(orgB);
          
        ArrayList roles = new ArrayList();
        roles.add(Role.getRole("Bogus1"));
        roles.add(Role.getRole("Bogus2"));
        
        NewTask remoteTask = createRFS(orgBClone, orgAClone, roles);
        publish(remoteTask);


        orgAClone = (Organization)getFactory().cloneInstance(orgA);
        orgBClone = (Organization)getFactory().cloneInstance(orgB);
        
        ArrayList rfdRoles = new ArrayList();
        rfdRoles.add(Role.getRole("Bogus_Subordinate"));
        rfdRoles.add(Role.getRole("Operational_Subordinate"));
        
        remoteTask = createRFD(orgBClone, orgAClone, rfdRoles);
        publish(remoteTask);
        
        System.out.println("OrgRTData: execute from " + getMessageAddress() + " " +
                           orgAClone.getItemIdentificationPG().getItemIdentification() + 
                           " reporting for service to " + 
                           orgBClone.getItemIdentificationPG().getItemIdentification());
        once = true;
      }
      } */
  }

  /**
   * Parses the prototype-ini file and in the process sets up
   * the organization_vector with pairs of "relationship"/"organization"
   * It then loops through the vector and for each organization
   * it parses the appropriate prototype-ini file.
   */
  protected void processOrganizations() {
    try {
      String cId = getMessageAddress().getAddress();
      ParsePrototypeFile(cId, GLMRelationship.SELF);

      String organizations[][] = null;

	  // get the time phased relations from teh relationship file
      Vector relationshipsVector = parseRelationshipFile(cId, GLMRelationship.SELF);

	  // Put the organizations for this cluster into array
	  if (relationshipsVector != null) {
		  //System.out.println(cId+": TPRTdataPlugin parsed relationships file");
	      organizations = new String[relationshipsVector.size()][5];
	      relationshipsVector.copyInto(organizations);
	  }
	  else {
	      organizations = new String[organization_vector.size()][5];
		  organization_vector.copyInto(organizations);
	  }
      // For each organization, parse appropriate prototype-ini file
      for (int j = 0; j < organizations.length; j++) {
        // 980723 EWD changed second condition to use index 1 instead of 0
	if ((organizations[j][0] != null) && (organizations[j][1] != null)) {
	  Class r_class = GLMRelationship.class;
	  Field f = r_class.getField(organizations[j][0].toUpperCase());
	  if ((GLMRelationship.SUPPORTING).equals(f.get(null))) {
            cloneMe(organizations[j][1], organizations[j][2], organizations[j][3], organizations[j][4]);
	  } else if((GLMRelationship.SUPERIOR).equals(f.get(null))) {
            createSuperior(organizations[j][1], organizations[j][3], organizations[j][4]);
	  }
	}
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  

  protected void createSuperior(String sup, String startTimeStr, String endTimeStr) {
    Organization superiorOrg = createOrganization(sup);

    if (superiorOrg == null) {
      throw new RuntimeException("OrgRTDataPlugin: Unable to create superior org asset " + sup);
    } else if (selfOrg == null) {
      throw new RuntimeException("OrgRTDataPlugin: selfOrg is null in createSuperior");
    }
    
    NewAssignedPG superiorCapability;
    
    if (!superiorOrg.hasAssignedPG()) {
      superiorCapability = 
        (NewAssignedPG)getFactory().createPropertyGroup(AssignedPGImpl.class);
    } else { 
      superiorCapability = (NewAssignedPG)superiorOrg.getAssignedPG().copy();
    }
    
    ArrayList roles = new ArrayList(1);
    roles.add(Constants.Role.ADMINISTRATIVESUPERIOR);
    superiorCapability.setRoles(roles);
    superiorOrg.setAssignedPG(superiorCapability);

    // clone the selfOrg
    Organization clone = (Organization)getFactory().cloneInstance(selfOrg);
    NewAssignedPG cloneCapability;
    
    if (!clone.hasAssignedPG()) {
      cloneCapability = 
        (NewAssignedPG)getFactory().createPropertyGroup(AssignedPGImpl.class);
    } else { 
      cloneCapability = (NewAssignedPG)clone.getAssignedPG().copy();
    }
    roles.clear();
    roles.add(Constants.Role.ADMINISTRATIVESUBORDINATE);
    cloneCapability.setRoles(roles);
    clone.setAssignedPG(cloneCapability);

    publish(createRFD(superiorOrg, clone, roles, startTimeStr, endTimeStr));
  }

  
  // creates a copy of my "self" org with special capable roles to send to a
  // cluster I am supporting.
  // Also create a client org in this cluster
  protected void cloneMe(String sendto, String caproles, String startTimeStr, String endTimeStr) {
    if (selfOrg == null) {
      System.err.println("OrgRTDataPlugin: selfOrg is null in cloneMe");
      return;
    }

    // clone the selfOrg
    Organization clone = (Organization)getFactory().cloneInstance(selfOrg);
    Organization  client = createOrganization(sendto);

    if (client == null) {
      System.err.println("OrgRTDataPlugin: Unable to create client " + sendto);
      return;
    }
    
    //To assist in debugging, I've broken the assignement into chunks
    NewAssignedPG cloneCapability;

    if (!clone.hasAssignedPG()) {
      cloneCapability = 
        (NewAssignedPG)getFactory().createPropertyGroup(AssignedPGImpl.class);
    } else {
      cloneCapability = (NewAssignedPG)clone.getAssignedPG().copy();
    }

    //To assist in debugging, I've broken the assignement into chunks
    NewAssignedPG clientCapability;

    if (!client.hasAssignedPG()) {
      clientCapability =
        (NewAssignedPG)getFactory().createPropertyGroup(AssignedPGImpl.class);
    } else {
      clientCapability = (NewAssignedPG)(client.getAssignedPG().copy());
    }
    
    Vector rolestrs= org.cougaar.util.StringUtility.parseCSV(caproles);
    Collection roles = new ArrayList();
    
    for (Iterator i = rolestrs.iterator(); i.hasNext();) {
      Role role = Role.getRole((String)i.next());
      roles.add(role);
    }
    cloneCapability.setRoles(roles);
    clone.setAssignedPG(cloneCapability);
    
    publish(createRFS(client, clone, roles, startTimeStr, endTimeStr));
  }

  protected Organization createOrganization(String orgStr) {
    final String uic = orgStr.startsWith("UIC/") ? orgStr : "UIC/".concat(orgStr);

    // Use the same domain name for all org assets now
    Organization org = (Organization)getFactory().createAsset("Organization");
    org.initRelationshipSchedule();
    org.setLocal(false);
  	
    ((NewTypeIdentificationPG)org.getTypeIdentificationPG()).setTypeIdentification(UTC);

    NewItemIdentificationPG itemIdProp = 
      (NewItemIdentificationPG)org.getItemIdentificationPG();
    itemIdProp.setItemIdentification(uic);
    itemIdProp.setNomenclature(orgStr);
    itemIdProp.setAlternateItemIdentification(orgStr);
    
    NewClusterPG cpg = (NewClusterPG)org.getClusterPG();
    cpg.setMessageAddress(MessageAddress.getMessageAddress(orgStr));
    
    return org;
  }
  
  //create the RFD task to be sent to myself which will result in an asset transfer
  // of myself being sent to the cluster I am supporting.
  protected NewTask createRFD(Organization sup, Organization sub, 
                              Collection roles, String startTimeStr, String endTimeStr) {
	long[] times = parseTimeStrings(startTimeStr, endTimeStr);
	//System.out.println("OrgTPRTDataPlugin: Creating RFD Task for: " + sup.getItemIdentificationPG().getItemIdentification()+ " START_TIME="+new Date(times[0]) + " END_TIME="+new Date(times[1]));
    NewTask rfdTask = createReportTask(sub, sup, roles,
                                       times[0], times[1]);
    rfdTask.setVerb(Constants.Verb.ReportForDuty);
    
    return rfdTask;
  }

  //create the RFD task to be sent to myself which will result in an asset transfer
  // of myself being sent to the cluster I am supporting.
  protected NewTask createRFS(Organization client, Organization reportingOrg, 
                              Collection roles, String startTimeStr, String endTimeStr) {
	long[] times = parseTimeStrings(startTimeStr, endTimeStr);
	//System.out.println("OrgTPRTDataPlugin: Creating RFS Task for: " + reportingOrg.getItemIdentificationPG().getItemIdentification()+ " START_TIME="+new Date(times[0]) + " END_TIME="+new Date(times[1]));
	NewTask rfsTask = createReportTask(reportingOrg, client, roles, 
                                       times[0], times[1]);
    rfsTask.setVerb(Constants.Verb.ReportForService);
    
    return rfsTask;
  }

  protected long[] parseTimeStrings(String startTimeStr, String endTimeStr) {
    try {
		String year  = "";
		String month = "";
		String day   = "";
	
		//System.out.println("OrgTPRTDataPlugin: parseTimeStrings: startTimeStr=" + startTimeStr + " endTimeStr=" + endTimeStr);

		boolean validDate = true;

		long startTime = DEFAULT_START_TIME;
		long endTime   = DEFAULT_END_TIME;
		

		// process only if startTimeStr and endTimeStr are of non-zero length
		if (startTimeStr != null && endTimeStr != null) {
			Calendar cal = Calendar.getInstance();
			StringTokenizer st = null;
			if (!startTimeStr.equals("")) {
				st = new StringTokenizer(startTimeStr, "/ \\");
				if (st.hasMoreTokens())
					month = st.nextToken();
				else
					validDate = false;
				if (st.hasMoreTokens())
					day = st.nextToken();
				else
					validDate = false;
				if (st.hasMoreTokens())
					year = st.nextToken();		
				else
					validDate = false;
			}
			
			if (validDate) {	// startTimeStr has all the components, now check endTimeStr
				long t1 = DEFAULT_START_TIME;
				if (!startTimeStr.equals("")) {
					cal.clear();
					cal.set(Integer.parseInt(year), Integer.parseInt(month)-1, Integer.parseInt(day), 0, 0, 0);
					//System.out.println("OrgTPRTDataPlugin: parseTimeStrings t1: year=" + year + " month=" + month + " day=" + day);
					t1 = cal.getTime().getTime();
				}
	
				if (!endTimeStr.equals("")) {
					st = new StringTokenizer(endTimeStr, "/ \\");
					if (st.hasMoreTokens())
						month = st.nextToken();
					else
						validDate = false;
					if (st.hasMoreTokens())
						day = st.nextToken();
					else
						validDate = false;
					if (st.hasMoreTokens())
						year = st.nextToken();		
					else
						validDate = false;
				}

				if (validDate) {	// endTimeStr also has all the components, change values of startTime and endTime from their defaults
					long t2 = DEFAULT_END_TIME;
					if (!endTimeStr.equals("")) {
						cal.clear();
						cal.set(Integer.parseInt(year), Integer.parseInt(month)-1, Integer.parseInt(day), 0, 0, 0);
						t2 = cal.getTime().getTime();
						//System.out.println("OrgTPRTDataPlugin: parseTimeStrings t2: year=" + year + " month=" + month + " day=" + day);
					}

					startTime = t1;
					endTime = t2;
					//System.out.println("OrgTPRTDataPlugin: parseTimeStrings startTime=" + new Date(startTime));
					//System.out.println("OrgTPRTDataPlugin: parseTimeStrings endTime=" + new Date(endTime));
				}
			}
		}

		return new long[] {startTime, endTime};
    }
    catch (Exception e)
    {
		e.printStackTrace();
		// System.out.println("returning default times");
		return new long[] {DEFAULT_START_TIME, DEFAULT_END_TIME};
    }
  }

  //create the RFS task to be sent to myself which will result in an asset transfer
  // of the copyofmyself being sent to the cluster I am supporting.
  protected NewTask createReportTask(Organization reportingOrg, 
                                     OrganizationAdapter sendto,
                                     Collection roles,
                                     long startTime,
                                     long endTime) {
    NewTask reportTask = getFactory().newTask();
    reportTask.setDirectObject(reportingOrg);

    Vector prepPhrases = new Vector(2);
    NewPrepositionalPhrase newpp = getFactory().newPrepositionalPhrase();
    newpp.setPreposition(Constants.Preposition.FOR);
    newpp.setIndirectObject(sendto);
    prepPhrases.add(newpp);

    newpp = getFactory().newPrepositionalPhrase();
    newpp.setPreposition(Constants.Preposition.AS);
    newpp.setIndirectObject(roles);
    prepPhrases.add(newpp);
    reportTask.setPrepositionalPhrases(prepPhrases.elements());

    reportTask.setPlan(getFactory().getRealityPlan());
    reportTask.setSource(getMessageAddress());

    AspectValue startTAV = 
      TimeAspectValue.create(AspectType.START_TIME, startTime);
    ScoringFunction startScoreFunc = 
      ScoringFunction.createStrictlyAtValue(startTAV);
    Preference startPreference = 
      getFactory().newPreference(AspectType.START_TIME, startScoreFunc);

    AspectValue endTAV = 
      TimeAspectValue.create(AspectType.END_TIME, endTime);
    ScoringFunction endScoreFunc = 
      ScoringFunction.createStrictlyAtValue(endTAV);    
    Preference endPreference = 
      getFactory().newPreference(AspectType.END_TIME, endScoreFunc );

    Vector preferenceVector = new Vector(2);
    preferenceVector.addElement(startPreference);
    preferenceVector.addElement(endPreference);

    reportTask.setPreferences(preferenceVector.elements());
    
    return reportTask;
  }

   
	
  private void publish(Object o) {
    publishAdd(o);
  }


  /**
   * 
   */
  protected void ParsePrototypeFile(String clusterId, String relationship) {

    // Use the same domainname for all org assets now
    String uic = "";
    String className = "";
    String unitName = null;
    String dataItem = "";
    Organization org = null;
    int newVal;

    String filename = clusterId + "-prototype-ini.dat";
    BufferedReader input = null;
    Reader fileStream = null;

    try {
      fileStream = 
        new InputStreamReader(ConfigFinder.getInstance().open(filename));
      input = new BufferedReader(fileStream);
      StreamTokenizer tokens = new StreamTokenizer(input);
      tokens.commentChar('#');
      tokens.wordChars('[', ']');
      tokens.wordChars('_', '_');
      tokens.wordChars('<', '>');      
      tokens.wordChars('/', '/');      
      tokens.ordinaryChars('0', '9');      
      tokens.wordChars('0', '9');      

      newVal = tokens.nextToken();
      // Parse the prototype-ini file
      while (newVal != StreamTokenizer.TT_EOF) {
          if (tokens.ttype == StreamTokenizer.TT_WORD) {
            dataItem = tokens.sval;
            if (dataItem.equals("[Prototype]")) {
              tokens.nextToken();
              className = tokens.sval;
              newVal = tokens.nextToken();
            } else if (dataItem.equals("[UniqueId]")) {
              tokens.nextToken();
              // Dont use unique-id as domain name anymore, just skip it
              //utc = tokens.sval;
              newVal = tokens.nextToken();
            } else if (dataItem.equals("[UnitName]")) {
              // This field is optional 
              tokens.nextToken();
              unitName = tokens.sval;
              newVal = tokens.nextToken();
            } else if (dataItem.equals("[UIC]")) {
              if (className != null) {
                tokens.nextToken();
	      	uic = tokens.sval;

		// This is a silly fix to a dumb bug
		if (!uic.startsWith("UIC/")) {
		  uic = "UIC/" + uic;
		}

                org = (Organization)getFactory().createAsset("Organization");
                org.initRelationshipSchedule();
                org.setLocal(false);

                NewTypeIdentificationPG typeIdPG = 
                  (NewTypeIdentificationPG)org.getTypeIdentificationPG();
                typeIdPG.setTypeIdentification(UTC);

	      	NewItemIdentificationPG itemIdPG = 
                  (NewItemIdentificationPG)org.getItemIdentificationPG();
                itemIdPG.setItemIdentification(uic);
	      	// Use unitName if it occurred, else use clusterId
		if (unitName!=null) {
                  itemIdPG.setNomenclature(unitName);
		} else {
                  itemIdPG.setNomenclature(clusterId);
	      	}
		itemIdPG.setAlternateItemIdentification(clusterId);

                NewClusterPG cpg = (NewClusterPG) org.getClusterPG();
                cpg.setMessageAddress(MessageAddress.getMessageAddress(clusterId));
              } else {
                System.out.println("OrgRTDataPlugin Error: [Prototype] value is null");
              }
              newVal = tokens.nextToken();
            } else if (dataItem.equals("[Relationship]")) {
              newVal = FillOrganizationVector(org, newVal, tokens, relationship);
              // ADDED BY TOPS
            } else if (dataItem.equals("[AssignmentPG]")) {
              newVal = setAssignmentForOrganization(org, dataItem, newVal, tokens);
              // END ADDED BY TOPS
            } else if (dataItem.substring(0,1).equals("[")) {
              // We've got a property or capability
              newVal = setPropertyForOrganization(org, dataItem, newVal, tokens);
            } else {
              // if The token you read is not one of the valid
              // choices from above
              System.out.println("OrgRTDataPlugin Incorrect token: " + dataItem);
            }
          } else {
            throw new RuntimeException("Format error in \""+filename+"\".");
          }
      }

      // For each organization, the following code sets
      // CapableRoles and Relationship slots for the
      // AssignedPG property
      // It adds the property to the organization and
      // adds the organization to ccv2 collections
      NewAssignedPG assignedCap = (NewAssignedPG)getFactory().createPropertyGroup(AssignedPGImpl.class);
      Collection roles =  org.getOrganizationPG().getRoles();
      if (roles != null) {
        assignedCap.setRoles(new ArrayList(roles));
      }
      org.setAssignedPG(assignedCap);
      
      // set up this asset's available schedule
      myCalendar.set(1990, 0, 1, 0, 0, 0);
      Date start = myCalendar.getTime();
      // set the end date of the available schedule to 01/01/2010
      myCalendar.set(2010, 0, 1, 0, 0, 0);
      Date end = myCalendar.getTime();
      NewSchedule availsched = getFactory().newSimpleSchedule(start, end);
      // set the available schedule
      ((NewRoleSchedule)org.getRoleSchedule()).setAvailableSchedule(availsched);

      if (relationship.equals(GLMRelationship.SELF)) {
        Relationship selfRelationship = 
          getFactory().newRelationship(Constants.Role.SELF, org, org,
                                       ETERNITY);  
        org.getRelationshipSchedule().add(selfRelationship);
        org.setLocal(true);

        publish(org);
      	selfOrg = org;
      }

      // Closing BufferedReader
      if (input != null)
	input.close();

      //only generates a NoSuchMethodException for AssetSkeleton because of a coding error
      //if we are successul in creating it here  it then the AssetSkeletomn will end up with two copies
      //the add/search criteria in AssetSkeleton is for a Vecotr and does not gurantee only one instance of 
      //each class.  Thus the Org allocator plugin fails to recognixe the correct set of cpabilities.
      
    } catch (Exception e) {
      e.printStackTrace();
    }
  } 


  /**
   * reads time-phased support and superior relationships from a file
   */
   protected Vector parseRelationshipFile(String clusterId, String relationship) {
	
		Vector relationshipsVector = new Vector(5,10);
	    String filename = clusterId + "-relationships.ini";

		try {
			StreamTokenizer st = new StreamTokenizer(new BufferedReader(new InputStreamReader(ConfigFinder.getInstance().open(filename))));
			st.eolIsSignificant(true);
	        st.commentChar('#');
			int count = 1;
			st.nextToken();
			while (st.ttype != StreamTokenizer.TT_EOF) {
				String[] relationshipsArray = {"", "", "", "", ""};

				Vector tokenVector = new Vector(5);
				// read the next full line of input
				while (st.ttype != StreamTokenizer.TT_EOL) {
					tokenVector.addElement(st.sval);
					st.nextToken();
				}

				//System.out.println("OrgTPRTDataPlugin:"+ " obtained line: " + tokenVector);
				// now process the line
				if (tokenVector.size() == 0) {
					st.nextToken();
					count++;
					continue;
				}

				// element 0 is always Superior or Subordinate
				relationshipsArray[0] = (String)tokenVector.elementAt(0);
				// find out what type of relationship this is
				Class r_class = GLMRelationship.class;
				Field f = r_class.getField(relationshipsArray[0].toUpperCase());

				if ((GLMRelationship.SUPPORTING).equals(f.get(null))) {

					if (tokenVector.size() == 5) {
						// element 1 is clientClusterName
						relationshipsArray[1] = (String)tokenVector.elementAt(1);
						// element 2 is roles
						relationshipsArray[2] = (String)tokenVector.elementAt(2);
						// element 3 is startDate
						relationshipsArray[3] = (String)tokenVector.elementAt(3);
						// element 4 is endDate
						relationshipsArray[4] = (String)tokenVector.elementAt(4);
					}
					else {
						System.out.println("Line " + count + ": incomplete or extra information, using default start and end times for all relations");
						return null;
					}
				}
				else
				if ((GLMRelationship.SUPERIOR).equals(f.get(null))) {

					if (tokenVector.size() == 4) {
						// element 1 is subordinateClusterName
						relationshipsArray[1] = (String)tokenVector.elementAt(1);
						// element 2 is startDate
						relationshipsArray[3] = (String)tokenVector.elementAt(2);
						// element 3 is endDate
						relationshipsArray[4] = (String)tokenVector.elementAt(3);
					}
					else
					if (tokenVector.size() == 5) {
						// element 1 is subordinateClusterName
						relationshipsArray[1] = (String)tokenVector.elementAt(1);
						// element 3 is startDate
						relationshipsArray[3] = (String)tokenVector.elementAt(3);
						// element 4 is endDate
						relationshipsArray[4] = (String)tokenVector.elementAt(4);
					}
					else {
						System.out.println("Line " + count + ": extra information, using default start and end times for all relations");
						return null;
					}
				}
				else {
					System.out.println("Line " + count + ": unrecognized relationship, using default start and end times for all relations");
					return null;
				}

				// we now have all tokens necessary from a single relationship statement
				if (relationship.equals(GLMRelationship.SELF))
					relationshipsVector.addElement(relationshipsArray);
				/*
				for (int i = 0; i < relationshipsArray.length; i++)
					System.out.println("OrgTPRTDataPlugin:"+ " obtained token: " + "*"+relationshipsArray[i]+"*");
				*/

				// advance to the next line
				st.nextToken();
				count++;
			}

		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return relationshipsVector;
   }



  private Object parseExpr(String type, String arg) {
    int i;
    if ((i = type.indexOf("<")) >= 0) {
      int j = type.lastIndexOf(">");
      String ctype = type.substring(0,i);
      String etype = type.substring(i+1, j);
      Collection c = null;
      if (ctype.equals("Collection") || ctype.equals("List")) {
        c = new ArrayList();
      } else {
        throw new RuntimeException("Unparsable collection type: "+type);
      }

      Vector l = org.cougaar.util.StringUtility.parseCSV(arg);
      for (Iterator it = l.iterator(); it.hasNext();) {
        c.add(parseExpr(etype,(String) it.next()));
      }
      return c;
    } else if ((i = type.indexOf("/")) >= 0) {
      String m = type.substring(0,i);
      String mt = type.substring(i+1);
      double qty = Double.valueOf(arg).doubleValue();
      return createMeasureObject(m, qty, mt);
    } else {
      Class cl = findClass(type);

      try {
        if (cl.isInterface()) {
          // interface means try the COF
          return parseWithCOF(cl, arg);
        } else {
          Class ac = getArgClass(cl);
          Object[] args = {arg};
          Constructor cons = Reflect.getConstructor(ac,stringArgSpec);
          if (cons != null) {
            // found a constructor - use it
            return cons.newInstance(args);
          } else {
            Method fm = Reflect.getMethod(ac, "create", stringArgSpec);
            if (fm == null) {
              String n = ac.getName();
              // remove the package prefix
              n = n.substring(n.lastIndexOf('.')+1);
              fm = Reflect.getMethod(ac, "create"+n, stringArgSpec);
              if (fm == null) 
                fm = Reflect.getMethod(ac, "get"+n, stringArgSpec);
            }
            if (fm == null) {
              throw new RuntimeException("Couldn't figure out how to construct "+type);
            }
            return fm.invoke(null,args);
          }
        }
      } catch (Exception e) {
        System.err.println("OrgRTDataPlugin: Exception constructing "+type+" from \""+arg+"\":");
        e.printStackTrace();
        throw new RuntimeException("Construction problem "+e);
      }
    }
  }

  private static Class[] stringArgSpec = {String.class};

  private static Class[][] argClasses = {{Integer.TYPE, Integer.class},
                                         {Double.TYPE, Double.class},
                                         {Boolean.TYPE, Boolean.class},
                                         {Float.TYPE, Float.class},
                                         {Long.TYPE, Long.class},
                                         {Short.TYPE, Short.class},
                                         {Byte.TYPE, Byte.class},
                                         {Character.TYPE, Character.class}};
                                     
  private static Class getArgClass(Class c) {
    if (! c.isPrimitive()) return c;
    for (int i = 0; i < argClasses.length; i++) {
      if (c == argClasses[i][0])
        return argClasses[i][1];
    }
    throw new IllegalArgumentException("Class "+c+" is an unknown primitive.");
  }

  private String getType(String type) {
    int i;
    if ((i = type.indexOf("<")) > -1) { // deal with collections 
      int j = type.lastIndexOf(">");
      return getType(type.substring(0,i)); // deal with measures
    } else if ((i= type.indexOf("/")) > -1) {
      return getType(type.substring(0,i));
    } else {
      return type;
    }
  }
    

  protected Object parseWithCOF(Class cl, String val) {
    String name = cl.getName();
    int dot = name.lastIndexOf('.');
    if (dot != -1) name = name.substring(dot+1);

    try {
      // lookup method on ldmf
      Object o = callFactoryMethod(name);

      Vector svs = org.cougaar.util.StringUtility.parseCSV(val);
      // svs should be a set of strings like "slot=value" or "slot=type value"
      for (Enumeration sp = svs.elements(); sp.hasMoreElements();) {
        String ss = (String) sp.nextElement();

        int eq = ss.indexOf('=');
        String slotname = ss.substring(0, eq);
        String vspec = ss.substring(eq+1);
        
        int spi = vspec.indexOf(' ');
        Object v;
        if (spi == -1) {
          v = vspec;
        } else {
          String st = vspec.substring(0, spi);
          String sv = vspec.substring(spi+1);
          v = parseExpr(st, sv);
        }
        callSetMethod(o, slotname, v);
      }
      return o;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  private Object callFactoryMethod(String ifcname) {
    // look up a zero-arg factory method in the ldmf
    String newname = "new"+ifcname;
    
    // try the COUGAAR factory
    try {
      Class ldmfc = aldmf.getClass();
      Method fm = ldmfc.getMethod(newname,nullClassList);
      return fm.invoke(aldmf, nullArgList);
    } catch (Exception e) {
      e.printStackTrace();
    }

    // try the main factory
    try {
      Class ldmfc = getFactory().getClass();
      Method fm = ldmfc.getMethod(newname,nullClassList);
      return fm.invoke(getFactory(), nullArgList);
    } catch (Exception e) {
    }
    throw new RuntimeException ("Couldn't find a factory method for "+ifcname);
  }
  private static final Class nullClassList[] = {};
  private static final Object nullArgList[] = {};

  private void callSetMethod(Object o, String slotname, Object value) {
    Class oc = o.getClass();
    String setname = "set"+slotname;
    Class vc = value.getClass();

    try {
      Method ms[] = Reflect.getMethods(oc);
      for (int i = 0; i<ms.length; i++) {
        Method m = ms[i];
        if (setname.equals(m.getName())) {
          Class mps[] = m.getParameterTypes();
          if (mps.length == 1 &&
              mps[0].isAssignableFrom(vc)) {
            Object args[] = {value};
            m.invoke(o, args);
            return;
          }
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Couldn't find set"+slotname+" for "+o+", value "+value);
    }

    throw new RuntimeException("Couldn't find set"+slotname+" for "+o+", value "+value);
  }

  /**
   * Creates the property, fills in the slots based on what's in the prototype-ini file
   * and then sets it for (or adds it to) the organization
   */
  protected int setPropertyForOrganization(Organization org, String prop, int newVal, StreamTokenizer tokens) {
    String propertyName = prop.substring(1, prop.length()-1);
    if (org != null) {
      NewPropertyGroup property = null;
      try {
	property = (NewPropertyGroup)getFactory().createPropertyGroup(propertyName);
      } catch (Exception e) {
	System.err.println("OrgRTDataPlugin: Unrecognized keyword for a prototype-ini file: [" + propertyName + "]");
      }
      try {
	newVal = tokens.nextToken();
	String member = tokens.sval;
	String propName = "New" + propertyName;
	// Parse through the property section of the file
	while (newVal != StreamTokenizer.TT_EOF) {
	  if ((tokens.ttype == StreamTokenizer.TT_WORD) && !(tokens.sval.substring(0,1).equals("["))) {
	    newVal = tokens.nextToken();
	    String dataType = tokens.sval;
	    newVal = tokens.nextToken();
	    // Call appropriate setters for the slots of the property
            Object arg = parseExpr(dataType, tokens.sval);

            createAndCallSetter(property, propName, "set" + member, 
                                getType(dataType), arg);
	    newVal = tokens.nextToken();
	    member = tokens.sval;
	  } else {
	    // Reached a left bracket "[", want to exit block
	    break;
	  }
	} //while

	// Add the property to the organization
	try {
	  // if a setter already exists for the property (such as setTypeIdentificationPG)
	  // then use it
	  Class propertyClass = Organization.class;
	  Class parameters[] = new Class[] {PropertyGroup.class};
	  Method meth = propertyClass.getMethod("set" + propertyName, parameters);
	  Object arguments[] = new Object[] {property};
	  meth.invoke(org, arguments);
	} catch (NoSuchMethodException nsme) {
	  // else call addOtherPropertyGroup
	  org.addOtherPropertyGroup(property);
	} catch (Exception e) {
	  e.printStackTrace();
	}
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      System.err.println("OrgRTDataPlugin Error: org is null");
    }
    return newVal;
  }

  /**
   * Fills in the organization_vector with arrays of relationship, clusterName and capableroles triples.
   */
  protected int FillOrganizationVector(Organization org, int newVal, StreamTokenizer tokens, String relationship) {
    organization_vector.removeAllElements(); // Clear out the organization_vector

    int x = 0;
    if (org != null) {
      try {
	while (newVal != StreamTokenizer.TT_EOF) {
	  String organization_array[] = new String[5]; // An array of relationship, clusterName and capableroles triples
	  newVal = tokens.nextToken();
	  // Parse [Relationship] part of prototype-ini file
	  if ((tokens.ttype == StreamTokenizer.TT_WORD) && !(tokens.sval.substring(0,1).equals("["))) {
	    organization_array[0] = tokens.sval;
	    newVal = tokens.nextToken();
	    organization_array[1] = tokens.sval;
	    newVal = tokens.nextToken();
	    organization_array[2] = tokens.sval;
	    // Only add to the organization_vector if
	    // the relationship is SELF
	    if (relationship.equals(GLMRelationship.SELF))
	      organization_vector.addElement(organization_array);
	  } else {
	    // Reached a left bracket "[", want to exit block
	    break;
	  }
	  x++;
	} //while
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      System.out.println("OrgRTDataPlugin Error: org is null");
    }

    return newVal;
  }

  /**
   * Returns the integer value for the appropriate
   * unitOfMeasure field in the measureClass
   */
  protected int getMeasureUnit(String measureClass, String unitOfMeasure) {
    try {
      String fullClassName = "org.cougaar.planning.ldm.measure." + measureClass;
      Field f = Class.forName(fullClassName).getField(unitOfMeasure);
      return f.getInt(null);
    } catch (Exception e) {
      System.err.println("OrgRTDataPlugin Exception: for measure unit: " + 
                         unitOfMeasure);
      e.printStackTrace();
    }
    return -1;
  }

  /**
   * Returns a measure object which is an instance of className and has
   * a quantity of unitOfMeasure
   */
  protected Object createMeasureObject(String className, double quantity, String unitOfMeasure) {
    try {
      Class classObj = Class.forName("org.cougaar.planning.ldm.measure." + className);
      String methodName = "new" + className;
      Class parameters[] = {double.class, int.class};
      Method meth = classObj.getMethod(methodName, parameters);
      Object arguments[] = {new Double(quantity), new Integer(getMeasureUnit(className, unitOfMeasure))};
      return meth.invoke(classObj, arguments); // static method call
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private static HashMap classes;
  protected static final Collection packages;

  static {
    // initialize packages:
    packages = new ArrayList();
    packages.add("org.cougaar.glm.ldm.asset");
    packages.add("org.cougaar.glm.ldm.plan");
    packages.add("org.cougaar.glm.ldm.oplan");
    packages.add("org.cougaar.glm.ldm.policy");


    packages.add("org.cougaar.planning.ldm.measure");
    packages.add("org.cougaar.planning.ldm.plan");
    packages.add("org.cougaar.planning.ldm.asset");
    packages.add("org.cougaar.planning.ldm.oplan");

    packages.add("java.lang");  // extras for fallthrough
    packages.add("java.util");

    // initialize the classmap with some common ones
    classes = new HashMap();

    classes.put("MessageAddress", MessageAddress.class);

    // precache some builtins
    classes.put("long", Long.TYPE);
    classes.put("int", Integer.TYPE);
    classes.put("integer", Integer.TYPE);
    classes.put("boolean", Boolean.TYPE);
    classes.put("float", Float.TYPE);
    classes.put("double", Double.TYPE);
    // and some java.lang
    classes.put("Double", Double.class);
    classes.put("String", String.class);
    classes.put("Integer", Integer.class);
    // and some java.util
    classes.put("Collection", Collection.class);
    classes.put("List", List.class);
    // COUGAAR-specific stuff will be looked for
  }

  private Class findClass(String name) {
    synchronized (classes) {
      Class c = (Class) classes.get(name);
      // try the cache
      if (c != null) return c;

      for (Iterator i = packages.iterator(); i.hasNext();) {
        String pkg = (String) i.next();
        try {                   // Oh so ugly!
          c = Class.forName(pkg+"."+name);
          if (c != null) {        // silly
            classes.put(name, c);
            return c;
          }
        } catch (ClassNotFoundException e) {}; // sigh
      }
      throw new RuntimeException("Could not find a class for '"+name+"'.");
    }
  }

  /**
   * Creates and calls the appropriate "setter" method for the classInstance
   * which is of type className.
   */
  protected void createAndCallSetter(Object classInstance, String className, String setterName, String type, Object value) {
    Class parameters[] = new Class[1];
    Object arguments[] = new Object[] {value};
    
    try {
      parameters[0] = findClass(type);
      Class propertyClass = findClass(className);
      //Method meth = propertyClass.getMethod(setterName, parameters);
      Method meth = findMethod(propertyClass, setterName, parameters);
      meth.invoke(classInstance, arguments);
    } catch (Exception e) {
      System.err.println("OrgRTPlugin Exception: createAndCallSetter("+classInstance.getClass().getName()+", "+className+", "+setterName+", "+type+", "+value+" : " + e);
      e.printStackTrace();
    }
  }

  private static Method findMethod(Class c, String name, Class params[]) {
    Method ms[] = Reflect.getMethods(c);
    int pl = params.length;
    for (int i = 0; i < ms.length; i++) {
      Method m = ms[i];
      if (name.equals(m.getName())) {
        Class mps[] = m.getParameterTypes();
        if (mps.length == pl) {
          int j;
          for (j = 0; j < pl; j++) {
            if (!(mps[j].isAssignableFrom(params[j]))) 
              break;            // j loop
          }
          if (j==pl)            // all passed
            return m;
        }
      }
    }
    return null;
  }
  
  /** 
   * This is not very straight forward due to the way classes were set up
   * The goal is to add a GeolocLocation to an Organization
   * 1) Organizations have an AssignmentPG
   * 2) AssignmentPG contains a Facility object, which is the super
   *    class of the TransportationNode object
   * 3) TransportationNode objects have a PositionPG
   * 4) PositionPG contains a Position object, which is the super
   *    class of the GeolocLocation object
   */

  /**
   * Creates the Position, fills in the slots based on what's in the prototype-ini file
   * and then sets it for (or adds it to) the organization
   */
  protected int setAssignmentForOrganization(Organization org, String prop, int newVal, StreamTokenizer tokens) {
    if (org != null) {
      NewGeolocLocation geoloc = aldmf.newGeolocLocation();
      String geolocClassName = "NewGeolocLocation";
  
      try {
	newVal = tokens.nextToken();
	String member = tokens.sval;
	// Parse through the property section of the file
	while (newVal != StreamTokenizer.TT_EOF) {
	  if ((tokens.ttype == StreamTokenizer.TT_WORD) && !(tokens.sval.substring(0,1).equals("["))) {
	    newVal = tokens.nextToken();
	    String dataType = tokens.sval;

	    newVal = tokens.nextToken();
	    // Call appropriate setters for the slots of the property
	    if (dataType.equals("String")) {
	      createAndCallSetter(geoloc, geolocClassName, "set"+member, dataType, tokens.sval);
	    } else {
	      try {
		// If it's a measure class, create the appropriate measure object,
		// and call setter with that as one of the parameters
		Class.forName("org.cougaar.planning.ldm.measure." + dataType);
		double qty =
		  ((tokens.ttype == StreamTokenizer.TT_NUMBER) ?
		   tokens.nval :
		   Double.parseDouble(tokens.sval));
		newVal = tokens.nextToken();
		Object o = createMeasureObject(dataType, qty, tokens.sval);
		createAndCallSetter(geoloc, geolocClassName, "set" + member, dataType, o);
	      } catch (Exception e) {
		// else it is not a datatype we handle
		System.err.println("OrgRTDataPlugin: Incorrect datatype: " + 
                                   dataType);
		e.printStackTrace();
	      }
	    }
	    newVal = tokens.nextToken();
	    member = tokens.sval;
	  } else {
	    // Reached a left bracket "[", want to exit block
	    break;
	  }
	} //while
      
 	// Add the property to the organization
	NewAssignmentPG assignProp = (NewAssignmentPG)org.getAssignmentPG();
	TransportationNode tn = (TransportationNode)assignProp.getHomeStation();
	if (tn == null){ // There are no NewFacility or NewTransportationNode objects
          tn = (TransportationNode)getFactory().createAsset(TransportationNode.class);
	}
	NewPositionPG posProp = (NewPositionPG) getFactory().createPropertyGroup(PositionPGImpl.class);
	posProp.setPosition((NewPosition)geoloc);
	tn.setPositionPG(posProp);
	assignProp.setHomeStation((Facility)tn);
	org.setAssignmentPG(assignProp);

        // set up the home location element of the locationpg
        {
          // get the pg
          LocationSchedulePG lspg = org.getLocationSchedulePG();
          if (lspg == null) {
            org.setLocationSchedulePG(lspg = new LocationSchedulePGImpl());
          }
    
          // get the schedule
          Schedule ls = lspg.getSchedule();
          if (ls == null) {
            ls = theLDMF.newLocationSchedule(EmptyEnumeration.getEnumeration());
            ((NewLocationSchedulePG)lspg).setSchedule(ls);
          }

          // now that we have it, lock it so nobody bashes it
          synchronized (ls) {
            /*
            ls.add(new LocationScheduleElementImpl(TimeSpan.MIN_VALUE, 
                                                   TimeSpan.MAX_VALUE,
                                                   geoloc));
            */
            ls.add(new HomeLocationScheduleElement(geoloc));
          }
        }
      } catch (Exception e) {
	e.printStackTrace();
      }
    } else {
      System.err.println("OrgRTDataPlugin Error: org is null");
    }
    return newVal;
  }

  public static class HomeLocationScheduleElement extends TaggedLocationScheduleElement {
    public HomeLocationScheduleElement(Location l) {
      super(TimeSpan.MIN_VALUE, TimeSpan.MAX_VALUE,l);
    }
    /** @return the string "HOME" to indicate that this is the home location
     * of the organization.
     **/
    public Object getOwner() { return "HOME"; }
    
  }

  private static class TrivialTimeSpan implements TimeSpan {
    long myStart;
    long myEnd;

    public TrivialTimeSpan(long start, long end) {
      myStart = start;
      myEnd = end;
    }

    public long getStartTime() {
      return myStart;
    }

    public long getEndTime() {
      return myEnd;
    }
  }

}
