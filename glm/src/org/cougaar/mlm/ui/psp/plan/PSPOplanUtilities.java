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
 
package org.cougaar.mlm.ui.psp.plan;

import java.util.*;

import org.cougaar.core.blackboard.CollectionSubscription;
import org.cougaar.core.blackboard.Subscription;
import org.cougaar.planning.ldm.policy.*;
import org.cougaar.lib.planserver.*;
import org.cougaar.util.UnaryPredicate;

import org.cougaar.glm.ldm.*;import org.cougaar.glm.ldm.*;import org.cougaar.glm.*;
import org.cougaar.glm.ldm.asset.*;
import org.cougaar.glm.ldm.oplan.*;
import org.cougaar.glm.ldm.plan.*;
import org.cougaar.glm.ldm.policy.*;

/**
  * This is a demonstration/proof-of-concept PSP which illustrates a number
  * of techniques based on PSP (server-side) layout and HTML to construct
  * a "multi-pane" Task View.
  *
  *
  * Important to note how cross-references between Cluster LPSs are specified in HTML.  No longer will
  * browser client directly speak to any LPS other than its host.  Task drill-down and
  * Task views to other clusters are handled via redirection at server rather than
  * URL link from client.
  *
  * This is required by Netscape browsers to work with multiple-form/multipe-URL data
  * model.  Netscape browsers insist on opening new window when Host changes.
  *
  **/

public class PSPOplanUtilities {
  
  // Predicate for all Oplans
  private static UnaryPredicate allOplansPredicate = new UnaryPredicate() {
    public boolean execute(Object o) {          
      return (o instanceof Oplan);
    }};

  // Predicate for all Cincs
  //private static UnaryPredicate allCincsPredicate = new UnaryPredicate() {
  //  public boolean execute(Object o) {
  //    return (o instanceof Cinc);
  //  }};

  // Predicate for all self Organizations
  private static UnaryPredicate allSelfOrganizationsPredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
      if (o instanceof Organization) {
        return ((Organization) o).isSelf();
      }
      return false;
    }};

  // Predicate for all OrgActivities
  private static class OrgActivitiesPredicate implements UnaryPredicate {
    Collection orgIDs;
    public OrgActivitiesPredicate(Collection orgIDs) {
      if (orgIDs == null) {
        this.orgIDs = null;     // No org filter
      } else if (orgIDs instanceof HashSet || orgIDs instanceof TreeSet) {
        this.orgIDs = orgIDs;
      } else if (orgIDs.size() == 1) {
        orgIDs = Collections.singleton(orgIDs.iterator().next());
      } else {
        this.orgIDs = new HashSet(orgIDs);
      }
    }
    public boolean execute(Object o) {
      if (o instanceof OrgActivity) {
        if (orgIDs == null) return true;
        String orgID = ((OrgActivity) o).getOrgID();
        return orgIDs.contains(orgID);
      }
      return false;
    }
  }

  // Prdicate for all OrgRelation
  private static UnaryPredicate allOrgRelationsPredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
      return (o instanceof OrgRelation);
    }};

  
  // Predicate for all ForcePackages
  private static UnaryPredicate allForcePackagesPredicate = new UnaryPredicate (){
    public boolean execute(Object o) {
      return (o instanceof ForcePackage);
    }};

  // Predicate for all TheaterInfos
/*  private static UnaryPredicate allTheaterInfosPredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
      return (o instanceof TheaterInfo);
    }}; */
  
  // Predicate for all Timespans
  private static UnaryPredicate allTimeSpansPredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
      return (o instanceof TimeSpan);
    }};

  // Predicate for all Policies
  private static UnaryPredicate allPoliciesPredicate = new UnaryPredicate() {
    public boolean execute(Object o) {
      return (o instanceof Policy);
    }};

  /*************************************************************************************
  * 
  **/
  private String getClusterName(String uuid) {
    int i = uuid.indexOf("/");
    return uuid.substring(0,i);
  }
    
  /*************************************************************************************/
    /** @deprecated There is no _single_ oplan **/
  public static Oplan getOplan(PlanServiceContext psc) {
      Collection oplans = getOplans(psc);
      if (oplans.isEmpty()) return null;
      return (Oplan) oplans.iterator().next();
  }
      
  public static Collection getOplans(PlanServiceContext psc) {
    return psc.getServerPlugInSupport().queryForSubscriber(allOplansPredicate);
  }

        /*************************************************************************************
         * 
         **/
 // public static Collection getCincs(PlanServiceContext psc) {
 //   return psc.getServerPlugInSupport().queryForSubscriber(allCincsPredicate);
 // }

        /*************************************************************************************
         * 
         **/
  public static Collection getSelfOrganizationIDs(PlanServiceContext psc) {
    Collection orgs = psc.getServerPlugInSupport().queryForSubscriber(allSelfOrganizationsPredicate);
    Collection result;
    if (orgs.size() == 1) {
      return Collections.singleton(((Organization) orgs.iterator().next()).getItemIdentificationPG().getItemIdentification());
    }
    result = new HashSet((int) (orgs.size() * 1.3));
    for (Iterator i = orgs.iterator(); i.hasNext(); ) {
      result.add(((Organization) i.next()).getItemIdentificationPG().getItemIdentification());
    }
    return result;
  }

        /*************************************************************************************
         * 
         **/
  public static Collection getOrgActivities(PlanServiceContext psc, String orgID) {
    return getOrgActivities(psc, Collections.singleton(orgID));
  }

  public static Collection getOrgActivities(PlanServiceContext psc) {
    return getOrgActivities(psc, (Collection) null);
  }

  public static Collection getOrgActivities(PlanServiceContext psc, Collection orgIDs) {
    return psc.getServerPlugInSupport().queryForSubscriber(new OrgActivitiesPredicate(orgIDs));
  }

        /*************************************************************************************
         * 
         **/
  public static Collection getOrgRelations(PlanServiceContext psc) {
    return psc.getServerPlugInSupport().queryForSubscriber(allOrgRelationsPredicate);
  }

  /*************************************************************************************
         * 
         **/
  public static Collection getForcePackages(PlanServiceContext psc) {
    return psc.getServerPlugInSupport().queryForSubscriber(allForcePackagesPredicate);
  }

        /*************************************************************************************
         * 
         **/
/*  public static Collection getTheaterInfos(PlanServiceContext psc) {
    return psc.getServerPlugInSupport().queryForSubscriber(allTheaterInfosPredicate);
  } */

        /*************************************************************************************
         * 
         **/
  public static Collection getTimeSpans(PlanServiceContext psc) {
    return psc.getServerPlugInSupport().queryForSubscriber(allTimeSpansPredicate);
  }


        /*************************************************************************************
         * 
         **/
  public static Collection getPolicies(PlanServiceContext psc) {
    return psc.getServerPlugInSupport().queryForSubscriber(allPoliciesPredicate);
  }


        /*************************************************************************************
         * 
         **/
        public static String openDoc(String title) {
    return "<HTML><HEAD><TITLE>" + title + "</TITLE></HEAD><BODY>";
        }

        /*************************************************************************************
         * 
         **/
        public static String closeDoc() {
    return "</BODY></HTML>";
        }

        /*************************************************************************************
         * 
         **/
        public static String openTable() {
                return openTable("", "");
        }

        /*************************************************************************************
         * 
         **/
        public static String openTable(String split) {
//              return "<TABLE BORDER=1 WIDTH=100%><TH WIDTH=20% VALIGN=TOP ALIGN=RIGHT>" + colHeader1
//                      + "</TH><TH ALIGN=LEFT>" + colHeader2 + "</TH>";
                return "<TABLE BORDER=1 WIDTH=100%><TH WIDTH="+ split + "></TH><TH></TH>";
        }

        /*************************************************************************************
         * 
         **/
        public static String openTable(String colHeader1, String colHeader2) {
//              return "<TABLE BORDER=1 WIDTH=100%><TH WIDTH=20% VALIGN=TOP ALIGN=RIGHT>" + colHeader1
//                      + "</TH><TH ALIGN=LEFT>" + colHeader2 + "</TH>";
                return "<TABLE BORDER=1 WIDTH=100%><TH>" + colHeader1   + "</TH><TH>" + colHeader2 + "</TH>";
        }

        /*************************************************************************************
         * 
         **/
        public static String openTable(String colHeader1, String colHeader2, String colHeader3, 
      String colHeader4, String colHeader5, String colHeader6) {
                return "<TABLE BORDER=1 WIDTH=100%>"
/*
                  + "<TH VALIGN=TOP>" + colHeader1 + "</TH>"
                        + "<TH VALIGN=TOP>" + colHeader2 + "</TH>"
                        + "<TH VALIGN=TOP>" + colHeader3 + "</TH>"
                        + "<TH VALIGN=TOP>" + colHeader4 + "</TH>";
*/
                  + "<TH WIDTH=25% VALIGN=TOP>" + colHeader1 + "</TH>"
                        + "<TH WIDTH=25% VALIGN=TOP>" + colHeader2 + "</TH>"
                        + "<TH WIDTH=25% VALIGN=TOP>" + colHeader3 + "</TH>"
                        + "<TH WIDTH=25% VALIGN=TOP>" + colHeader4 + "</TH>"
                        + "<TH WIDTH=25% VALIGN=TOP>" + colHeader5 + "</TH>"
                        + "<TH WIDTH=25% VALIGN=TOP>" + colHeader6 + "</TH>";

        }
        
        /*************************************************************************************
         * 
         **/
   public static String openTable(String colHeader1, String colHeader2, String colHeader3, 
      String colHeader4) {
                return "<TABLE BORDER=1 WIDTH=100%>"
/*
                  + "<TH VALIGN=TOP>" + colHeader1 + "</TH>"
                        + "<TH VALIGN=TOP>" + colHeader2 + "</TH>"
                        + "<TH VALIGN=TOP>" + colHeader3 + "</TH>"
                        + "<TH VALIGN=TOP>" + colHeader4 + "</TH>";
*/
                  + "<TH WIDTH=25% VALIGN=TOP>" + colHeader1 + "</TH>"
                        + "<TH WIDTH=25% VALIGN=TOP>" + colHeader2 + "</TH>"
                        + "<TH WIDTH=25% VALIGN=TOP>" + colHeader3 + "</TH>"
                        + "<TH WIDTH=25% VALIGN=TOP>" + colHeader4 + "</TH>";


        }       

        /*************************************************************************************
         * 
         **/
        public static String closeTable() {
                return "</TABLE>";
        }

        /*************************************************************************************
         * 
         **/
        public static String openTOC(String title) {
                return "<H4><U>" + title + "</U></H4><UL>";
        }

        /*************************************************************************************
         * 
         **/
        public static String closeTOC() {
                return "</UL>";
        }

        /*************************************************************************************
         * 
         **/
        public static String writeTOCSection(String sectionName) {
                return "<LI><B>" + sectionName + "</B>";
        }

        /*************************************************************************************
         * 
         **/
        public static String writeTOCSection(String sectionName, String url, String target) {
                return "<LI><B><A HREF=\"" + url + "\" TARGET=\"" + target + "\">" + sectionName + "</A></B>";
        }

        /*************************************************************************************
         * 
         **/
        public static String writeTOCListItem(String sectionName) {
                return "<LI>" + sectionName;
        }

        /*************************************************************************************
         * 
         **/
        public static String writeTOCListItem(String sectionName, String url, String target) {
                return "<LI><A HREF=\"" + url + "\" TARGET=\"" + target + "\">" + sectionName + "</A>";
        }

        /*************************************************************************************
         * 
         **/
        public static String sectionHeader(String headingName) {
                return "<A NAME=\"" + headingName + "\"><H2>" + headingName + "</H2></A>";
        }

        /*************************************************************************************
         * 
         **/
  public static String writeProperty(Object key, Object value) {
                return "<TR><TD VALIGN=TOP ALIGN=RIGHT WIDTH=5%><B>" + key + "</B></TD><TD>" + value + "</TD></TR>";
        }

        /*************************************************************************************
         * 
         **/
  public static String writeTableRow(Object key, Object value) {
                return "<TR><TD VALIGN=TOP>" + key + "</TD><TD>" + value + "</TD></TR>";
        }

        /*************************************************************************************
         * 
         **/
  public static String writeTableRow(Object key, Object value1, Object value2, Object value3) {
                return "<TR><TD VALIGN=TOP>" + key      + "</TD>"
                + "<TD VALIGN=TOP>" + value1 + "</TD>"
                + "<TD VALIGN=TOP>" + value2 + "</TD>"
                + "<TD VALIGN=TOP>" + value3 + "</TD></TR>";
        }

        /*************************************************************************************
         * 
         **/
  public static String writeTableRow(Object key, Object value1, Object value2, Object value3, Object value4, Object value5) {
                return "<TR><TD VALIGN=TOP>" + key      + "</TD>"
                + "<TD VALIGN=TOP>" + value1 + "</TD>"
                + "<TD VALIGN=TOP>" + value2 + "</TD>"
                + "<TD VALIGN=TOP>" + value3 + "</TD>"
                + "<TD VALIGN=TOP>" + value1 + "</TD>"
                + "<TD VALIGN=TOP>" + value2 + "</TD>"
                + "<TD VALIGN=TOP>" + value3 + "</TD></TR>";
        }

        /*************************************************************************************
         * 
         **/
        public static String writeTable(String title) {
                return "<TR><TD VALIGN=TOP ALIGN=RIGHT><B>" + title + "</B></TD><TD>";
        }

        /*************************************************************************************
         * 
         **/
        public static String writeTimeSpan(TimeSpan ts) {
/*              if (ts != null) {
                        Integer startDelta = new Integer(ts.getStartDelta());
                        Integer endDelta = new Integer(ts.getEndDelta()); */
                        
/*                      return openTable("Start Delta", "Start Time", "End Delta", "End Time") 
                                + writeTableRow(startDelta, ts.getStartTime(), endDelta, ts.getEndTime())
                                + closeTable();
                } */
                        //else 
                        return "null";
        }

        /*************************************************************************************
         * 
         **/
/*      public static String writeGeoLoc(GeolocLocationImpl g) {
                if (g != null) {                                
                        return openTable("Code", "Name", "Latitiude", "Longitude") 
                                + writeTableRow(g.getGeolocCode(), g.getName(), g.getLatitude().toString(), g.getLongitude().toString())
                                + closeTable();
                }
                        else return "null";
        } */

        /*************************************************************************************
         * 
         **/
//      public static String writeCINCs(Iterator iter) {
//              StringBuffer buffer = new StringBuffer();
//              Cinc c;

//              if (iter != null) {
//                      buffer.append(openTable("Name", "Role"));
//                      while (iter.hasNext()){
//                              c = (Cinc)iter.next();
//                              buffer.append(writeTableRow(c.getName(), c.getRole()));
//                      }
//                      buffer.append(closeTable());
//                      return buffer.toString();
//              }
//              else
//                      return "null";
//      }

        /*************************************************************************************
         * 
         **/
/* public static String writeTheaterInfos(Iterator iter) {
                StringBuffer buffer = new StringBuffer();
                TheaterInfo ti;

                if (iter != null) {
                        buffer.append(openTable("20"));
                        while (iter.hasNext()){
                                ti = (TheaterInfo)iter.next();

        buffer.append(writeProperty("ID", ti.getTheaterID()));
        buffer.append(writeProperty("Terrain Type", ti.getTerrainType()));
        buffer.append(writeProperty("Season", ti.getSeason()));                                         
        buffer.append(writeProperty("EnemyForceType", ti.getEnemyForceType()));
        buffer.append(writeProperty("HNSForWater", new Boolean(ti.getHNSForWater())));
        buffer.append(writeProperty("HNSPOL", new Boolean(ti.getHNSPOL())));
        buffer.append(writeProperty("HNSPOLCapacity", ti.getHNSPOLCapacity()));
        buffer.append(writeProperty("HNSWaterCapability", ti.getHNSWaterCapability()));
        buffer.append(writeProperty("TransportInfo", writeTransportInfos(ti.getTransportInfo().getPOD())));
        buffer.append(writeProperty("DFSP", writeDFSPs(ti.getDFSP())));

                        }
                        buffer.append(closeTable());
                        return buffer.toString();
                }
                else
                        return "null";
  } */

        /*************************************************************************************
         * 
         **/
        public static String writeForcePackages(Iterator iter) {
                StringBuffer buffer = new StringBuffer();
                ForcePackage fp;

                if (iter != null) {
                        buffer.append(openTable("20"));
                        while (iter.hasNext()){
                                fp = (ForcePackage)iter.next();

        buffer.append(writeProperty("ID", fp.getForcePackageId()));
        buffer.append(writeProperty("Timespan", writeTimeSpan(fp.getTimeSpan())));
//        buffer.append(writeProperty("Relation", writeOrgRelations(fp.getOrgRelation())));

      }                 
                        buffer.append(closeTable());
                        return buffer.toString();
                }
                else
                        return "null";
  }

        /*************************************************************************************
         * 
         **/
//      public static String writeOrganizations(Iterator iter) {
//              StringBuffer buffer = new StringBuffer();
//              Organization o;

//              if (iter != null) {
//                      buffer.append(openTable("20"));
//                      while (iter.hasNext()){
//                              o = (Organization)iter.next();

//        buffer.append(writeProperty("Id", o.getOrgId()));
//        buffer.append(writeProperty("OrgActivities", writeOrgActivities(o.getOrgActivities())));
//        buffer.append(writeProperty("OrgRelation", writeOrgRelations(o.getOrgRelation())));

//      }                       
//                      buffer.append(closeTable());
//                      return buffer.toString();
//              }
//              else
//                      return "null";
//  }

        /*************************************************************************************
         * 
         **/
        public static String writePolicies(Iterator iter) {
                StringBuffer buffer = new StringBuffer();
                Policy p;

                if (iter != null) {
                        buffer.append(openTable("20"));
                        while (iter.hasNext()){
                                p = (Policy)iter.next();

        buffer.append(writeProperty("Name", p.getName()));
        buffer.append(writeProperty("Type", p.getClass().getName()));
        buffer.append(writeProperty("Rules", writeRuleParameters(p.getRuleParameters())));

      }                 
                        buffer.append(closeTable());
                        return buffer.toString();
                }
                else
                        return "null";
  }

        /*************************************************************************************
         * 
         **/
        public static String writeTransportInfos(Iterator iter) {
                StringBuffer buffer = new StringBuffer();
                POD p;

                if (iter != null) {
                        buffer.append(openTable("Type", "Value"));
                        while (iter.hasNext()) {          
                                p = (POD)iter.next();
                                buffer.append(writeTableRow(p.getType(), p.getValue()));
                        }
                        buffer.append(closeTable());
                        return buffer.toString();
                }
                else
                        return "null";          
        }

        /*************************************************************************************
         * 
         **/
        public static String writeDFSPs(Iterator iter) {
                StringBuffer buffer = new StringBuffer();
                DFSP d;

                if (iter != null) {
                        buffer.append(openTable("Name", "GeoLoc"));
                        while (iter.hasNext()) {
                                d = (DFSP)iter.next();
                                
//                              GeolocLocationImpl g = d.getGeoLoc();
                                
//                          buffer.append(writeTableRow(d.getName(), writeGeoLoc(g)));
                
                        }
                        buffer.append(closeTable());
                        return buffer.toString();
                }
                else
                        return "null";          
        }

        /*************************************************************************************
         * 
         **/
        public static String writeOrgRelations(Iterator iter) {
                StringBuffer buffer = new StringBuffer();
                OrgRelation o;

                if (iter != null) {
                        buffer.append(openTable("AssignedRole", "Relationship", "OtherOrgID", "Timespan"));
                        while (iter.hasNext()) {          
                                o = (OrgRelation)iter.next();
                                buffer.append(writeTableRow(o.getAssignedRole(), o.getRelationType(), o.getOtherOrgId(), writeTimeSpan(o.getTimeSpan())));
/*
                                buffer.append(writeProperty("AssignedRole", o.getAssignedRole()));
                                buffer.append(writeProperty("OtherOrgId", o.getOtherOrgId()));
                                buffer.append(writeProperty("RelationType", o.getRelationType()));
                                buffer.append(writeProperty("Timespan", writeTimeSpan(o.getTimeSpan())));
*/
                        }
                        buffer.append(closeTable());
                        return buffer.toString();
                }
                else
                        return "null";          
        }

        /*************************************************************************************
         * 
         **/
        public static String writeDeploymentRequirements(Iterator iter) {
                StringBuffer buffer = new StringBuffer();


                if (iter != null) {
                        buffer.append(openTable());
                        
                    /* Iterator GeoLocs = iter.getGeoLoc();
                        GeolocLocationImpl g;
                        
                        while (GeoLocs.hasNext()) {             
                                g = (GeolocLocationImpl)GeoLocs.next();
                                buffer.append(writeTableRow("GeoLoc", writeGeoLoc(g)));
                        }
                        buffer.append(closeTable());
                        return buffer.toString(); */
                        return "";
                }
                else
                        return "null";          
        }

        /*************************************************************************************
         * 
         **/
        public static String writeOrgActivities(Iterator iter) {
                StringBuffer buffer = new StringBuffer();
                OrgActivity o;

                if (iter != null) {
                        buffer.append(openTable());
                        while (iter.hasNext()) {          
                                o = (OrgActivity)iter.next();
                                buffer.append(writeProperty("ActivityName", o.getActivityName()));
                                buffer.append(writeProperty("ActivityType", o.getActivityType()));
                                buffer.append(writeProperty("OpTempo", o.getOpTempo()));
                                buffer.append(writeProperty("TimeSpan", writeTimeSpan(o.getTimeSpan())));
                                buffer.append(writeProperty("Items", writeKeyPairs(o.getItems())));
//                              buffer.append(writeProperty("GeoLoc", writeGeoLoc(o.getGeoLoc())));

                        }
                        buffer.append(closeTable());
                        return buffer.toString();
                }
                else
                        return "null";          
        }

        /*************************************************************************************
         * 
         **/
//      public static String writeGeoLocs(Iterator iter) {
//              StringBuffer buffer = new StringBuffer();
//              GeolocLocationImpl g;

//              if (iter != null) {
//                      buffer.append(openTable());                     
//                      while (iter.hasNext()) {          
//                              g = (GeolocLocationImpl)iter.next();
//                              buffer.append(writeTableRow("GeoLoc", writeGeoLoc(g)));
//                      }
//                      buffer.append(closeTable());
//                      return buffer.toString(); 
//              }
//              else
//                      return "null";          
//      }


        /*************************************************************************************
         * 
         **/
        public static String writeKeyPairs(Map hmap) {
                StringBuffer buffer = new StringBuffer();
                Object key, value;

                if (hmap != null) {
                        Iterator it = hmap.keySet().iterator();
                        if (it != null) {
                                buffer.append(openTable("Key", "Value"));               
                                while (it.hasNext()) {
                                        key = it.next();
                                        value = hmap.get(key);
                                        buffer.append(writeTableRow(key, value));
                                }
                                buffer.append(closeTable());                            
                                return buffer.toString();
                        }
                        else
                                return "null";          
                }
                else
                        return "null";                          
        }

        /*************************************************************************************
         * 
         **/
        public static String writeRuleParameters(RuleParameter[] rules) {
                StringBuffer buffer = new StringBuffer();
                Object key, value;

                if (rules != null) {
                        buffer.append(openTable("Name", "Value"));              
                        for (int i = 0; i < rules.length; i ++)
        buffer.append(writeTableRow(rules[i].getName(), stripEnds(rules[i].toString())));
                        buffer.append(closeTable());                            
                        return buffer.toString();
                }
                else
                        return "null";                          
        }

        /*************************************************************************************
         * 
         **/
        private static String stripEnds(String source) {
    return (source.substring(2, (source.length() - 1)));
  }

}
