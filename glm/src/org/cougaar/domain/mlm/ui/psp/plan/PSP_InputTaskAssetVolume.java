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
 
package org.cougaar.domain.mlm.ui.psp.plan;

import java.io.IOException;
import java.io.PrintStream;
import java.text.*;
import java.util.*;

import org.cougaar.core.cluster.CollectionSubscription;
import org.cougaar.core.cluster.Subscription;
import org.cougaar.domain.planning.ldm.asset.*;
import org.cougaar.domain.planning.ldm.measure.*;
import org.cougaar.domain.planning.ldm.plan.*;
import org.cougaar.core.society.UID;
import org.cougaar.lib.planserver.*;
import org.cougaar.core.util.*;
import org.cougaar.util.*;

import org.cougaar.domain.glm.ldm.*;import org.cougaar.domain.glm.ldm.*;import org.cougaar.domain.glm.*;
import org.cougaar.domain.glm.ldm.asset.*;
import org.cougaar.domain.glm.ldm.oplan.*;
import org.cougaar.domain.glm.ldm.plan.*;
import org.cougaar.domain.glm.ldm.policy.*;

/**
 * .<pre>
 * Formerly called PSP_TransportVolume, but now accepts verb as
 * parameter argument.
 *
 * Find all incomming tasks to the cluster and add up the "volume"
 * of assets, including:
 *   counter
 *   total mass
 *   total physical volume
 *   etc.
 *
 * Limit Tasks by these optional filters:
 *   verb
 *   unit name
 *   task uid(s)   
 * </pre>
 */

public class PSP_InputTaskAssetVolume extends PSP_BaseAdapter implements PlanServiceProvider, UISubscriber {

  public static final String DEFAULT_VERB = "Transport";

  /**
   * A zero-argument constructor is required for dynamically loaded PSPs,
   *  required by Class.newInstance()
   **/
  public PSP_InputTaskAssetVolume() {
    super();
    setDebug();
  }

  /*************************************************************************
   * 
   **/
  public PSP_InputTaskAssetVolume( String pkg, String id ) throws RuntimePSPException {
    setResourceLocation(pkg, id);
  }

  /**************************************************************************
   * 
   **/
  public boolean test(HttpInput query_parameters, PlanServiceContext sc) {
    super.initializeTest(); // IF subclass off of PSP_BaseAdapter.java
    return false;  // This PSP is only accessed by direct reference.
  }

  /** minimal debug for potential errors **/
  public static final boolean MINI_DEBUG = true;

  /** DEBUG forced to off! **/
  public static final boolean DEBUG = false;
  protected boolean setDebug() {return DEBUG;}

  /****************************************************************************
   * Main execute method for PSP : Dispatch based on mode parameter
   **/
  public void execute(PrintStream out,
                      HttpInput query_parameters,
                      PlanServiceContext psc,
                      PlanServiceUtilities psu) 
    throws Exception 
  {
    MyPSPState myState = new MyPSPState(this, query_parameters, psc);
    myState.configure(query_parameters);

    System.out.println("PSP_InputTaskAssetVolume Invoked...");

    if (DEBUG) {
      System.out.println("key: "+myState.key);
      System.out.println("unitFilter: "+myState.forUnitFilter);
      System.out.println("taskFilter: "+myState.inputTaskFilter);
    }

    if (myState.key == myState.KEY_UNSET) {
      displayUsage(out, myState);
    } else {
      try {
        generateAssetMetrics(myState);
        displayAssetMetrics(out, myState);
      } catch (Exception topLevelException) {
        displayFailure(out, myState, topLevelException);
      }
    }
  }

  /** BEGIN UTILITIES **/

  protected static Enumeration searchUsingPredicate(
      MyPSPState myState, UnaryPredicate pred) 
  {
    Subscription subscription = 
      myState.sps.subscribe(myState.subscriber, pred);
    return ((CollectionSubscription)subscription).elements();
  }

  protected static String getForUnit(Task task) {
    PrepositionalPhrase prepFor= task.getPrepositionalPhrase(Constants.Preposition.FOR);
    if (prepFor != null) {
      Object indObj = prepFor.getIndirectObject();
      if (indObj instanceof String) {
        return ((String)indObj).trim();
      } else if (indObj instanceof Asset) {
        String s =
          ((Asset)indObj).getTypeIdentificationPG().getTypeIdentification();
        if (s != null)
          return s.trim();
      }
    }
    return null;
  }

  /** END UTILITIES **/

  /** 
   * Simple task filter.
   * <p>
   * Note: changed filter from "startsWith" to "equals"
   * <p>
   * @param task the Task we're testing
   * @param inputTaskFilter if non-null: task UID (equals String)/(in Set)
   * @param forUnitFilter if non-null: FOR equals this unit name 
   * @return true if match
   */
  protected static final boolean taskFitsFilters(
     Task task, 
     final Object inputTaskFilter,
     final String forUnitFilter) {
    if (inputTaskFilter != null) {
      String taskId = task.getUID().toString();
      if (inputTaskFilter instanceof String) {
        if (!(((String)inputTaskFilter).equals(taskId)))
          return false;
      } else  {
        if (!(((Set)inputTaskFilter).contains(taskId)))
          return false;
      }
    }
    if (forUnitFilter != null) {
      String forUnit = getForUnit(task);
      if ((forUnit == null) ||
          (!(forUnit.equals(forUnitFilter))))
        return false;
    }
    return true;
  }

  /** 
   *  Root tasks have:<br>
   * <ol>
   *   <li>source != destination</li>
   *   <li>optional verb filter</li>
   * </ol>
   */
  protected static UnaryPredicate getRootTasksPred(final Verb verbFilter) {
    if (verbFilter == null) {
      // could make predicate static
      return new UnaryPredicate() 
        {
          public boolean execute(Object o) {
            if (o instanceof Task) {
              Task task = (Task)o;
              if (!(task.getSource().equals(task.getDestination()))) {
                return true;
              }
            }
            return false;
          }
        };
    } else {
      // predicate depends on verb filter
      return new UnaryPredicate() 
        {
          public boolean execute(Object o) {
            if (o instanceof Task) {
              Task task = (Task)o;
              if ((!(task.getSource().equals(task.getDestination()))) &&
                  (verbFilter.equals(task.getVerb()))) {
                return true;
              }
            }
            return false;
          }
        };
    }
  }

  protected static UnaryPredicate getRootTasksPred(
      final Verb verbFilter,
      final Object inputTaskFilter,
      final String forUnitFilter) {
    if ((inputTaskFilter == null) &&
        (forUnitFilter == null)) {
      return getRootTasksPred(verbFilter);
    } else {
      return new UnaryPredicate() 
        {
          protected UnaryPredicate rootsPred = getRootTasksPred(verbFilter);
          public boolean execute(Object o) {
            return (rootsPred.execute(o) &&
                    taskFitsFilters((Task)o, inputTaskFilter, forUnitFilter));
          }
        };
    }
  }

  /**
   * filter the root tasks
   */
  protected static Enumeration searchForRootTasks(
      MyPSPState myState) {
    UnaryPredicate pred =
      getRootTasksPred(
        myState.verbFilter, 
        myState.inputTaskFilter, 
        myState.forUnitFilter);
    return searchUsingPredicate(myState, pred);
  }

  /** AssetMetrics class for calculating metrics **/

  protected static class AssetMetrics 
    implements org.cougaar.core.util.SelfPrinter, java.io.Serializable {

    private AssetMetrics() {}
    public AssetMetrics(String xidentifier) {
      setIdentifier(xidentifier);
    }

    private String identifier;
    public String getIdentifier() {
      return identifier;
    }
    public void setIdentifier(String xidentifier) {
      identifier = xidentifier;
    }

    private long counter;
    public long getCounter() {
      return counter;
    }
    public void setCounter(long xcounter) {
      counter = xcounter;
    }
    public void incrementCounter() {
      counter++;
    }
    public void incrementCounter(int quantity) {
      counter += quantity;
    }

    // mass
    private long counterMass;
    private double totalMassGrams;
    public void setCounterMass(long xcounterMass) {
      counterMass = xcounterMass;
    }
    public long getCounterMass() {
      return counterMass;
    }
    public void setTotalMassGrams(double xtotalMassGrams) {
      totalMassGrams = xtotalMassGrams;
    }
    public double getTotalMassGrams() {
      return totalMassGrams;
    }
    public void addMass(Mass mass, int quantity) {
      if (mass != null) {
        counterMass += quantity;
        totalMassGrams += (quantity * mass.getGrams());
      } else {
        if (DEBUG) {
          System.out.println("  lacks mass");
        }
      }
    }
    public double getAverageMassGrams() {
      if (counterMass <= 0) {
        return 0;
      }
      try {
        return (totalMassGrams/counterMass);
      } catch (Exception eBadMath) {
        return 0;
      }
    }
    public Mass getTotalMass() {
      return Mass.newMass(totalMassGrams, Mass.GRAMS);
    }
    public Mass getAverageMass() {
      return Mass.newMass(getAverageMassGrams(), Mass.GRAMS);
    }

    // volume
    private long counterVolume;
    private double totalVolumeLiters;
    public long getCounterVolume() {
      return counterVolume;
    }
    public void setCounterVolume(long xcounterVolume) {
      counterVolume = xcounterVolume;
    }
    public double getTotalVolumeLiters() {
      return totalVolumeLiters;
    }
    public void setTotalVolumeLiters(double xtotalVolumeLiters) {
      totalVolumeLiters = xtotalVolumeLiters;
    }
    public void addVolume(Volume volume, int quantity) {
      if (volume != null) {
        counterVolume += quantity;
        totalVolumeLiters += (quantity * volume.getLiters());
      } else {
        if (DEBUG) {
          System.out.println("  lacks volume");
        }
      }
    }
    public double getAverageVolumeLiters() {
      if (counterVolume <= 0) {
        return 0;
      }
      try {
        return (totalVolumeLiters/counterVolume);
      } catch (Exception eBadMath) {
        return 0;
      }
    }
    public Volume getTotalVolume() {
      return Volume.newVolume(totalVolumeLiters, Volume.LITERS);
    }
    public Volume getAverageVolume() {
      return Volume.newVolume(getAverageVolumeLiters(), Volume.LITERS);
    }

    // footprint area
    private long counterFootprintArea;
    private double totalFootprintAreaSquareMeters;
    public long getCounterFootprintArea() {
      return counterFootprintArea;
    }
    public void setCounterFootprintArea(long xcounterFootprintArea) {
      counterFootprintArea = xcounterFootprintArea;
    }
    public double getTotalFootprintAreaSquareMeters() {
      return totalFootprintAreaSquareMeters;
    }
    public void setTotalFootprintAreaSquareMeters(
        double xtotalFootprintAreaSquareMeters) {
      totalFootprintAreaSquareMeters = 
        xtotalFootprintAreaSquareMeters;
    }
    public void addFootprintArea(Area footprintArea, int quantity) {
      if (footprintArea != null) {
        counterFootprintArea += quantity;
        totalFootprintAreaSquareMeters += 
          (quantity * footprintArea.getSquareMeters());
      } else {
        if (DEBUG) {
          System.out.println("  lacks footprint area");
        }
      }
    }
    public double getAverageFootprintAreaSquareMeters() {
      if (counterFootprintArea <= 0) {
        return 0;
      }
      try {
        return (totalFootprintAreaSquareMeters/counterFootprintArea);
      } catch (Exception eBadMath) {
        return 0;
      }
    }
    public Area getTotalFootprintArea() {
      return Area.newArea(totalFootprintAreaSquareMeters, 
                          Area.SQUARE_METERS);
    }
    public Area getAverageFootprintArea() {
      return Area.newArea(getAverageFootprintAreaSquareMeters(),
                          Area.SQUARE_METERS);
    }

    // add by physicalPG
    public void add(PhysicalPG physPG, int quantity) {
      incrementCounter(quantity);
      addMass(physPG.getMass(), quantity);
      addVolume(physPG.getVolume(), quantity);
      addFootprintArea(physPG.getFootprintArea(), quantity);
    }

    public void printContent(org.cougaar.core.util.AsciiPrinter pr) {
      pr.print(identifier, "Identifier");
      pr.print(counter, "Counter");
      // mass
      pr.print(counterMass, "CounterMass");
      pr.print(totalMassGrams, "TotalMassGrams");
      // volume
      pr.print(counterVolume, "CounterVolume");
      pr.print(totalVolumeLiters, "TotalVolumeLiters");
      // footprint area
      pr.print(counterFootprintArea, "CounterFootprintArea");
      pr.print(totalFootprintAreaSquareMeters, 
               "TotalFootprintAreaSquareMeters");
    }

    public String toString() {
      return org.cougaar.core.util.PrettyStringPrinter.toString(this);
    }
  }

  /** routines for recording Assets in the metrics **/

  /**
   * recursive!
   * given Task's directObject, add metrics for the asset(s).
   */
  private static void addAssetMetrics(
      MyPSPState myState, Asset dirObj) {
    if (dirObj instanceof AssetGroup) {
      if (DEBUG) {
        System.err.println("Asset Group: "+dirObj.getUID().getUID());
      }
      Enumeration assetsEn = ((AssetGroup)dirObj).getAssets().elements();
      while (assetsEn.hasMoreElements()) {
        addAssetMetrics(myState, (Asset)assetsEn.nextElement());
      }
    } else if (dirObj != null) {
      if (DEBUG) {
        System.err.println("Asset: "+dirObj.getUID().getUID());
      }
      int quantity = 1;
      while (dirObj instanceof AggregateAsset) {
        AggregateAsset agg = (AggregateAsset)dirObj;
        quantity *= (int)agg.getQuantity();
        dirObj = agg.getAsset();
      } 
      if (DEBUG) {
        System.err.println("Quantity: "+quantity);
      }
      // have quantity and info
      PhysicalPG physPG =null;
      if (dirObj instanceof GLMAsset &&
          (physPG = ((GLMAsset)dirObj).getPhysicalPG()) != null) {
        if (myState.key == myState.KEY_NONE) {
          // all assets in one big metrics
          if (DEBUG) {
            System.err.println("Add to big metric");
          }
          myState.allMetrics.add(physPG, quantity);
        } else {
          // assets have quality that groups them in map
          Object key = null;
          switch (myState.key) {
            case MyPSPState.KEY_TYPEID: {
                TypeIdentificationPG tipg = 
                  dirObj.getTypeIdentificationPG();
                if (tipg != null) {
                  key = tipg.getTypeIdentification();
                } else {
                  if (DEBUG) {
                    System.err.println("Lacks TypeID");
                  }
                }
              }
              break;
            case MyPSPState.KEY_TYPENOMEN: {
                TypeIdentificationPG tipg = 
                  dirObj.getTypeIdentificationPG();
                if (tipg != null) {
                  key = tipg.getNomenclature();
                } else {
                  if (DEBUG) {
                    System.err.println("Lacks TypeNomen");
                  }
                }
              }
              break;
            case MyPSPState.KEY_ITEMID: {
                ItemIdentificationPG iipg = 
                  dirObj.getItemIdentificationPG();
                if (iipg != null) {
                  key = iipg.getItemIdentification();
                } else {
                  if (DEBUG) {
                    System.err.println("Lacks ItemID");
                  }
                }
              }
              break;
            default:
              // invalid key? shouldn't happen!
              if (DEBUG) {
                System.err.println("Invalid key?");
              }
              break;
          }
          if (key != null) {
            AssetMetrics am;
            Object o = myState.seenAssets.get(key);
            if (o == null) {
              am = new AssetMetrics(key.toString());
              myState.seenAssets.put(key, am);
            } else {
              am = (AssetMetrics)o;
            }
            // add to info about asset type
            am.add(physPG, quantity);
          } else {
            // key value is null?
            if (DEBUG) {
              System.err.println("Key value is null?");
            }
            myState.unaccounted++;
          }
        }
      } else {
        // no phys info?
        if (DEBUG) {
          System.err.println("Asset lacks PhysicalPG");
        }
        myState.unaccounted++;
      }
    } else {
      // dirObj is null?
      if (DEBUG) {
        System.err.println("Task with null directObject");
      }
      myState.unaccounted++;
    }
  }

  /** Generate metrics data **/

  protected static void generateAssetMetrics(MyPSPState myState)
    throws  IOException
  {
    if (DEBUG) {
      System.out.println("Find root task direct objects");
    }
    // find root tasks
    Enumeration rtEn = searchForRootTasks(myState);

    // make metrics table
    if (myState.key == myState.KEY_NONE) {
      myState.allMetrics = new AssetMetrics("All Assets");
    } else {
      myState.seenAssets = new HashMap();
    }

    // add metrics for each root task
    while (rtEn.hasMoreElements()) {
      Task rootTask = (Task)rtEn.nextElement();
      if (DEBUG) {
        System.out.println("ROOT Task: "+(rootTask.getUID().toString()));
      }
      addAssetMetrics(myState, rootTask.getDirectObject());
    }

    // "t" has all the info needed
  }

  /** HTML output displays **/
  protected static void displayUsage(PrintStream out, MyPSPState myState) 
  {
    out.print(
     "<HTML><HEAD><TITLE>Input Task Asset Volume</TITLE></HEAD>\n"+
     "<BODY BGCOLOR=\"#DDDDDD\">\n"+
     "<H2><CENTER>Input Task Asset Volume for Cluster ");
    out.print(myState.clusterID);
    out.print(
     "</CENTER></H2><P>\n"+
      "<FORM METHOD=\"POST\" ACTION=\"");
    out.print(myState.cluster_psp_url);
    out.print(
      "?POST\">\n"+
      "&nbsp;Group Assets: <SELECT NAME=\""+
      myState.KEY+
      "\">\n"+
      "  <OPTION SELECTED VALUE=\"");
    out.print(myState.KEY_NONE);
    out.print("\">"+
      "All together"+
      "</OPTION>\n"+
      "  <OPTION VALUE=\"");
    out.print(myState.KEY_TYPEID);
    out.print("\">"+
      "By Type Identification"+
      "</OPTION>\n"+
      "  <OPTION VALUE=\"");
    out.print(myState.KEY_TYPENOMEN);
    out.print("\">"+
      "By Type Nomenclature"+
      "</OPTION>\n"+
      "  <OPTION VALUE=\"");
    out.print(myState.KEY_ITEMID);
    out.print("\">"+
      "By Item Identification"+
      "</OPTION>\n"+
      "</SELECT><br>\n"+
      "&nbsp;Optional Verb filter: "+
      "<INPUT TYPE=\"text\" NAME=\""+
      myState.VERB_FILTER+
      "\" SIZE=20 VALUE=\"");
    if (DEFAULT_VERB != null) {
      out.print(DEFAULT_VERB);
    }
    out.print("\"><br>\n"+
      "&nbsp;Optional ForUnit filter: "+
      "<INPUT TYPE=\"text\" NAME=\""+
      myState.FOR_UNIT_FILTER+
      "\" SIZE=20><br>\n"+
      "&nbsp;Optional TaskUID filter: "+
      "<INPUT TYPE=\"text\" NAME=\""+
      myState.INPUT_TASK_FILTER+
      "\" SIZE=20><br>\n"+
      "<INPUT TYPE=\"submit\" NAME=\"FETCH\" VALUE=\"GET METRICS!\">\n"+
      "</FORM></BODY></HTML>\n");
  }

  protected static void printMetricHTML(PrintStream out, AssetMetrics am) {
    out.print("<TR>\n<TD>");
    // id
    out.print(am.getIdentifier());
    out.print("</TD>\n<TD>");
    // counter
    out.print(am.getCounter());
    out.print("</TD>\n<TD>");
    // mass
    out.print(am.getCounterMass());
    out.print("</TD>\n<TD>");
    out.print(am.getTotalMassGrams());
    out.print("</TD>\n<TD>");
    out.print(am.getAverageMassGrams());
    out.print("</TD>\n<TD>");
    // volume
    out.print(am.getCounterVolume());
    out.print("</TD>\n<TD>");
    out.print(am.getTotalVolumeLiters());
    out.print("</TD>\n<TD>");
    out.print(am.getAverageVolumeLiters());
    out.print("</TD>\n<TD>");
    // footprint area
    out.print(am.getCounterFootprintArea());
    out.print("</TD>\n<TD>");
    out.print(am.getTotalFootprintAreaSquareMeters());
    out.print("</TD>\n<TD>");
    out.print(am.getAverageFootprintAreaSquareMeters());
    out.print("</TD>\n</TR>");
  }

  protected static void displayAssetMetrics(
      PrintStream out, MyPSPState myState) 
  {
    out.print(
     "<HTML><HEAD><TITLE>Input Task Asset Volume</TITLE></HEAD>\n"+
     "<BODY BGCOLOR=\"#DDDDDD\">\n"+
     "<H2><CENTER>Input Task Asset Volume for Cluster ");
    out.print(myState.clusterID);
    out.print(
     "</CENTER></H2><P>\n");
    if (myState.verbFilter != null) {
      out.print("Verb Filter: ");
      out.print(myState.verbFilter);
      out.print("<p>\n");
    }
    if (myState.forUnitFilter != null) {
      out.print("Unit Filter: ");
      out.print(myState.forUnitFilter);
      out.print("<p>\n");
    }
    if (myState.inputTaskFilter != null) {
      out.print("Input Task Filter: ");
      out.print(myState.inputTaskFilter);
      out.print("<p>\n");
    }
    // begin table
    out.print(
      "<TABLE align=center border=1 cellPadding=1\n"+
      " bgcolor=\"#FOFOFO\" cellSpacing=1 width=75%\n"+
      " bordercolordark=\"#660000\" bordercolorlight=\"#cc9966\">\n"+
      "<TR>\n"+
      "<TD rowspan=2><FONT color=mediumblue><B>");
    String key_id;
    switch (myState.key) {
      case MyPSPState.KEY_TYPEID: 
        key_id = "Type Identification"; 
        break;
      case MyPSPState.KEY_TYPENOMEN: 
        key_id = "Type Nomenclature"; 
        break;
      case MyPSPState.KEY_ITEMID: 
        key_id = "Item Identification"; 
        break;
      case MyPSPState.KEY_NONE: 
        key_id = "All Assets"; 
        break;
      default:
        key_id = "ID"; 
        break;
    }
    out.print(key_id);
    out.print(
      "</FONT></B></TD>\n"+
      "<TD rowspan=2><FONT color=mediumblue><B>Count</FONT></B></TD>\n"+
      "<TD colspan=3><FONT color=mediumblue><B>Mass</FONT></B></TD>\n"+
      "<TD colspan=3><FONT color=mediumblue><B>Volume</FONT></B></TD>\n"+
      "<TD colspan=3><FONT color=mediumblue><B>FootprintArea</FONT></B></TD>\n"+
      "</TR>\n"+
      "<TR>\n"+
      "<TD><FONT color=mediumblue><B>Count</FONT></B></TD>\n"+
      "<TD><FONT color=mediumblue><B>Total Grams</FONT></B></TD>\n"+
      "<TD><FONT color=mediumblue><B>Average Grams</FONT></B></TD>\n"+
      "<TD><FONT color=mediumblue><B>Count</FONT></B></TD>\n"+
      "<TD><FONT color=mediumblue><B>Total Liters</FONT></B></TD>\n"+
      "<TD><FONT color=mediumblue><B>Average Liters</FONT></B></TD>\n"+
      "<TD><FONT color=mediumblue><B>Count</FONT></B></TD>\n"+
      "<TD><FONT color=mediumblue><B>Total Square Meters</FONT></B></TD>\n"+
      "<TD><FONT color=mediumblue><B>Average Square Meters</FONT></B></TD>\n"+
      "</TR>\n");
    // print unaccounted
    if (myState.unaccounted > 0) {
      out.print(
        "<TD><FONT COLOR=red>Unaccounted</FONT></TD>\n"+
        "<TD>");
      out.print(myState.unaccounted);
      out.print(
        "</TD>\""+
        "<TD colspan=9><I>Due to either missing DirectObject");
      if (myState.key == myState.KEY_NONE) {
        out.print(
          " or PhysicalPG");
      } else {
        out.print(
          ", PhysicalPG, or ");
        out.print(key_id);
      }
      out.print(
        "</I></TD>\n"+
        "</TR>\n");
    }
    // print table rows
    if (myState.key == myState.KEY_NONE) {
      printMetricHTML(out, myState.allMetrics);
    } else {
      Collection col = myState.seenAssets.values();
      Iterator iter = col.iterator();
      int rows = 0;
      while (iter.hasNext()) {
        AssetMetrics am = (AssetMetrics)iter.next();
        printMetricHTML(out, am);
        if ((++rows % 50) == 0) {
          // restart table
          out.print("</TABLE>\n");
          out.flush();
          out.print(
            "<TABLE align=center border=1 cellPadding=1\n"+
            " bgcolor=\"#FOFOFO\" cellSpacing=1 width=75%\n"+
            " bordercolordark=\"#660000\" bordercolorlight=\"#cc9966\">\n");
        }
      }
    }
    // end table
    out.print(
      "</TABLE>\n"+
      "</BODY></HTML>\n");
  }

  protected static void displayFailure(
      PrintStream out, MyPSPState myState, Exception e) 
  {
    // PSP error output
    System.err.print(
      "PSP_InputTaskAssetVolume: Exception processing PSP_InputTaskAssetVolume: ");
    System.err.println(e);
    e.printStackTrace();
    // HTML error output
    out.print(
     "<HTML><HEAD><TITLE>Failed Input Task Asset Volume</TITLE></HEAD>\n"+
     "<BODY BGCOLOR=\"#DDDDDD\">\n"+
     "<H2><CENTER>Failed Input Task Asset Volume for Cluster ");
    out.print(myState.clusterID);
    out.print(
     "</CENTER></H2><P>\n"+
     "Exception: <p>");
    e.printStackTrace(out);
    out.print("</BODY></HTML>\n");
  }

  /**
   * A PSP can output either HTML or XML (for now).  The server
   * should be able to ask and find out what type it is.
   **/
  public boolean returnsXML() { return true; }

  /**
   * A PSP can output either HTML or XML (for now).  The server
   * should be able to ask and find out what type it is.
   **/
  public boolean returnsHTML() { return false; }

  /**  Any PlanServiceProvider must be able to provide DTD of its
   *  output IFF it is an XML PSP... ie.  returnsXML() == true;
   *  or return null
   **/
  public String getDTD()  {
      return "";
  }

  /***************************************************************************
   * 
   **/
  public void subscriptionChanged(Subscription subscription) {
  }

  protected static class MyPSPState extends PSPState {

    /** my additional fields **/

    // filters
    public static final String VERB_FILTER = "Verb";
    public Verb verbFilter;

    public static final String FOR_UNIT_FILTER = "TransportedUnitName";
    public String forUnitFilter;

    public static final String INPUT_TASK_FILTER = "InputTaskUID";
    public Object inputTaskFilter;

    // key for deciding asset grouping, i.e. what property of
    // an asset is used to differentiate to assets?
    public static final int KEY_MIN = -1;
    public static final int KEY_UNSET = -1;
    public static final int KEY_TYPEID = 0;
    public static final int KEY_TYPENOMEN = 1;
    public static final int KEY_ITEMID = 2;
    public static final int KEY_NONE = 3;
    public static final int KEY_MAX = 3;

    public static final String KEY = "key";
    public int key;
    // map of (key -> asset metrics) for key != NONE
    public HashMap seenAssets;
    // single asset metric for key == NONE
    public AssetMetrics allMetrics;
    // unaccounted assets, e.g. lack PhysicalPG, etc
    public long unaccounted;

    /** constructor **/
    public MyPSPState(
        UISubscriber xsubscriber,
        HttpInput query_parameters,
        PlanServiceContext xpsc) {
      super(xsubscriber, query_parameters, xpsc);
      /**************************************************
       * Original verb behavior always had DEFAULT_VERB *
       **************************************************/
      verbFilter = 
        ((DEFAULT_VERB != null) ?
         Verb.getVerb(DEFAULT_VERB) :
         null);
      forUnitFilter = null;
      inputTaskFilter = null;
      key = KEY_UNSET;
      seenAssets = null;
      allMetrics = null;
      unaccounted = 0;
    }

    /** use a query parameter to set a field **/
    public void setParam(String name, String value) {
      //super.setParam(name, value);
      if (name.equalsIgnoreCase(KEY)) {
        try {
          key = Integer.parseInt(value);
        } catch (Exception eNotNumber) {
          key = KEY_UNSET; 
        }
        if ((key < KEY_MIN) || (key > KEY_MAX)) {
          key = KEY_UNSET;
        }
      } else if (name.equalsIgnoreCase(VERB_FILTER)) {
        verbFilter = 
          ((value != null) ?
           Verb.getVerb(value) :
           null);
      } else if (name.equalsIgnoreCase(FOR_UNIT_FILTER)) {
        forUnitFilter = value;
      } else if (name.equalsIgnoreCase(INPUT_TASK_FILTER)) {
        // currently only single String filter -- can add java.util.Set
        inputTaskFilter = value;
      }
    }
  }

}
